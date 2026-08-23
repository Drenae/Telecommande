package com.telecommande.domain.usecase.discovery

import com.telecommande.data.repository.discovery.DiscoveryRepository
import javax.inject.Inject

class StopTvDiscoveryUseCase @Inject constructor(
    private val discoveryRepository: DiscoveryRepository
) {
    operator fun invoke() {
        discoveryRepository.stopTvDiscovery()
    }
}