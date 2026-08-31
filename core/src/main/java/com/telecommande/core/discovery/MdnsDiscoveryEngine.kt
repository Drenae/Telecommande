package com.telecommande.core.discovery

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.net.DatagramPacket
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.MulticastSocket
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap

/**
 * Découverte DNS-SD/mDNS directe, indépendante de NsdManager.
 *
 * Le moteur interroge _androidtvremote2._tcp.local. sur 224.0.0.251:5353 et reconstruit
 * les services à partir des enregistrements PTR/SRV/TXT/A/AAAA reçus.
 */
class MdnsDiscoveryEngine(
    private val scope: CoroutineScope,
    private val onFound: (DiscoveredTv) -> Unit,
    private val onLost: (DiscoveredTv) -> Unit
) {
    private var job: Job? = null
    private var socket: MulticastSocket? = null

    private val ptrInstances = ConcurrentHashMap.newKeySet<String>()
    private val srvRecords = ConcurrentHashMap<String, SrvRecord>()
    private val txtRecords = ConcurrentHashMap<String, Map<String, String>>()
    private val addresses = ConcurrentHashMap<String, InetAddress>()
    private val emitted = ConcurrentHashMap<String, DiscoveredTv>()

    fun start() {
        if (job?.isActive == true) return
        clearState(emitLost = false)

        job = scope.launch(Dispatchers.IO) {
            try {
                val multicastAddress = InetAddress.getByName(MDNS_ADDRESS)
                val multicastSocket = MulticastSocket(null).apply {
                    reuseAddress = true
                    bind(InetSocketAddress(MDNS_PORT))
                    soTimeout = 750
                    joinGroup(multicastAddress)
                }
                socket = multicastSocket

                Timber.i("mDNS direct démarré pour %s", SERVICE_FQDN)
                sendBrowseQuery(multicastSocket)

                var lastQueryAt = System.currentTimeMillis()
                val buffer = ByteArray(MAX_PACKET_SIZE)

                while (isActive) {
                    if (System.currentTimeMillis() - lastQueryAt >= QUERY_INTERVAL_MS) {
                        sendBrowseQuery(multicastSocket)
                        lastQueryAt = System.currentTimeMillis()
                    }

                    try {
                        val packet = DatagramPacket(buffer, buffer.size)
                        multicastSocket.receive(packet)
                        parsePacket(packet.data.copyOf(packet.length))
                    } catch (_: java.net.SocketTimeoutException) {
                        // Permet à la boucle de vérifier isActive et de renvoyer une requête périodique.
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Échec du moteur mDNS direct")
            } finally {
                closeSocket()
            }
        }
    }

    fun stop(emitLost: Boolean = true) {
        job?.cancel()
        job = null
        closeSocket()
        clearState(emitLost)
    }

    private fun closeSocket() {
        try {
            socket?.close()
        } catch (_: Exception) {
        } finally {
            socket = null
        }
    }

    private fun clearState(emitLost: Boolean) {
        if (emitLost) emitted.values.forEach(onLost)
        ptrInstances.clear()
        srvRecords.clear()
        txtRecords.clear()
        addresses.clear()
        emitted.clear()
    }

    private fun sendBrowseQuery(multicastSocket: MulticastSocket) {
        val query = buildQuery(SERVICE_FQDN, TYPE_PTR)
        val target = InetAddress.getByName(MDNS_ADDRESS)
        multicastSocket.send(DatagramPacket(query, query.size, target, MDNS_PORT))
        Timber.v("Requête mDNS envoyée pour %s", SERVICE_FQDN)
    }

    private fun parsePacket(data: ByteArray) {
        if (data.size < DNS_HEADER_SIZE) return

        try {
            val reader = DnsReader(data)
            reader.skip(4) // ID + flags
            val qdCount = reader.readU16()
            val anCount = reader.readU16()
            val nsCount = reader.readU16()
            val arCount = reader.readU16()

            repeat(qdCount) {
                reader.readName()
                reader.skip(4) // QTYPE + QCLASS
            }

            repeat(anCount + nsCount + arCount) {
                parseRecord(reader)
            }

            publishResolvableServices()
        } catch (e: Exception) {
            Timber.v(e, "Paquet mDNS ignoré car incomplet/invalide")
        }
    }

    private fun parseRecord(reader: DnsReader) {
        val name = normalize(reader.readName())
        val type = reader.readU16()
        reader.readU16() // class
        val ttl = reader.readU32()
        val length = reader.readU16()
        val start = reader.position

        when (type) {
            TYPE_PTR -> {
                val instance = normalize(reader.readName())
                if (name.equals(SERVICE_FQDN, ignoreCase = true)) {
                    if (ttl == 0L) {
                        ptrInstances.remove(instance)
                        emitted.remove(instance)?.let(onLost)
                    } else {
                        ptrInstances.add(instance)
                    }
                }
            }

            TYPE_SRV -> {
                if (length >= 6) {
                    reader.readU16() // priority
                    reader.readU16() // weight
                    val port = reader.readU16()
                    val target = normalize(reader.readName())
                    if (ttl == 0L) srvRecords.remove(name)
                    else srvRecords[name] = SrvRecord(target, port)
                }
            }

            TYPE_TXT -> {
                val end = start + length
                val attributes = linkedMapOf<String, String>()
                while (reader.position < end) {
                    val itemLength = reader.readU8()
                    if (itemLength == 0 || reader.position + itemLength > end) {
                        if (itemLength > 0) reader.skip((end - reader.position).coerceAtLeast(0))
                        break
                    }
                    val item = reader.readBytes(itemLength).toString(StandardCharsets.UTF_8)
                    val separator = item.indexOf('=')
                    if (separator >= 0) attributes[item.substring(0, separator)] = item.substring(separator + 1)
                    else if (item.isNotBlank()) attributes[item] = ""
                }
                if (ttl == 0L) txtRecords.remove(name) else txtRecords[name] = attributes
            }

            TYPE_A -> {
                if (length == 4) {
                    val address = InetAddress.getByAddress(reader.readBytes(4))
                    if (address is Inet4Address) {
                        if (ttl == 0L) addresses.remove(name) else addresses[name] = address
                    }
                }
            }

            TYPE_AAAA -> {
                if (length == 16) {
                    val address = InetAddress.getByAddress(reader.readBytes(16))
                    if (address is Inet6Address && !addresses.containsKey(name)) {
                        if (ttl == 0L) addresses.remove(name) else addresses[name] = address
                    }
                }
            }
        }

        reader.position = (start + length).coerceAtMost(reader.size)
    }

    private fun publishResolvableServices() {
        ptrInstances.toList().forEach { instance ->
            val srv = srvRecords[instance] ?: return@forEach
            val address = addresses[srv.target] ?: return@forEach
            val attributes = txtRecords[instance].orEmpty()
            val serviceName = instance.removeSuffix(".$SERVICE_FQDN").removeSuffix(".")
            val tv = DiscoveredTv(
                serviceName = serviceName,
                friendlyName = serviceName,
                hostAddress = address,
                port = srv.port,
                attributes = attributes
            )

            val previous = emitted[instance]
            if (previous != tv) {
                emitted[instance] = tv
                Timber.i(
                    "TV mDNS directe trouvée : nom=%s, ip=%s, port=%d, txt=%s",
                    tv.friendlyName,
                    tv.ipAddress,
                    tv.port,
                    tv.attributes
                )
                onFound(tv)
            }
        }
    }

    private fun buildQuery(name: String, type: Int): ByteArray {
        val output = ByteArrayOutputStream()
        repeat(2) { output.write(0) } // transaction ID
        repeat(2) { output.write(0) } // flags
        output.write(0); output.write(1) // QDCOUNT
        repeat(6) { output.write(0) } // ANCOUNT, NSCOUNT, ARCOUNT
        writeName(output, name)
        output.write((type shr 8) and 0xFF); output.write(type and 0xFF)
        output.write(0); output.write(1) // IN
        return output.toByteArray()
    }

    private fun writeName(output: ByteArrayOutputStream, name: String) {
        name.trimEnd('.').split('.').forEach { label ->
            val bytes = label.toByteArray(StandardCharsets.UTF_8)
            output.write(bytes.size)
            output.write(bytes)
        }
        output.write(0)
    }

    private fun normalize(name: String): String = name.trimEnd('.')

    private data class SrvRecord(val target: String, val port: Int)

    private class DnsReader(private val data: ByteArray) {
        var position: Int = 0
        val size: Int get() = data.size

        fun skip(count: Int) {
            position = (position + count).coerceAtMost(data.size)
        }

        fun readU8(): Int {
            require(position < data.size)
            return data[position++].toInt() and 0xFF
        }

        fun readU16(): Int = (readU8() shl 8) or readU8()

        fun readU32(): Long =
            ((readU8().toLong() shl 24) or (readU8().toLong() shl 16) or (readU8().toLong() shl 8) or readU8().toLong()) and 0xFFFFFFFFL

        fun readBytes(length: Int): ByteArray {
            require(length >= 0 && position + length <= data.size)
            return data.copyOfRange(position, position + length).also { position += length }
        }

        fun readName(): String {
            val labels = mutableListOf<String>()
            var cursor = position
            var jumped = false
            var guard = 0

            while (cursor < data.size && guard++ < 128) {
                val length = data[cursor].toInt() and 0xFF
                when {
                    length == 0 -> {
                        if (!jumped) position = cursor + 1
                        break
                    }
                    length and 0xC0 == 0xC0 -> {
                        require(cursor + 1 < data.size)
                        val pointer = ((length and 0x3F) shl 8) or (data[cursor + 1].toInt() and 0xFF)
                        if (!jumped) position = cursor + 2
                        cursor = pointer
                        jumped = true
                    }
                    else -> {
                        val start = cursor + 1
                        val end = start + length
                        require(end <= data.size)
                        labels += data.copyOfRange(start, end).toString(StandardCharsets.UTF_8)
                        cursor = end
                        if (!jumped) position = cursor
                    }
                }
            }
            return labels.joinToString(".")
        }
    }

    companion object {
        private const val MDNS_ADDRESS = "224.0.0.251"
        private const val MDNS_PORT = 5353
        private const val DNS_HEADER_SIZE = 12
        private const val MAX_PACKET_SIZE = 9000
        private const val QUERY_INTERVAL_MS = 1500L

        private const val TYPE_A = 1
        private const val TYPE_PTR = 12
        private const val TYPE_TXT = 16
        private const val TYPE_AAAA = 28
        private const val TYPE_SRV = 33

        const val SERVICE_FQDN = "_androidtvremote2._tcp.local"
    }
}
