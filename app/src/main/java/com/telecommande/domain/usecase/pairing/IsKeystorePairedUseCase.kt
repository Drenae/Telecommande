package com.telecommande.domain.usecase.pairing

import com.telecommande.data.repository.pairing.PairingRepository
import javax.inject.Inject

class IsKeystorePairedUseCase @Inject constructor(
    private val pairingRepository: PairingRepository
) {
    operator fun invoke(): Boolean {
        return pairingRepository.isKeystorePairedInitially()
    }
}