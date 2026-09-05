package com.telecommande.util

import com.telecommande.core.discovery.DiscoveredTv
import com.telecommande.data.model.PairedTvInfo

fun DiscoveredTv.getMacAddressFromAttributesOrNull(): String? {
    return attributes["bt"]
        ?.trim()
        ?.takeIf { value ->
            value.matches(Regex("(?i)^[0-9A-F]{2}(:[0-9A-F]{2}){5}$"))
        }
}

fun DiscoveredTv.findMatchingPairedTv(
    pairedTvs: List<PairedTvInfo>
): PairedTvInfo? {
    val discoveredMac = getMacAddressFromAttributesOrNull()

    return pairedTvs.find { paired ->
        when {
            discoveredMac != null && paired.macAddress != null ->
                paired.macAddress.equals(discoveredMac, ignoreCase = true)

            paired.name != null ->
                paired.name == friendlyName || paired.name == serviceName

            else -> false
        }
    }
}

fun PairedTvInfo.resolveDisplayName(
    displayNames: Map<String, String>
): String {
    return displayNames[keystoreAlias] ?: name ?: ipAddress
}

fun PairedTvInfo.technicalName(): String {
    return name ?: ipAddress
}
