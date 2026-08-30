package com.telecommande.core.pairing

import com.google.protobuf.InvalidProtocolBufferException
import com.telecommande.core.wire.PacketParser
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import timber.log.Timber
import java.io.InputStream

class PairingPacketParser(
    inputStream: InputStream,
    private val messagesChannel: SendChannel<Pairingmessage.PairingMessage>
) : PacketParser(inputStream) {

    override suspend fun messageBufferReceived(buf: ByteArray) {
        if (!currentCoroutineContext().isActive) {
            Timber.d("Contexte de coroutine inactif, ne pas traiter le tampon de message reçu.")
            return
        }
        try {
            if (buf.isEmpty()) {
                Timber.w("Tampon de message reçu vide, tentative d'analyse ignorée pour PairingMessage.")
                return
            }
            val pairingMessage = Pairingmessage.PairingMessage.parseFrom(buf)
            Timber.v("Message d'appairage analysé: %s", pairingMessage.status)

            if (pairingMessage.status == Pairingmessage.PairingMessage.Status.STATUS_OK) {
                messagesChannel.send(pairingMessage)
                Timber.d("Message d'appairage avec statut OK envoyé au canal.")
            } else {
                Timber.w("Message d'appairage reçu avec un statut non-OK: %s. Message non envoyé au canal principal.", pairingMessage.status)
            }
        } catch (e: InvalidProtocolBufferException) {
            Timber.e(e, "Échec de l'analyse du tampon en PairingMessage.")
            messagesChannel.close(e)
        } catch (e: Exception) {
            if (currentCoroutineContext().isActive) {
                Timber.e(e, "Erreur lors de l'envoi du message d'appairage au canal.")
                messagesChannel.close(e)
            } else {
                Timber.d("Erreur lors de l'envoi au canal alors que la coroutine est inactive: %s", e.message)
            }
        }
    }
}
