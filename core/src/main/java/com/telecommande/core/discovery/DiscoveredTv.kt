package com.telecommande.core.discovery

import java.net.InetAddress

data class DiscoveredTv(
    val serviceName: String,
    val friendlyName: String,
    val hostAddress: InetAddress?,
    val port: Int,
    val attributes: Map<String, String> = emptyMap()
) {
    val ipAddress: String?
        get() = hostAddress?.hostAddress

    override fun toString(): String {
        return "DiscoveredTv(friendlyName='$friendlyName', serviceName='$serviceName', ipAddress='${ipAddress ?: "N/A"}', port=$port, attributes=$attributes)"
    }
}
