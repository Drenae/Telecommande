package com.telecommande.domain.usecase.pairing

import com.telecommande.data.repository.SettingsRepository
import javax.inject.Inject

class ClearActiveTvUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke() {
        settingsRepository.clearActiveTvInfo()
    }
}