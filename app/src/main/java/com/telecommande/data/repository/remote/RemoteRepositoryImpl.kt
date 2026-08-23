package com.telecommande.data.repository.remote

import com.telecommande.core.AndroidRemoteTv
import com.telecommande.core.remote.Remotemessage
import com.telecommande.data.repository.TvCoreEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject
import com.telecommande.core.event.AndroidTvEvent as CoreAndroidTvEvent

class RemoteRepositoryImpl @Inject constructor(
    private val androidRemoteTv: AndroidRemoteTv
) : RemoteRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override val tvCoreEvents: Flow<TvCoreEvent> = androidRemoteTv.eventFlow
        .map { coreEvent ->
            Timber.d("RemoteRepo: Raw CoreAndroidTvEvent: %s", coreEvent)
            when (coreEvent) {
                is CoreAndroidTvEvent.Connected -> TvCoreEvent.Connected
                is CoreAndroidTvEvent.Disconnected -> TvCoreEvent.Disconnected
                is CoreAndroidTvEvent.Error -> TvCoreEvent.Error(coreEvent.message)
                is CoreAndroidTvEvent.VolumeUpdated -> TvCoreEvent.VolumeUpdated(coreEvent.level, coreEvent.max, coreEvent.muted)
                is CoreAndroidTvEvent.AppLinkLaunchSent -> TvCoreEvent.AppLinkLaunchSent(coreEvent.appLink)
                is CoreAndroidTvEvent.SecretRequested -> TvCoreEvent.SecretRequested
                is CoreAndroidTvEvent.SessionCreated -> null
                is CoreAndroidTvEvent.Paired -> null
                is CoreAndroidTvEvent.ConnectingToRemote -> null
            }
        }
        .filterNotNull()
        .onEach { mappedEvent ->
            when(mappedEvent) {
                is TvCoreEvent.Connected -> _isConnected.value = true
                is TvCoreEvent.Disconnected, is TvCoreEvent.Error -> {
                    _isConnected.value = false
                }
                else -> { }
            }
            Timber.d("RemoteRepo: Emitting Filtered TvCoreEvent: %s, isConnected: %s", mappedEvent.javaClass.simpleName, _isConnected.value)
        }

    private val _isConnected = MutableStateFlow(androidRemoteTv.isConnected.value)
    override val isConnected: StateFlow<Boolean> = _isConnected

    init {
        androidRemoteTv.isConnected
            .onEach { connectionState ->
                if (_isConnected.value != connectionState) {
                    _isConnected.value = connectionState
                    Timber.d("RemoteRepo: _isConnected (from AndroidRemoteTv) mis à jour à: %s", connectionState)
                }
            }
            .launchIn(repositoryScope)
    }

    override suspend fun connectToActiveTv(hostAddress: String) {
        Timber.d("RemoteRepo: Appel de androidRemoteTv.connect() avec l'adresse: %s", hostAddress)
        try {
            androidRemoteTv.connect(hostAddress)
        } catch (e: Exception) {
            Timber.e(e, "RemoteRepo: Exception during connect to %s: %s", hostAddress, e.message)
            _isConnected.value = false
        }
    }

    override suspend fun disconnectFromTv() {
        Timber.d("RemoteRepo: Appel de androidRemoteTv.disconnect()")
        androidRemoteTv.disconnect()
    }

    override suspend fun sendCommand(
        keyCode: Remotemessage.RemoteKeyCode,
        action: Remotemessage.RemoteDirection
    ) {
        if (!_isConnected.value) {
            Timber.w("RemoteRepo: Cannot send command %s, not connected.", keyCode.name)
            return
        }
        Timber.d("RemoteRepo: Appel de androidRemoteTv.sendCommand() pour %s.", keyCode.name)
        try {
            androidRemoteTv.sendCommand(keyCode, action)
        } catch (e: Exception) {
            Timber.e(e, "RemoteRepo: Exception sending command %s: %s", keyCode.name, e.message)
        }
    }

    override fun launchApplication(appLink: String) {
        if (!_isConnected.value) {
            Timber.w("RemoteRepo: Cannot launch application %s, not connected.", appLink)
            return
        }
        Timber.d("RemoteRepo: Appel de androidRemoteTv.launchApplication() pour %s.", appLink)
        try {
            androidRemoteTv.launchApplication(appLink)
        } catch (e: Exception) {
            Timber.e(e, "RemoteRepo: Exception launching application %s: %s", appLink, e.message)
        }
    }
}