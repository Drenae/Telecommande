package com.telecommande.core.discovery

import android.content.Context
import android.net.wifi.WifiManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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
import java.net.NetworkInterface
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap

/** Découverte DNS-SD/mDNS directe, indépendante de NsdManager. */
class MdnsDiscoveryEngine(
    context: Context,
    private val scope: CoroutineScope,
    private val onFound: (DiscoveredTv) -> Unit,
    private val onLost: (DiscoveredTv) -> Unit
) {
    private val appContext = context.applicationContext
    private var job: Job? = null
    private var socket: MulticastSocket? = null

    private val ptrInstances = ConcurrentHashMap.newKeySet<String>()
    private val srvRecords = ConcurrentHashMap<String, SrvRecord>()
    private val txtRecords = ConcurrentHashMap<String, Map<String, String>>()
    private val addresses = ConcurrentHashMap<String, InetAddress>()
    private val emitted = ConcurrentHashMap<String, DiscoveredTv>()

    fun start() {
        if (job?.isActive == true) return
        clearState(false)
        job = scope.launch(Dispatchers.IO) {
            try {
                val multicastAddress = InetAddress.getByName(MDNS_ADDRESS)
                val networkInterface = findWifiNetworkInterface()
                val multicastSocket = MulticastSocket(null).apply {
                    reuseAddress = true
                    bind(InetSocketAddress(MDNS_PORT))
                    soTimeout = 750
                    if (networkInterface != null) {
                        this.networkInterface = networkInterface
                        joinGroup(InetSocketAddress(multicastAddress, MDNS_PORT), networkInterface)
                        Timber.i("mDNS lié à l'interface Wi-Fi %s", networkInterface.name)
                    } else {
                        @Suppress("DEPRECATION")
                        joinGroup(multicastAddress)
                        Timber.w("Interface Wi-Fi introuvable : fallback mDNS sur interface système")
                    }
                }
                socket = multicastSocket
                Timber.i("mDNS direct démarré pour %s", SERVICE_FQDN)
                sendQueries(multicastSocket)
                var lastQueryAt = System.currentTimeMillis()
                val buffer = ByteArray(MAX_PACKET_SIZE)
                while (isActive) {
                    if (System.currentTimeMillis() - lastQueryAt >= QUERY_INTERVAL_MS) {
                        sendQueries(multicastSocket)
                        lastQueryAt = System.currentTimeMillis()
                    }
                    try {
                        val packet = DatagramPacket(buffer, buffer.size)
                        multicastSocket.receive(packet)
                        parsePacket(packet.data.copyOf(packet.length))
                    } catch (_: java.net.SocketTimeoutException) {}
                }
            } catch (e: Exception) {
                Timber.e(e, "Échec du moteur mDNS direct")
            } finally { closeSocket() }
        }
    }

    fun stop(emitLost: Boolean = true) {
        job?.cancel(); job = null; closeSocket(); clearState(emitLost)
    }

    private fun findWifiNetworkInterface(): NetworkInterface? {
        return try {
            val wifi = appContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            @Suppress("DEPRECATION")
            val ip = wifi?.connectionInfo?.ipAddress ?: 0
            if (ip != 0) {
                val bytes = byteArrayOf((ip and 0xff).toByte(), (ip shr 8 and 0xff).toByte(), (ip shr 16 and 0xff).toByte(), (ip shr 24 and 0xff).toByte())
                NetworkInterface.getByInetAddress(InetAddress.getByAddress(bytes))
            } else null
        } catch (e: Exception) {
            Timber.w(e, "Impossible de déterminer l'interface Wi-Fi")
            null
        }
    }

    private fun closeSocket() { try { socket?.close() } catch (_: Exception) {} finally { socket = null } }
    private fun clearState(emitLost: Boolean) {
        if (emitLost) emitted.values.forEach(onLost)
        ptrInstances.clear(); srvRecords.clear(); txtRecords.clear(); addresses.clear(); emitted.clear()
    }

    private fun sendQueries(s: MulticastSocket) {
        sendQuery(s, SERVICE_FQDN, TYPE_PTR)
        ptrInstances.forEach { sendQuery(s, it, TYPE_ANY) }
    }

    private fun sendQuery(s: MulticastSocket, name: String, type: Int) {
        val query = buildQuery(name, type)
        s.send(DatagramPacket(query, query.size, InetAddress.getByName(MDNS_ADDRESS), MDNS_PORT))
    }

    private fun parsePacket(data: ByteArray) {
        if (data.size < DNS_HEADER_SIZE) return
        try {
            val r = DnsReader(data); r.skip(4)
            val qd = r.readU16(); val an = r.readU16(); val ns = r.readU16(); val ar = r.readU16()
            repeat(qd) { r.readName(); r.skip(4) }
            repeat(an + ns + ar) { parseRecord(r) }
            publishResolvableServices()
        } catch (e: Exception) { Timber.v(e, "Paquet mDNS ignoré car incomplet/invalide") }
    }

    private fun parseRecord(r: DnsReader) {
        val name = normalize(r.readName()); val type = r.readU16(); r.readU16(); val ttl = r.readU32(); val length = r.readU16(); val start = r.position
        when (type) {
            TYPE_PTR -> {
                val instance = normalize(r.readName())
                if (name.equals(SERVICE_FQDN, true)) {
                    if (ttl == 0L) { ptrInstances.remove(instance); emitted.remove(instance)?.let(onLost) } else ptrInstances.add(instance)
                }
            }
            TYPE_SRV -> if (length >= 6) { r.readU16(); r.readU16(); val port = r.readU16(); val target = normalize(r.readName()); if (ttl == 0L) srvRecords.remove(name) else srvRecords[name] = SrvRecord(target, port) }
            TYPE_TXT -> {
                val end = start + length; val attrs = linkedMapOf<String,String>()
                while (r.position < end) { val n = r.readU8(); if (n == 0 || r.position + n > end) break; val item = r.readBytes(n).toString(StandardCharsets.UTF_8); val p = item.indexOf('='); if (p >= 0) attrs[item.substring(0,p)] = item.substring(p+1) else if (item.isNotBlank()) attrs[item] = "" }
                if (ttl == 0L) txtRecords.remove(name) else txtRecords[name] = attrs
            }
            TYPE_A -> if (length == 4) { val a = InetAddress.getByAddress(r.readBytes(4)); if (a is Inet4Address) { if (ttl == 0L) addresses.remove(name) else addresses[name] = a } }
            TYPE_AAAA -> if (length == 16) { val a = InetAddress.getByAddress(r.readBytes(16)); if (a is Inet6Address && !addresses.containsKey(name)) { if (ttl == 0L) addresses.remove(name) else addresses[name] = a } }
        }
        r.position = (start + length).coerceAtMost(r.size)
    }

    private fun publishResolvableServices() {
        ptrInstances.toList().forEach { instance ->
            val srv = srvRecords[instance] ?: return@forEach
            val address = addresses[srv.target] ?: return@forEach
            val attrs = txtRecords[instance].orEmpty()
            val serviceName = instance.removeSuffix(".$SERVICE_FQDN").removeSuffix(".")
            val tv = DiscoveredTv(serviceName, attrs["fn"]?.takeIf { it.isNotBlank() } ?: serviceName, address, srv.port, attrs)
            if (emitted[instance] != tv) {
                emitted[instance] = tv
                Timber.i("TV mDNS directe trouvée : nom=%s, ip=%s, port=%d, txt=%s", tv.friendlyName, tv.ipAddress, tv.port, tv.attributes)
                onFound(tv)
            }
        }
    }

    private fun buildQuery(name: String, type: Int): ByteArray {
        val o = ByteArrayOutputStream(); repeat(4) { o.write(0) }; o.write(0); o.write(1); repeat(6) { o.write(0) }; writeName(o,name); o.write(type shr 8 and 255); o.write(type and 255); o.write(0); o.write(1); return o.toByteArray()
    }
    private fun writeName(o: ByteArrayOutputStream, name: String) { name.trimEnd('.').split('.').forEach { val b=it.toByteArray(StandardCharsets.UTF_8); o.write(b.size); o.write(b) }; o.write(0) }
    private fun normalize(name: String)=name.trimEnd('.')
    private data class SrvRecord(val target:String,val port:Int)

    private class DnsReader(private val data:ByteArray) {
        var position=0; val size get()=data.size
        fun skip(n:Int){ position=(position+n).coerceAtMost(data.size) }
        fun readU8():Int { require(position<data.size); return data[position++].toInt() and 255 }
        fun readU16()=(readU8() shl 8) or readU8()
        fun readU32():Long=((readU8().toLong() shl 24) or (readU8().toLong() shl 16) or (readU8().toLong() shl 8) or readU8().toLong()) and 0xffffffffL
        fun readBytes(n:Int):ByteArray { require(n>=0 && position+n<=data.size); return data.copyOfRange(position,position+n).also{position+=n} }
        fun readName():String { val labels=mutableListOf<String>(); var cursor=position; var jumped=false; var guard=0; while(cursor<data.size && guard++<128){ val len=data[cursor].toInt() and 255; when { len==0->{if(!jumped)position=cursor+1;break}; len and 0xc0==0xc0->{require(cursor+1<data.size);val p=((len and 0x3f) shl 8) or (data[cursor+1].toInt() and 255);if(!jumped)position=cursor+2;cursor=p;jumped=true}; else->{val st=cursor+1;val en=st+len;require(en<=data.size);labels+=data.copyOfRange(st,en).toString(StandardCharsets.UTF_8);cursor=en;if(!jumped)position=cursor} } }; return labels.joinToString(".") }
    }
    companion object {
        private const val MDNS_ADDRESS="224.0.0.251"
        private const val MDNS_PORT=5353
        private const val DNS_HEADER_SIZE=12
        private const val MAX_PACKET_SIZE=9000
        private const val QUERY_INTERVAL_MS=5000L
        private const val TYPE_A=1
        private const val TYPE_PTR=12
        private const val TYPE_TXT=16
        private const val TYPE_AAAA=28
        private const val TYPE_SRV=33
        private const val TYPE_ANY=255
        const val SERVICE_FQDN="_androidtvremote2._tcp.local"
    }
}
