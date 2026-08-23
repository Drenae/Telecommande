package com.telecommande.data.repository

import com.telecommande.data.model.PairedTvInfo
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val activeTvInfoFlow: Flow<PairedTvInfo?>
    val pairedTvsFlow: Flow<List<PairedTvInfo>>

    suspend fun saveActiveTvInfo(tvInfo: PairedTvInfo?)
    suspend fun getActiveTvInfo(): PairedTvInfo?
    suspend fun clearActiveTvInfo()

    suspend fun addPairedTv(tvInfo: PairedTvInfo)
    suspend fun removePairedTvByKeystoreAlias(keystoreAlias: String)
    suspend fun getPairedTvByKeystoreAlias(keystoreAlias: String): PairedTvInfo?
}
