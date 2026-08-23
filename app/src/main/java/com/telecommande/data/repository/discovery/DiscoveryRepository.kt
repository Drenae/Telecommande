package com.telecommande.data.repository.discovery

import com.telecommande.data.repository.DiscoveryEvent
import kotlinx.coroutines.flow.Flow

interface DiscoveryRepository {
    val discoveryEvents: Flow<DiscoveryEvent>
    fun startTvDiscovery()
    fun stopTvDiscovery()
}