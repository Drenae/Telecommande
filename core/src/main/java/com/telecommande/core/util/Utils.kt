package com.telecommande.core.util

import timber.log.Timber
import java.security.cert.Certificate
import javax.net.ssl.SSLPeerUnverifiedException
import javax.net.ssl.SSLSession

object Utils {

    fun intBigEndianBytesToLong(input: ByteArray): Long {
        require(input.size == 4) { "Le tableau d'octets d'entrée doit contenir 4 octets." }
        return (input[0].toUByte().toLong() shl 24) or
                (input[1].toUByte().toLong() shl 16) or
                (input[2].toUByte().toLong() shl 8) or
                input[3].toUByte().toLong()
    }

    fun intToBigEndianIntBytes(value: Int): ByteArray {
        return byteArrayOf(
            (value shr 24).toByte(),
            (value shr 16).toByte(),
            (value shr 8).toByte(),
            value.toByte()
        )
    }

    fun getPeerCert(session: SSLSession): Certificate? {
        return try {
            val certs = session.peerCertificates
            if (certs.isNullOrEmpty()) {
                Timber.w("Aucun certificat pair trouvé pour la session : %s:%d", session.peerHost, session.peerPort)
                null
            } else {
                certs[0]
            }
        } catch (e: SSLPeerUnverifiedException) {
            Timber.w(e, "Pair non vérifié pour la session : %s:%d.", session.peerHost, session.peerPort)
            null
        } catch (e: Exception) {
            Timber.e(e, "Exception inattendue lors de la récupération du certificat pair pour la session : %s:%d.", session.peerHost, session.peerPort)
            null
        }
    }

    fun getLocalCert(session: SSLSession): Certificate? {
        return try {
            val certs = session.localCertificates
            if (certs.isNullOrEmpty()) {
                Timber.w("Aucun certificat local trouvé pour la session : %s:%d", session.peerHost, session.peerPort)
                null
            } else {
                certs[0]
            }
        } catch (e: Exception) {
            Timber.e(e, "Exception inattendue lors de la récupération du certificat local pour la session : %s:%d.", session.peerHost, session.peerPort)
            null
        }
    }

    fun bytesToHexString(bytes: ByteArray?): String {
        if (bytes == null || bytes.isEmpty()) {
            return ""
        }
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun hexStringToBytes(hexString: String?): ByteArray {
        require(!hexString.isNullOrEmpty()) { "La chaîne hexadécimale d'entrée ne peut être nulle ou vide." }
        require(hexString.length % 2 == 0) { "La chaîne hexadécimale d'entrée doit avoir un nombre pair de caractères." }

        return try {
            hexString.chunked(2)
                .map { it.toInt(16).toByte() }
                .toByteArray()
        } catch (e: NumberFormatException) {
            Timber.e(e, "Échec de la conversion de la chaîne hexadécimale en octets : '%s'", hexString)
            throw IllegalArgumentException("Chaîne hexadécimale d'entrée invalide : $hexString", e)
        }
    }
}