package com.telecommande.domain.usecase.pairing

import com.telecommande.data.repository.TvCoreEvent
import com.telecommande.data.repository.pairing.PairingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObservePairingEventsUseCase @Inject constructor(
    private val pairingRepository: PairingRepository
) {
    operator fun invoke(): Flow<TvCoreEvent> {
        return pairingRepository.tvCoreEvents
    }
}