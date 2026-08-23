package com.telecommande.data.repository.discovery

import com.telecommande.core.discovery.TvDiscoveryManager
import com.telecommande.data.repository.DiscoveryEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import com.telecommande.core.event.DiscoveryEvent as CoreDiscoveryEvent

class DiscoveryRepositoryImpl @Inject constructor(
    private val tvDiscoveryManager: TvDiscoveryManager
) : DiscoveryRepository {

    override val discoveryEvents: Flow<DiscoveryEvent> =
        tvDiscoveryManager.eventFlow.map { coreDiscoveryEvent ->
            Timber.d("Mapping CoreDiscoveryEvent: %s", coreDiscoveryEvent)
            when (coreDiscoveryEvent) {
                is CoreDiscoveryEvent.DiscoveryStarted -> DiscoveryEvent.DiscoveryStarted
                is CoreDiscoveryEvent.DiscoveryStopped -> DiscoveryEvent.DiscoveryStopped
                is CoreDiscoveryEvent.TvFound -> DiscoveryEvent.TvFound(coreDiscoveryEvent.tv)
                is CoreDiscoveryEvent.TvLost -> DiscoveryEvent.TvLost(coreDiscoveryEvent.tv)
                is CoreDiscoveryEvent.Error -> DiscoveryEvent.Error(
                    coreDiscoveryEvent.message,
                    coreDiscoveryEvent.errorCode
                )
            }
        }

    override fun startTvDiscovery() {
        Timber.d("Appel de tvDiscoveryManager.startDiscovery()")
        tvDiscoveryManager.startDiscovery()
    }

    override fun stopTvDiscovery() {
        Timber.d("Appel de tvDiscoveryManager.stopDiscovery()")
        tvDiscoveryManager.stopDiscovery()
    }
}