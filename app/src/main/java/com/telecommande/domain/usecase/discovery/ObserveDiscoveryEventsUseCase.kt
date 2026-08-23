package com.telecommande.domain.usecase.discovery

import com.telecommande.data.repository.DiscoveryEvent
import com.telecommande.data.repository.discovery.DiscoveryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveDiscoveryEventsUseCase @Inject constructor(
    private val discoveryRepository: DiscoveryRepository
) {
    operator fun invoke(): Flow<DiscoveryEvent> {
        return discoveryRepository.discoveryEvents
    }
}