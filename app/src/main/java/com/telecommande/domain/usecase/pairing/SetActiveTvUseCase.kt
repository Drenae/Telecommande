package com.telecommande.domain.usecase.pairing

import com.telecommande.data.model.PairedTvInfo
import com.telecommande.data.repository.SettingsRepository
import javax.inject.Inject

class SetActiveTvUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(tvInfo: PairedTvInfo) {
        settingsRepository.saveActiveTvInfo(tvInfo)
    }
}