package com.telecommande.domain.usecase.connection

import com.telecommande.data.repository.pairing.PairingRepository
import javax.inject.Inject

class ConnectToTvUseCase @Inject constructor(
    private val pairingRepository: PairingRepository
) {
    suspend operator fun invoke(hostAddress: String) {
        pairingRepository.connectForPairing(hostAddress)
    }
}