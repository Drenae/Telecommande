package com.telecommande.core.discovery

import com.telecommande.core.protocol.TvProtocolType
import java.net.InetAddress

data class DiscoveredTv(
    val serviceName: String,
    val friendlyName: String,
    val hostAddress: InetAddress?,
    val port: Int,
    val attributes: Map<String, String> = emptyMap(),
    val protocolType: TvProtocolType = TvProtocolType.ANDROID_TV_REMOTE_V2
) {
    val ipAddress: String?
        get() = hostAddress?.hostAddress

    override fun toString(): String {
        return "DiscoveredTv(friendlyName='$friendlyName', serviceName='$serviceName', ipAddress='${ipAddress ?: "N/A"}', port=$port, protocolType=$protocolType, attributes=$attributes)"
    }
}
