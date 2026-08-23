package com.telecommande.core.event

import com.telecommande.core.discovery.DiscoveredTv

sealed class DiscoveryEvent {
    data object DiscoveryStarted : DiscoveryEvent()
    data object DiscoveryStopped : DiscoveryEvent()
    data class TvFound(val tv: DiscoveredTv) : DiscoveryEvent()
    data class TvLost(val tv: DiscoveredTv) : DiscoveryEvent()
    data class Error(val message: String, val errorCode: Int = 0) : DiscoveryEvent()
}
