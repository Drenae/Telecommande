package com.telecommande.core.remote

import com.google.protobuf.InvalidProtocolBufferException
import com.telecommande.core.event.RemoteEvent
import com.telecommande.core.wire.PacketParser
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class RemotePacketParser(
    inputStream: InputStream,
    private val outputStream: OutputStream,
    private val messagesChannel: SendChannel<Remotemessage.RemoteMessage>,
    private val eventFlow: MutableSharedFlow<RemoteEvent>
) : PacketParser(inputStream) {

    private val remoteMessageManager: RemoteMessageManager = RemoteMessageManager()
    private var hasNotifiedConnected = false

    override suspend fun messageBufferReceived(buf: ByteArray) {
        if (!currentCoroutineContext().isActive) {
            Timber.d("Contexte de coroutine distant inactif, ne pas traiter le tampon de message reçu.")
            return
        }
        if (buf.isEmpty()) {
            Timber.w("Tampon de message distant reçu vide, ignoré.")
            return
        }

        Timber.v("Tampon de message distant brut reçu (longueur : %d)", buf.size)

        val remoteMessage: Remotemessage.RemoteMessage = try {
            Remotemessage.RemoteMessage.parseFrom(buf)
        } catch (e: InvalidProtocolBufferException) {
            Timber.e(e, "Échec de l'analyse du RemoteMessage: %s", e.message)
            eventFlow.emit(RemoteEvent.Error("Erreur de protocole : impossible d'analyser le message distant reçu."))
            messagesChannel.close(e)
            return
        }

        Timber.v("RemoteMessage distant analysé : %s", remoteMessage.toString().take(300))

        try {
            when {
                remoteMessage.hasRemotePingRequest() -> {
                    Timber.d("Requête Ping distante reçue : val1=%d", remoteMessage.remotePingRequest.val1)
                    val pingResponse = remoteMessageManager.createPingResponse(remoteMessage.remotePingRequest.val1)
                    withContext(currentCoroutineContext()) {
                        outputStream.write(pingResponse)
                    }
                    Timber.d("Réponse Ping distante envoyée.")
                }
                remoteMessage.hasRemoteStart() -> {
                    Timber.i("Message RemoteStart reçu.")
                    if (!hasNotifiedConnected) {
                        hasNotifiedConnected = true
                        eventFlow.emit(RemoteEvent.Connected)
                        Timber.i("Connexion à distance établie (notifiée via RemoteStart).")
                    } else {
                        Timber.d("RemoteStart reçu mais déjà notifié comme connecté.")
                    }
                    sendToChannel(remoteMessage)
                }
                remoteMessage.hasRemoteSetVolumeLevel() -> {
                    val volumeInfo = remoteMessage.remoteSetVolumeLevel
                    Timber.i(
                        "Message RemoteSetVolumeLevel reçu : Niveau=%d, Max=%d, Muet=%b, ModèleJoueur=%s",
                        volumeInfo.volumeLevel,
                        volumeInfo.volumeMax,
                        volumeInfo.volumeMuted,
                        volumeInfo.playerModel
                    )
                    eventFlow.emit(
                        RemoteEvent.VolumeStateChanged(
                            level = volumeInfo.volumeLevel.toInt(),
                            max = volumeInfo.volumeMax.toInt(),
                            muted = volumeInfo.volumeMuted,
                            deviceName = volumeInfo.playerModel.takeIf { it.isNotEmpty() }
                        )
                    )
                }
                else -> {
                    Timber.v("Mise en file d'attente du RemoteMessage (type non spécifiquement géré ici)")
                    sendToChannel(remoteMessage)
                }
            }
        } catch (e: IOException) {
            Timber.e(e, "IOException lors du traitement du message distant ou de l'envoi de la réponse Ping: %s", e.message)
            if (currentCoroutineContext().isActive) {
                eventFlow.emit(RemoteEvent.Error("Erreur réseau distante: ${e.message}"))
                eventFlow.emit(RemoteEvent.Disconnected)
                messagesChannel.close(e)
            }
        } catch (e: Exception) {
            Timber.e(e, "Erreur inattendue lors du traitement du message distant: %s", e.message)
            if (currentCoroutineContext().isActive && !messagesChannel.isClosedForSend) {
                eventFlow.emit(RemoteEvent.Error("Erreur interne lors du traitement du message: ${e.message}"))
                messagesChannel.close(e)
            }
        }
    }

    private suspend fun sendToChannel(message: Remotemessage.RemoteMessage) {
        if (messagesChannel.isClosedForSend) {
            Timber.w("Canal de messages distant déjà fermé, impossible d'envoyer le message.")
            return
        }
        messagesChannel.send(message)
        Timber.v("Message distant envoyé au canal de la session.")
    }
}