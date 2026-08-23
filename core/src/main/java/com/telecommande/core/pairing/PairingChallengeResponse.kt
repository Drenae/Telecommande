package com.telecommande.core.pairing

import com.telecommande.core.exception.PairingException
import com.telecommande.core.util.Utils
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.PublicKey
import java.security.cert.Certificate
import java.security.interfaces.RSAPublicKey
import java.util.Arrays

internal class PairingChallengeResponse(
    private val mClientCertificate: Certificate,
    private val mServerCertificate: Certificate
) {
    companion object {
        private const val HASH_ALGORITHM = "SHA-256"
    }

    interface DebugLogger {
        fun debug(message: String)
        fun verbose(message: String)
    }

    private fun logDebug(message: String) {
        println(message)
    }

    private fun logVerbose(message: String) {
        println(message)
    }

    @Throws(PairingException::class)
    fun getAlpha(nonce: ByteArray): ByteArray {
        val clientPubKey: PublicKey = mClientCertificate.publicKey
        val serverPubKey: PublicKey = mServerCertificate.publicKey

        logDebug("getAlpha, nonce=" + Utils.bytesToHexString(nonce))

        if (clientPubKey !is RSAPublicKey || serverPubKey !is RSAPublicKey) {
            throw PairingException("Only supports RSA public keys")
        }

        val digest: MessageDigest = try {
            MessageDigest.getInstance(HASH_ALGORITHM)
        } catch (e: NoSuchAlgorithmException) {
            throw PairingException("Could not get digest algorithm", e)
        }

        var clientModulus = clientPubKey.modulus.abs().toByteArray()
        var clientExponent = clientPubKey.publicExponent.abs().toByteArray()
        var serverModulus = serverPubKey.modulus.abs().toByteArray()
        var serverExponent = serverPubKey.publicExponent.abs().toByteArray()

        clientModulus = removeLeadingNullBytes(clientModulus)
        clientExponent = removeLeadingNullBytes(clientExponent)
        serverModulus = removeLeadingNullBytes(serverModulus)
        serverExponent = removeLeadingNullBytes(serverExponent)

        logVerbose("Hash inputs, in order: ")
        logVerbose("   client modulus: " + Utils.bytesToHexString(clientModulus))
        logVerbose("  client exponent: " + Utils.bytesToHexString(clientExponent))
        logVerbose("   server modulus: " + Utils.bytesToHexString(serverModulus))
        logVerbose("  server exponent: " + Utils.bytesToHexString(serverExponent))
        logVerbose("            nonce: " + Utils.bytesToHexString(nonce))

        digest.update(clientModulus)
        digest.update(clientExponent)
        digest.update(serverModulus)
        digest.update(serverExponent)
        digest.update(nonce)

        val digestBytesResult = digest.digest()
        logDebug("Generated hash: " + Utils.bytesToHexString(digestBytesResult))
        return digestBytesResult
    }

    @Throws(PairingException::class)
    fun getGamma(nonce: ByteArray): ByteArray {
        val alphaBytes = getAlpha(nonce)
        check(alphaBytes.size >= nonce.size) { "alphaBytes length must be >= nonce length" }

        val result = ByteArray(nonce.size * 2)
        System.arraycopy(alphaBytes, 0, result, 0, nonce.size)
        System.arraycopy(nonce, 0, result, nonce.size, nonce.size)

        return result
    }

    fun extractNonce(gamma: ByteArray): ByteArray {
        require(gamma.size >= 2 && gamma.size % 2 == 0) {
            "Gamma length must be >= 2 and even. Received length: ${gamma.size}"
        }
        val nonceLength = gamma.size / 2
        val nonce = ByteArray(nonceLength)
        System.arraycopy(gamma, nonceLength, nonce, 0, nonceLength)
        return nonce
    }

    @Throws(PairingException::class)
    fun checkGamma(gamma: ByteArray): Boolean {
        val nonce: ByteArray = try {
            extractNonce(gamma)
        } catch (e: IllegalArgumentException) {
            logDebug("Illegal nonce value (due to gamma format): ${e.message}")
            return false
        }
        logDebug("Nonce is: " + Utils.bytesToHexString(nonce))
        logDebug("User gamma is: " + Utils.bytesToHexString(gamma))
        val generatedGamma = getGamma(nonce)
        logDebug("Generated gamma is: " + Utils.bytesToHexString(generatedGamma))
        return Arrays.equals(gamma, generatedGamma)
    }

    private fun removeLeadingNullBytes(inArray: ByteArray): ByteArray {
        var offset = 0
        while (offset < inArray.size && inArray[offset].toInt() == 0) {
            offset += 1
        }
        return Arrays.copyOfRange(inArray, offset, inArray.size)
    }
}