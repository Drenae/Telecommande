package com.telecommande.domain.usecase.pairing

import com.telecommande.data.model.PairedTvInfo
import com.telecommande.data.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetActiveTvUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(): Flow<PairedTvInfo?> {
        return settingsRepository.activeTvInfoFlow
    }

    suspend fun immediate(): PairedTvInfo? {
        return settingsRepository.getActiveTvInfo()
    }
}

