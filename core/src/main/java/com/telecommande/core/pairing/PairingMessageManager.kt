package com.telecommande.core.pairing

import com.google.protobuf.ByteString
import com.telecommande.core.wire.MessageManager
import timber.log.Timber

internal class PairingMessageManager : MessageManager() {
    fun createPairingMessage(clientName: String, serviceName: String): ByteArray {
        Timber.d("Création du message d'appairage pour client: %s, service: %s", clientName, serviceName)
        val pairingMessageProto = Pairingmessage.PairingMessage.newBuilder()
            .setPairingRequest(
                Pairingmessage.PairingRequest.newBuilder()
                    .setClientName(clientName)
                    .setServiceName(serviceName)
            )
            .setStatus(Pairingmessage.PairingMessage.Status.STATUS_OK)
            .setProtocolVersion(2)
            .build()
        return addLengthAndCreate(pairingMessageProto.toByteArray())
    }

    fun createPairingOption(): ByteArray {
        Timber.d("Création du message d'option d'appairage.")
        val pairingEncoding = Pairingmessage.PairingEncoding.newBuilder()
            .setType(Pairingmessage.PairingEncoding.EncodingType.ENCODING_TYPE_HEXADECIMAL)
            .setSymbolLength(6)
            .build()

        val pairingOptionProto = Pairingmessage.PairingMessage.newBuilder()
            .setPairingOption(
                Pairingmessage.PairingOption.newBuilder()
                    .setPreferredRole(Pairingmessage.RoleType.ROLE_TYPE_INPUT)
                    .addInputEncodings(pairingEncoding)
            )
            .setStatus(Pairingmessage.PairingMessage.Status.STATUS_OK)
            .setProtocolVersion(2)
            .build()
        return addLengthAndCreate(pairingOptionProto.toByteArray())
    }

    fun createConfigMessage(): ByteArray {
        Timber.d("Création du message de configuration d'appairage.")
        val pairingEncoding = Pairingmessage.PairingEncoding.newBuilder()
            .setType(Pairingmessage.PairingEncoding.EncodingType.ENCODING_TYPE_HEXADECIMAL)
            .setSymbolLength(6)
            .build()

        val pairingConfigProto = Pairingmessage.PairingMessage.newBuilder()
            .setPairingConfiguration(
                Pairingmessage.PairingConfiguration.newBuilder()
                    .setClientRole(Pairingmessage.RoleType.ROLE_TYPE_INPUT)
                    .setEncoding(pairingEncoding)
            )
            .setStatus(Pairingmessage.PairingMessage.Status.STATUS_OK)
            .setProtocolVersion(2)
            .build()
        return addLengthAndCreate(pairingConfigProto.toByteArray())
    }

    fun createSecretMessage(pairingSecretMessageProto: Pairingmessage.PairingMessage): ByteArray {
        Timber.d("Création du message secret à partir du proto fourni.")
        return addLengthAndCreate(pairingSecretMessageProto.toByteArray())
    }

    fun createSecretMessageProto(secret: ByteArray): Pairingmessage.PairingMessage {
        Timber.d("Création du proto de message secret à partir des octets du secret.")
        return Pairingmessage.PairingMessage.newBuilder()
            .setPairingSecret(
                Pairingmessage.PairingSecret.newBuilder().setSecret(ByteString.copyFrom(secret))
            )
            .setStatus(Pairingmessage.PairingMessage.Status.STATUS_OK)
            .setProtocolVersion(2)
            .build()
    }
}
