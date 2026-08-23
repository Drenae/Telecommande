package com.telecommande.domain.usecase.pairing

import com.telecommande.data.repository.pairing.PairingRepository
import javax.inject.Inject

class SubmitPinUseCase @Inject constructor(
    private val pairingRepository: PairingRepository
) {
    suspend operator fun invoke(pin: String) {
        pairingRepository.sendSecret(pin)
    }
}