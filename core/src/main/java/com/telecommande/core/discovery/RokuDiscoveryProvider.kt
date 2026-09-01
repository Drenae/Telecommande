package com.telecommande.core.discovery

import com.telecommande.core.protocol.TvProtocolType
import kotlinx.coroutines.CoroutineScope
import timber.log.Timber
import java.net.InetAddress
import java.net.URI
import java.util.concurrent.ConcurrentHashMap

/** Découverte Roku officielle via SSDP ST: roku:ecp. */
class RokuDiscoveryProvider(
    scope: CoroutineScope,
    private val onFound: (DiscoveredTv) -> Unit,
    private val onLost: (DiscoveredTv) -> Unit
) : TvDiscoveryProvider {

    override val protocolType: TvProtocolType = TvProtocolType.ROKU_ECP
    override val name: String = "Roku ECP (SSDP)"

    private val found = ConcurrentHashMap<String, DiscoveredTv>()
    private val engine = SsdpDiscoveryEngine(
        scope = scope,
        searchTarget = SEARCH_TARGET,
        onResponse = ::handleResponse
    )

    override fun start() = engine.start()

    override fun stop() {
        engine.stop()
        found.values.toList().forEach(onLost)
        found.clear()
    }

    private fun handleResponse(response: SsdpResponse) {
        val st = response["st"] ?: return
        if (!st.equals(SEARCH_TARGET, ignoreCase = true)) return
        val location = response["location"] ?: return
        val uri = runCatching { URI(location) }.getOrNull() ?: return
        val host = uri.host?.takeIf { it.isNotBlank() } ?: response.sourceAddress
        val port = if (uri.port > 0) uri.port else DEFAULT_ECP_PORT
        val usn = response["usn"]?.takeIf { it.isNotBlank() } ?: "$host:$port"
        val address = runCatching { InetAddress.getByName(host) }.getOrNull() ?: return

        val tv = DiscoveredTv(
            serviceName = usn,
            friendlyName = "Roku",
            hostAddress = address,
            port = port,
            attributes = buildMap {
                put("location", location)
                put("st", st)
                put("usn", usn)
                response["server"]?.let { put("server", it) }
            },
            protocolType = protocolType
        )
        val previous = found.put(usn.lowercase(), tv)
        if (previous != tv) {
            Timber.i("Roku SSDP trouvé : ip=%s, port=%d, usn=%s", tv.ipAddress, tv.port, usn)
            onFound(tv)
        }
    }

    companion object {
        private const val SEARCH_TARGET = "roku:ecp"
        private const val DEFAULT_ECP_PORT = 8060
    }
}
