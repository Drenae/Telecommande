package com.telecommande.data.repository.remote

import com.telecommande.core.protocol.TvCommand
import com.telecommande.core.protocol.TvProtocolType
import com.telecommande.data.repository.TvCoreEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface RemoteRepository {
    val tvCoreEvents: Flow<TvCoreEvent>
    val isConnected: StateFlow<Boolean>

    suspend fun connectToTv(
        hostAddress: String,
        credentialId: String?,
        protocolType: TvProtocolType
    )
    suspend fun disconnectFromTv()
    suspend fun sendCommand(command: TvCommand)
    fun launchApplication(appLink: String)
}
