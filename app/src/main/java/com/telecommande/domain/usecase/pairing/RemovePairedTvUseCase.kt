package com.telecommande.domain.usecase.pairing

import com.telecommande.data.repository.SettingsRepository
import javax.inject.Inject

class RemovePairedTvUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(keystoreAlias: String) {
        settingsRepository.removePairedTvByKeystoreAlias(keystoreAlias)
    }
}