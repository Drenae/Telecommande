package com.telecommande.core.remote

import com.google.protobuf.InvalidProtocolBufferException
import com.telecommande.core.event.RemoteEvent
import com.telecommande.core.wire.PacketParser
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.isActive
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
        if (!currentCoroutineContext().isActive) return
        if (buf.isEmpty()) {
            Timber.w("Tampon de message distant reçu vide, ignoré.")
            return
        }

        val remoteMessage: Remotemessage.RemoteMessage = try {
            Remotemessage.RemoteMessage.parseFrom(buf)
        } catch (e: InvalidProtocolBufferException) {
            Timber.e(e, "Échec de l'analyse du RemoteMessage: %s", e.message)
            eventFlow.emit(RemoteEvent.Error("Erreur de protocole : impossible d'analyser le message distant reçu."))
            messagesChannel.close(e)
            return
        }

        traceIncomingMessage(remoteMessage, buf)

        try {
            when {
                remoteMessage.hasRemotePingRequest() -> {
                    val pingResponse = remoteMessageManager.createPingResponse(remoteMessage.remotePingRequest.val1)
                    synchronized(outputStream) {
                        outputStream.write(pingResponse)
                        outputStream.flush()
                    }
                }

                remoteMessage.hasRemoteStart() -> {
                    Timber.i("Message RemoteStart reçu.")
                    if (!hasNotifiedConnected) {
                        hasNotifiedConnected = true
                        eventFlow.emit(RemoteEvent.Connected)
                        Timber.i("Connexion à distance établie (notifiée via RemoteStart).")
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

                remoteMessage.hasRemoteImeShowRequest() -> {
                    val textField = remoteMessage.remoteImeShowRequest.remoteTextFieldStatus
                    val label = textField.label.takeIf { it.isNotBlank() }
                    Timber.i(
                        "TV IME demandée : label=%s, valeur=%s, sélection=%d..%d, fieldCounter=%d",
                        label ?: "<sans label>",
                        textField.value,
                        textField.start,
                        textField.end,
                        textField.counterField
                    )
                    eventFlow.emit(
                        RemoteEvent.TextInputRequested(
                            value = textField.value,
                            selectionStart = textField.start,
                            selectionEnd = textField.end,
                            fieldCounter = textField.counterField,
                            label = label
                        )
                    )
                }

                else -> sendToChannel(remoteMessage)
            }
        } catch (e: IOException) {
            Timber.e(e, "IOException lors du traitement du message distant: %s", e.message)
            if (currentCoroutineContext().isActive) {
                eventFlow.emit(RemoteEvent.Error("Erreur réseau distante: ${e.message}"))
                eventFlow.emit(RemoteEvent.Disconnected)
                messagesChannel.close(e)
            }
        } catch (e: Exception) {
            Timber.e(e, "Erreur inattendue lors du traitement du message distant: %s", e.message)
            if (currentCoroutineContext().isActive) {
                eventFlow.emit(RemoteEvent.Error("Erreur interne lors du traitement du message: ${e.message}"))
                messagesChannel.close(e)
            }
        }
    }

    private fun traceIncomingMessage(message: Remotemessage.RemoteMessage, raw: ByteArray) {
        val type = when {
            message.hasRemoteConfigure() -> "RemoteConfigure"
            message.hasRemoteSetActive() -> "RemoteSetActive"
            message.hasRemoteError() -> "RemoteError"
            message.hasRemotePingRequest() -> "RemotePingRequest"
            message.hasRemotePingResponse() -> "RemotePingResponse"
            message.hasRemoteKeyInject() -> "RemoteKeyInject"
            message.hasRemoteImeKeyInject() -> "RemoteImeKeyInject"
            message.hasRemoteImeBatchEdit() -> "RemoteImeBatchEdit"
            message.hasRemoteImeShowRequest() -> "RemoteImeShowRequest"
            message.hasRemoteVoiceBegin() -> "RemoteVoiceBegin"
            message.hasRemoteVoicePayload() -> "RemoteVoicePayload"
            message.hasRemoteVoiceEnd() -> "RemoteVoiceEnd"
            message.hasRemoteStart() -> "RemoteStart"
            message.hasRemoteSetVolumeLevel() -> "RemoteSetVolumeLevel"
            message.hasRemoteAdjustVolumeLevel() -> "RemoteAdjustVolumeLevel"
            message.hasRemoteSetPreferredAudioDevice() -> "RemoteSetPreferredAudioDevice"
            message.hasRemoteResetPreferredAudioDevice() -> "RemoteResetPreferredAudioDevice"
            message.hasRemoteAppLinkLaunchRequest() -> "RemoteAppLinkLaunchRequest"
            else -> "INCONNU"
        }
        val hex = raw.joinToString(" ") { "%02X".format(it.toInt() and 0xFF) }
        Timber.d("TV RX: type=%s, longueur=%d, hex=[%s]", type, raw.size, hex)
        if (type == "INCONNU") Timber.w("TV RX message inconnu décodé: %s", message.toString().take(500))
    }

    private suspend fun sendToChannel(message: Remotemessage.RemoteMessage) {
        messagesChannel.send(message)
    }
}
