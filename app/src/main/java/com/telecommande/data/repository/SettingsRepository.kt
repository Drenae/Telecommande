package com.telecommande.data.repository

import com.telecommande.data.model.PairedTvInfo
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val activeTvInfoFlow: Flow<PairedTvInfo?>
    suspend fun saveActiveTvInfo(tvInfo: PairedTvInfo?)
    suspend fun getActiveTvInfo(): PairedTvInfo?
    suspend fun clearActiveTvInfo()

    val pairedTvsFlow: Flow<List<PairedTvInfo>>
    suspend fun addPairedTv(tvInfo: PairedTvInfo)
    suspend fun removePairedTvByKeystoreAlias(keystoreAlias: String)
    suspend fun removePairedTvByIpAddress(tvIpAddress: String)
    suspend fun getPairedTvByKeystoreAlias(keystoreAlias: String): PairedTvInfo?
    suspend fun getPairedTvByIpAddress(tvIpAddress: String): PairedTvInfo?
    suspend fun getAllPairedTvs(): List<PairedTvInfo>
    suspend fun clearAllPairedTvs()
}