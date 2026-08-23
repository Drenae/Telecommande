package com.telecommande.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telecommande.core.remote.Remotemessage
import com.telecommande.ui.manager.RemoteManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import timber.log.Timber
import javax.inject.Inject

data class HomeUiState(
    val isConnected: Boolean = false,
    val isLoading: Boolean = false,
    val activeTvName: String? = null,
    val snackbarMessage: String? = null,
    val pairingRequiredEvent: Boolean = false,
    val volumeLevel: Int = 0,
    val volumeMax: Int = 100,
    val isMuted: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val remoteManager: RemoteManager
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = remoteManager.state
        .map { managerState ->
            HomeUiState(
                isConnected = managerState.isConnected,
                isLoading = managerState.isLoading,
                activeTvName = managerState.activeTvName,
                snackbarMessage = managerState.snackbarMessage,
                pairingRequiredEvent = managerState.pairingRequiredOnActiveTv,
                volumeLevel = managerState.volumeLevel,
                volumeMax = managerState.volumeMax,
                isMuted = managerState.isMuted
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeUiState()
        )

    init {
        Timber.d("HomeViewModel : Initialisation avec Hilt")
        remoteManager.initialize(viewModelScope)
    }

    fun connectToActiveTv() {
        remoteManager.connect()
    }

    fun sendPowerCommand() = remoteManager.sendCommand(Remotemessage.RemoteKeyCode.KEYCODE_POWER, Remotemessage.RemoteDirection.SHORT, viewModelScope)
    fun sendDpadCenterCommand() = remoteManager.sendCommand(Remotemessage.RemoteKeyCode.KEYCODE_DPAD_CENTER, Remotemessage.RemoteDirection.SHORT, viewModelScope)
    fun sendDpadUpCommand() = remoteManager.sendCommand(Remotemessage.RemoteKeyCode.KEYCODE_DPAD_UP, Remotemessage.RemoteDirection.SHORT, viewModelScope)
    fun sendDpadDownCommand() = remoteManager.sendCommand(Remotemessage.RemoteKeyCode.KEYCODE_DPAD_DOWN, Remotemessage.RemoteDirection.SHORT, viewModelScope)
    fun sendDpadLeftCommand() = remoteManager.sendCommand(Remotemessage.RemoteKeyCode.KEYCODE_DPAD_LEFT, Remotemessage.RemoteDirection.SHORT, viewModelScope)
    fun sendDpadRightCommand() = remoteManager.sendCommand(Remotemessage.RemoteKeyCode.KEYCODE_DPAD_RIGHT, Remotemessage.RemoteDirection.SHORT, viewModelScope)
    fun sendBackCommand() = remoteManager.sendCommand(Remotemessage.RemoteKeyCode.KEYCODE_BACK, Remotemessage.RemoteDirection.SHORT, viewModelScope)
    fun sendHomeCommand() = remoteManager.sendCommand(Remotemessage.RemoteKeyCode.KEYCODE_HOME, Remotemessage.RemoteDirection.SHORT, viewModelScope)
    fun sendVolumeUpCommand() = remoteManager.sendCommand(Remotemessage.RemoteKeyCode.KEYCODE_VOLUME_UP, Remotemessage.RemoteDirection.SHORT, viewModelScope)
    fun sendVolumeDownCommand() = remoteManager.sendCommand(Remotemessage.RemoteKeyCode.KEYCODE_VOLUME_DOWN, Remotemessage.RemoteDirection.SHORT, viewModelScope)
    fun sendMuteCommand() = remoteManager.sendCommand(Remotemessage.RemoteKeyCode.KEYCODE_VOLUME_MUTE, Remotemessage.RemoteDirection.SHORT, viewModelScope)

    fun launchAppByLink(appLink: String) {
        remoteManager.launchApp(appLink, viewModelScope)
    }

    fun clearSnackbarMessage() {
        remoteManager.clearSnackbar()
    }

    fun consumePairingRequiredEvent() {
        remoteManager.consumePairingRequiredEvent()
    }

    override fun onCleared() {
        super.onCleared()
        remoteManager.cleanup()
        Timber.d("HomeViewModel : onCleared")
    }
}