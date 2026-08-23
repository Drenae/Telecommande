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
        remoteManager.initialize(viewModelScope)
    }

    fun sendPowerCommand() = sendShortCommand(Remotemessage.RemoteKeyCode.KEYCODE_POWER)
    fun sendDpadCenterCommand() = sendShortCommand(Remotemessage.RemoteKeyCode.KEYCODE_DPAD_CENTER)
    fun sendDpadUpCommand() = sendShortCommand(Remotemessage.RemoteKeyCode.KEYCODE_DPAD_UP)
    fun sendDpadDownCommand() = sendShortCommand(Remotemessage.RemoteKeyCode.KEYCODE_DPAD_DOWN)
    fun sendDpadLeftCommand() = sendShortCommand(Remotemessage.RemoteKeyCode.KEYCODE_DPAD_LEFT)
    fun sendDpadRightCommand() = sendShortCommand(Remotemessage.RemoteKeyCode.KEYCODE_DPAD_RIGHT)
    fun sendBackCommand() = sendShortCommand(Remotemessage.RemoteKeyCode.KEYCODE_BACK)
    fun sendHomeCommand() = sendShortCommand(Remotemessage.RemoteKeyCode.KEYCODE_HOME)
    fun sendVolumeUpCommand() = sendShortCommand(Remotemessage.RemoteKeyCode.KEYCODE_VOLUME_UP)
    fun sendVolumeDownCommand() = sendShortCommand(Remotemessage.RemoteKeyCode.KEYCODE_VOLUME_DOWN)
    fun sendMuteCommand() = sendShortCommand(Remotemessage.RemoteKeyCode.KEYCODE_VOLUME_MUTE)

    private fun sendShortCommand(keyCode: Remotemessage.RemoteKeyCode) {
        remoteManager.sendCommand(keyCode, Remotemessage.RemoteDirection.SHORT, viewModelScope)
    }

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
    }
}