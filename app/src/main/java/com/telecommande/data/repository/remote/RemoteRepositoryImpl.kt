package com.telecommande.data.repository.remote

import com.telecommande.core.AndroidRemoteTv
import com.telecommande.core.event.AndroidTvEvent as CoreAndroidTvEvent
import com.telecommande.core.remote.Remotemessage
import com.telecommande.data.repository.TvCoreEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

class RemoteRepositoryImpl @Inject constructor(
    private val androidRemoteTv: AndroidRemoteTv
) : RemoteRepository {

    override val tvCoreEvents: Flow<TvCoreEvent> = androidRemoteTv.eventFlow
        .map { coreEvent ->
            Timber.d("RemoteRepo: Raw CoreAndroidTvEvent: %s", coreEvent)
            when (coreEvent) {
                is CoreAndroidTvEvent.Connected -> TvCoreEvent.Connected
                is CoreAndroidTvEvent.Disconnected -> TvCoreEvent.Disconnected
                is CoreAndroidTvEvent.Error -> TvCoreEvent.Error(coreEvent.message)
                is CoreAndroidTvEvent.VolumeUpdated -> TvCoreEvent.VolumeUpdated(
                    coreEvent.level,
                    coreEvent.max,
                    coreEvent.muted
                )
                is CoreAndroidTvEvent.AppLinkLaunchSent -> TvCoreEvent.AppLinkLaunchSent(coreEvent.appLink)
                is CoreAndroidTvEvent.SecretRequested -> TvCoreEvent.SecretRequested
                is CoreAndroidTvEvent.SessionCreated -> null
                is CoreAndroidTvEvent.Paired -> null
                is CoreAndroidTvEvent.ConnectingToRemote -> null
            }
        }
        .filterNotNull()

    override val isConnected: StateFlow<Boolean> = androidRemoteTv.isConnected

    override suspend fun connectToActiveTv(hostAddress: String) {
        Timber.d("RemoteRepo: Appel de androidRemoteTv.connect() avec l'adresse: %s", hostAddress)
        androidRemoteTv.connect(hostAddress)
    }

    override suspend fun disconnectFromTv() {
        Timber.d("RemoteRepo: Appel de androidRemoteTv.disconnect()")
        androidRemoteTv.disconnect()
    }

    override suspend fun sendCommand(
        keyCode: Remotemessage.RemoteKeyCode,
        action: Remotemessage.RemoteDirection
    ) {
        if (!isConnected.value) {
            Timber.w("RemoteRepo: Cannot send command %s, not connected.", keyCode.name)
            return
        }

        Timber.d("RemoteRepo: Appel de androidRemoteTv.sendCommand() pour %s.", keyCode.name)
        androidRemoteTv.sendCommand(keyCode, action)
    }

    override fun launchApplication(appLink: String) {
        if (!isConnected.value) {
            Timber.w("RemoteRepo: Cannot launch application %s, not connected.", appLink)
            return
        }

        Timber.d("RemoteRepo: Appel de androidRemoteTv.launchApplication() pour %s.", appLink)
        androidRemoteTv.launchApplication(appLink)
    }
}
