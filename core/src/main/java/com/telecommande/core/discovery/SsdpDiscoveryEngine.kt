package com.telecommande.core.discovery

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.net.SocketTimeoutException
import java.nio.charset.StandardCharsets
import java.util.Locale

/** Réponse SSDP normalisée pour les providers de découverte. */
data class SsdpResponse(
    val sourceAddress: String,
    val statusLine: String,
    val headers: Map<String, String>
) {
    operator fun get(header: String): String? = headers[header.lowercase(Locale.ROOT)]
}

/**
 * Moteur SSDP M-SEARCH générique.
 *
 * Chaque provider choisit son Search Target (ST) puis transforme les réponses
 * en [DiscoveredTv]. Le moteur ne contient aucune logique propre à une marque.
 */
class SsdpDiscoveryEngine(
    private val scope: CoroutineScope,
    private val searchTarget: String,
    private val onResponse: (SsdpResponse) -> Unit
) {
    @Volatile
    private var running = false
    private var job: Job? = null
    private var socket: DatagramSocket? = null

    fun start() {
        if (running) return
        running = true
        job = scope.launch {
            try {
                DatagramSocket(null).use { udp ->
                    socket = udp
                    udp.reuseAddress = true
                    udp.bind(InetSocketAddress(0))
                    udp.soTimeout = RECEIVE_TIMEOUT_MS
                    Timber.i("SSDP démarré : ST=%s", searchTarget)

                    while (isActive && running) {
                        sendSearch(udp)
                        receiveWindow(udp)
                        delay(SEARCH_INTERVAL_MS)
                    }
                }
            } catch (e: Exception) {
                if (running) Timber.e(e, "Erreur SSDP pour ST=%s", searchTarget)
            } finally {
                socket = null
                running = false
            }
        }
    }

    fun stop() {
        running = false
        socket?.close()
        socket = null
        job?.cancel()
        job = null
    }

    private fun sendSearch(udp: DatagramSocket) {
        val payload = buildString {
            append("M-SEARCH * HTTP/1.1\r\n")
            append("HOST: $SSDP_HOST:$SSDP_PORT\r\n")
            append("MAN: \"ssdp:discover\"\r\n")
            append("MX: 2\r\n")
            append("ST: $searchTarget\r\n")
            append("\r\n")
        }.toByteArray(StandardCharsets.UTF_8)
        udp.send(DatagramPacket(payload, payload.size, InetSocketAddress(SSDP_HOST, SSDP_PORT)))
    }

    private fun receiveWindow(udp: DatagramSocket) {
        val deadline = System.currentTimeMillis() + RESPONSE_WINDOW_MS
        while (running && System.currentTimeMillis() < deadline) {
            val buffer = ByteArray(MAX_PACKET_SIZE)
            val packet = DatagramPacket(buffer, buffer.size)
            try {
                udp.receive(packet)
                parseResponse(packet)?.let(onResponse)
            } catch (_: SocketTimeoutException) {
                // Continue jusqu'à la fin de la fenêtre de réponses.
            }
        }
    }

    private fun parseResponse(packet: DatagramPacket): SsdpResponse? {
        val raw = String(packet.data, packet.offset, packet.length, StandardCharsets.UTF_8)
        val lines = raw.split("\r\n", "\n")
        val statusLine = lines.firstOrNull()?.trim().orEmpty()
        if (statusLine.isBlank()) return null

        val headers = linkedMapOf<String, String>()
        lines.drop(1).forEach { line ->
            val separator = line.indexOf(':')
            if (separator <= 0) return@forEach
            val key = line.substring(0, separator).trim().lowercase(Locale.ROOT)
            val value = line.substring(separator + 1).trim()
            if (key.isNotBlank() && value.isNotBlank()) headers[key] = value
        }
        return SsdpResponse(packet.address.hostAddress.orEmpty(), statusLine, headers)
    }

    companion object {
        private const val SSDP_HOST = "239.255.255.250"
        private const val SSDP_PORT = 1900
        private const val RECEIVE_TIMEOUT_MS = 500
        private const val RESPONSE_WINDOW_MS = 2500L
        private const val SEARCH_INTERVAL_MS = 7500L
        private const val MAX_PACKET_SIZE = 8192
    }
}
