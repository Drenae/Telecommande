package com.telecommande.domain.usecase.pairing

import com.telecommande.data.model.PairedTvInfo
import com.telecommande.data.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPairedTvsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(): Flow<List<PairedTvInfo>> {
        return settingsRepository.pairedTvsFlow
    }

    suspend fun immediate(): List<PairedTvInfo> {
        return settingsRepository.getAllPairedTvs()
    }
}