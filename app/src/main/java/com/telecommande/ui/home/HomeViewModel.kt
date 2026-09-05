package com.telecommande.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telecommande.core.protocol.TvCommand
import com.telecommande.data.repository.SettingsRepository
import com.telecommande.ui.manager.RemoteManager
import com.telecommande.util.resolveDisplayName
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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
    private val remoteManager: RemoteManager,
    settingsRepository: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        remoteManager.state,
        settingsRepository.activeTvInfoFlow,
        settingsRepository.tvDisplayNamesFlow
    ) { managerState, activeTvInfo, displayNames ->
        val displayName = activeTvInfo?.resolveDisplayName(displayNames)
            ?: managerState.activeTvName

        HomeUiState(
            isConnected = managerState.isConnected,
            isLoading = managerState.isLoading,
            activeTvName = displayName,
            snackbarMessage = managerState.snackbarMessage,
            pairingRequiredEvent = managerState.pairingRequiredOnActiveTv,
            volumeLevel = managerState.volumeLevel,
            volumeMax = managerState.volumeMax,
            isMuted = managerState.isMuted
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    init {
        remoteManager.initialize(viewModelScope)
    }

    fun sendPowerCommand() = sendCommand(TvCommand.POWER)
    fun sendDpadCenterCommand() = sendCommand(TvCommand.OK)
    fun sendDpadUpCommand() = sendCommand(TvCommand.UP)
    fun sendDpadDownCommand() = sendCommand(TvCommand.DOWN)
    fun sendDpadLeftCommand() = sendCommand(TvCommand.LEFT)
    fun sendDpadRightCommand() = sendCommand(TvCommand.RIGHT)
    fun sendBackCommand() = sendCommand(TvCommand.BACK)
    fun sendHomeCommand() = sendCommand(TvCommand.HOME)
    fun sendVolumeUpCommand() = sendCommand(TvCommand.VOLUME_UP)
    fun sendVolumeDownCommand() = sendCommand(TvCommand.VOLUME_DOWN)
    fun sendMuteCommand() = sendCommand(TvCommand.MUTE)
    fun sendMediaRewindCommand() = sendCommand(TvCommand.REWIND)
    fun sendMediaPlayPauseCommand() = sendCommand(TvCommand.PLAY_PAUSE)
    fun sendMediaStopCommand() = sendCommand(TvCommand.STOP)
    fun sendMediaFastForwardCommand() = sendCommand(TvCommand.FAST_FORWARD)

    private fun sendCommand(command: TvCommand) {
        remoteManager.sendCommand(command, viewModelScope)
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
