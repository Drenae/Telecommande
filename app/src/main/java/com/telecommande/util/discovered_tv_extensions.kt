package com.telecommande.util

import com.telecommande.core.discovery.DiscoveredTv

fun DiscoveredTv.getMacAddressFromAttributesOrNull(): String? {
    return attributes["bt"]
        ?.trim()
        ?.takeIf { value ->
            value.matches(Regex("(?i)^[0-9A-F]{2}(:[0-9A-F]{2}){5}$"))
        }
}
