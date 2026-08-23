package com.telecommande.data.repository.pairing

import com.telecommande.data.repository.TvCoreEvent
import kotlinx.coroutines.flow.Flow

interface PairingRepository {
    val tvCoreEvents: Flow<TvCoreEvent>
    suspend fun connectForPairing(hostAddress: String)
    suspend fun sendSecret(pin: String)
    fun isKeystorePairedInitially(): Boolean
    suspend fun deleteKeystoreForReset(): Boolean
}