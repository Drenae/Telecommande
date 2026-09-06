package com.telecommande.core.wire

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.IOException
import java.io.InputStream

abstract class PacketParser(private val inputStream: InputStream) {

    companion object {
        private const val MAX_EXPECTED_PACKET_LENGTH = 8192
        private const val MAX_VARINT32_BYTES = 5
    }

    suspend fun parsePackets() {
        Timber.i("Démarrage de la boucle d'analyse des paquets sur %s", Thread.currentThread().name)

        withContext(Dispatchers.IO) {
            while (currentCoroutineContext().isActive) {
                try {
                    currentCoroutineContext().ensureActive()
                    val packetLength = readVarint32Length()

                    if (packetLength == null) {
                        Timber.i("Flux fermé lors de la lecture de la longueur du paquet. Arrêt.")
                        break
                    }

                    if (packetLength < 0 || packetLength > MAX_EXPECTED_PACKET_LENGTH) {
                        Timber.e(
                            "Erreur - Longueur du paquet %d hors limites (max=%d). Données probablement corrompues. Arrêt.",
                            packetLength,
                            MAX_EXPECTED_PACKET_LENGTH
                        )
                        break
                    }

                    if (packetLength == 0) {
                        messageBufferReceived(ByteArray(0))
                        continue
                    }

                    val buffer = ByteArray(packetLength)
                    var totalBytesReadForPacket = 0

                    while (totalBytesReadForPacket < packetLength && currentCoroutineContext().isActive) {
                        currentCoroutineContext().ensureActive()
                        val remainingBytes = packetLength - totalBytesReadForPacket
                        val bytesReadThisCycle = inputStream.read(buffer, totalBytesReadForPacket, remainingBytes)

                        if (bytesReadThisCycle < 0) {
                            Timber.e(
                                "Flux fermé inopinément lors de la lecture des données du paquet. Attendu %d octets, mais le flux s'est terminé après %d octets. Arrêt.",
                                packetLength,
                                totalBytesReadForPacket
                            )
                            throw IOException("Flux fermé inopinément lors de la lecture des données du paquet.")
                        }
                        totalBytesReadForPacket += bytesReadThisCycle
                    }

                    if (!currentCoroutineContext().isActive) continue

                    if (totalBytesReadForPacket == packetLength) {
                        messageBufferReceived(buffer)
                    } else {
                        Timber.w(
                            "Lecture du paquet terminée mais totalBytesReadForPacket (%d) != packetLength (%d).",
                            totalBytesReadForPacket,
                            packetLength
                        )
                    }
                } catch (e: IOException) {
                    if (currentCoroutineContext().isActive) {
                        Timber.e(e, "IOException dans la boucle d'analyse : %s", e.message)
                    } else {
                        Timber.i(
                            "IOException après demande d'annulation, probablement due à la fermeture du flux : %s",
                            e.message
                        )
                    }
                    break
                } catch (e: Exception) {
                    if (currentCoroutineContext().isActive) {
                        Timber.e(e, "Erreur inattendue %s dans la boucle d'analyse : %s", e.javaClass.simpleName, e.message)
                    }
                    break
                }
            }
        }
        Timber.i("Boucle d'analyse des paquets terminée.")
    }

    /** Android TV Remote v2 utilise un varint protobuf comme préfixe de longueur. */
    private fun readVarint32Length(): Int? {
        var result = 0
        var shift = 0

        repeat(MAX_VARINT32_BYTES) {
            val next = inputStream.read()
            if (next == -1) {
                if (shift == 0) return null
                throw IOException("Flux fermé au milieu du préfixe varint de longueur.")
            }

            result = result or ((next and 0x7F) shl shift)
            if ((next and 0x80) == 0) return result
            shift += 7
        }

        throw IOException("Préfixe varint de longueur invalide (plus de $MAX_VARINT32_BYTES octets).")
    }

    protected abstract suspend fun messageBufferReceived(buf: ByteArray)
}
