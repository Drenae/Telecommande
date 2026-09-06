package com.telecommande.core.wire

import timber.log.Timber
import java.io.ByteArrayOutputStream

abstract class MessageManager {

    protected fun addLengthAndCreate(message: ByteArray): ByteArray {
        val output = ByteArrayOutputStream(message.size + 5)
        writeVarint32(output, message.size)
        output.write(message)
        val combinedArray = output.toByteArray()

        val logMessage = "Envoi d'octets (longueur: ${combinedArray.size}): [${
            combinedArray.joinToString(separator = " ") { String.format("%02X", it) }
        }]"
        Timber.v(logMessage)

        return combinedArray
    }

    /** Android TV Remote v2 encadre chaque protobuf avec sa taille encodée en varint. */
    private fun writeVarint32(output: ByteArrayOutputStream, value: Int) {
        require(value >= 0) { "La longueur du message ne peut pas être négative." }
        var remaining = value
        while (true) {
            if ((remaining and 0x7F.inv()) == 0) {
                output.write(remaining)
                return
            }
            output.write((remaining and 0x7F) or 0x80)
            remaining = remaining ushr 7
        }
    }
}
