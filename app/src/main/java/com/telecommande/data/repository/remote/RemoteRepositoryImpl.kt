package com.telecommande.data.repository.remote

import com.telecommande.core.AndroidRemoteTv
import com.telecommande.core.event.AndroidTvEvent as CoreAndroidTvEvent
import com.telecommande.core.protocol.TvCommand
import com.telecommande.core.protocol.TvProtocolType
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

    override suspend fun connectToTv(
        hostAddress: String,
        credentialId: String?,
        protocolType: TvProtocolType
    ) {
        when (protocolType) {
            TvProtocolType.ANDROID_TV_REMOTE_V2 -> androidRemoteTv.connect(hostAddress, credentialId)
            else -> throw UnsupportedOperationException(
                "Protocole TV non encore implémenté pour la connexion : $protocolType"
            )
        }
    }

    override suspend fun disconnectFromTv() {
        androidRemoteTv.disconnect()
    }

    override suspend fun sendCommand(command: TvCommand) {
        if (!isConnected.value) {
            Timber.w("RemoteRepo: Cannot send command %s, not connected.", command.name)
            return
        }

        androidRemoteTv.sendCommand(command)
    }

    override fun launchApplication(appLink: String) {
        if (!isConnected.value) {
            Timber.w("RemoteRepo: Cannot launch application %s, not connected.", appLink)
            return
        }

        androidRemoteTv.launchApplication(appLink)
    }
}
