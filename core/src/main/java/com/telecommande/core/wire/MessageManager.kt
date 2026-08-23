package com.telecommande.core.wire

import timber.log.Timber
import java.nio.ByteBuffer
import java.nio.ByteOrder

abstract class MessageManager {

    protected val packetBuffer: ByteBuffer = ByteBuffer.allocate(65539)

    init {
        packetBuffer.order(ByteOrder.BIG_ENDIAN)
    }

    protected fun addLengthAndCreate(message: ByteArray): ByteArray {
        val length = message.size
        if (length > 255) {
            Timber.e("La longueur du message (%d) dépasse le maximum autorisé par le préfixe d'un seul octet (255).", length)
        }

        packetBuffer.clear()
        packetBuffer.put(length.toByte())
        packetBuffer.put(message)
        packetBuffer.flip()

        val combinedArray = ByteArray(packetBuffer.remaining())
        packetBuffer.get(combinedArray)

        val logMessage = "Envoi d'octets (longueur: ${combinedArray.size}): [${
            combinedArray.joinToString(separator = " ") { String.format("%02X", it) }
        }]"
        Timber.v(logMessage)

        return combinedArray
    }
}