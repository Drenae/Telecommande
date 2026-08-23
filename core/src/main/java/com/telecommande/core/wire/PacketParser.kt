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
    }

    suspend fun parsePackets() {
        Timber.i("Démarrage de la boucle d'analyse des paquets sur %s", Thread.currentThread().name)
        var packetLength: Int
        var totalBytesReadForPacket: Int

        withContext(Dispatchers.IO) {
            while (currentCoroutineContext().isActive) {
                try {
                    currentCoroutineContext().ensureActive()
                    packetLength = inputStream.read()

                    if (packetLength == -1) {
                        Timber.i("Flux fermé (retour -1) lors de la lecture de la longueur du paquet. Arrêt.")
                        break
                    }

                    if (packetLength > MAX_EXPECTED_PACKET_LENGTH) {
                        Timber.e("Erreur - Longueur du paquet %d dépasse MAX_EXPECTED_PACKET_LENGTH %d. Données probablement corrompues. Arrêt.", packetLength, MAX_EXPECTED_PACKET_LENGTH)
                        break
                    }

                    if (packetLength == 0) {
                        Timber.d("Longueur de paquet reçue 0. Traitement comme message vide.")
                        messageBufferReceived(ByteArray(0))
                        continue
                    }

                    Timber.v("Attente d'un paquet de taille : %d", packetLength)
                    val buffer = ByteArray(packetLength)
                    totalBytesReadForPacket = 0

                    while (totalBytesReadForPacket < packetLength && currentCoroutineContext().isActive) {
                        currentCoroutineContext().ensureActive()
                        val remainingBytes = packetLength - totalBytesReadForPacket
                        val bytesReadThisCycle = inputStream.read(buffer, totalBytesReadForPacket, remainingBytes)

                        if (bytesReadThisCycle < 0) {
                            Timber.e("Flux fermé inopinément lors de la lecture des données du paquet. Attendu %d octets, mais le flux s'est terminé après %d octets. Arrêt.", packetLength, totalBytesReadForPacket)
                            throw IOException("Flux fermé inopinément lors de la lecture des données du paquet.")
                        }
                        totalBytesReadForPacket += bytesReadThisCycle
                        Timber.v("%d octets lus ce cycle, total lu %d/%d", bytesReadThisCycle, totalBytesReadForPacket, packetLength)
                    }

                    if (!currentCoroutineContext().isActive) {
                        Timber.i("Annulation demandée pendant la lecture du corps du paquet. Rejet du paquet partiel.")
                        continue
                    }

                    if (totalBytesReadForPacket == packetLength) {
                        messageBufferReceived(buffer)
                    } else {
                        Timber.w("Lecture du paquet terminée mais totalBytesReadForPacket (%d) != packetLength (%d). Cela peut indiquer un problème si non annulé.", totalBytesReadForPacket, packetLength)
                    }

                } catch (e: IOException) {
                    if (currentCoroutineContext().isActive) {
                        Timber.e(e, "IOException dans la boucle d'analyse : %s", e.message)
                    } else {
                        Timber.i("IOException après demande d'annulation, probablement due à la fermeture du flux : %s", e.message)
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

    protected abstract suspend fun messageBufferReceived(buf: ByteArray)
}