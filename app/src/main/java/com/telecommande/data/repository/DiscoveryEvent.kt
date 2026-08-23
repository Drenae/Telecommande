package com.telecommande.data.repository

import com.telecommande.core.discovery.DiscoveredTv

sealed class DiscoveryEvent {
    data object DiscoveryStarted : DiscoveryEvent()
    data object DiscoveryStopped : DiscoveryEvent()
    data class TvFound(val tv: DiscoveredTv) : DiscoveryEvent()
    data class TvLost(val tv: DiscoveredTv) : DiscoveryEvent()
    data class Error(val message: String, val errorCode: Int) : DiscoveryEvent()
}