package com.telecommande.data.repository.remote

import com.telecommande.core.remote.Remotemessage
import com.telecommande.data.repository.TvCoreEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface RemoteRepository {
    val tvCoreEvents: Flow<TvCoreEvent>
    val isConnected: StateFlow<Boolean>

    suspend fun disconnectFromTv()

    suspend fun sendCommand(
        keyCode: Remotemessage.RemoteKeyCode,
        action: Remotemessage.RemoteDirection = Remotemessage.RemoteDirection.SHORT
    )

    fun launchApplication(appLink: String)
}
