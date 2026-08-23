package com.telecommande.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telecommande.core.discovery.DiscoveredTv
import com.telecommande.data.model.PairedTvInfo
import com.telecommande.data.repository.SettingsRepository
import com.telecommande.domain.usecase.pairing.ResetPairingUseCase
import com.telecommande.ui.manager.DiscoveryManager
import com.telecommande.ui.manager.DiscoveryState
import com.telecommande.ui.manager.PairingManager
import com.telecommande.ui.manager.PairingStep
import com.telecommande.ui.manager.RemoteManager
import com.telecommande.ui.manager.RemoteState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class SettingsUiState(
    val discoveryState: DiscoveryState = DiscoveryState(),
    val remoteState: RemoteState = RemoteState(),
    val pairingStep: PairingStep = PairingStep.Idle,
    val pairedTvs: List<PairedTvInfo> = emptyList(),
    val activeTv: PairedTvInfo? = null,
    val isLoadingOverall: Boolean = false,
    val snackbarMessage: String? = null,
    val showPinEntryDialogForTv: DiscoveredTv? = null,
    val currentPinInput: String = "",
    val primaryStatusMessage: String = "",
    val errorDialogContent: String? = null,
    val isPinDialogLoading: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val discoveryManager: DiscoveryManager,
    private val remoteManager: RemoteManager,
    private val pairingManager: PairingManager,
    private val settingsRepository: SettingsRepository,
    private val resetPairingUseCase: ResetPairingUseCase
) : ViewModel() {

    private val _currentPinInput = MutableStateFlow("")
    private val _internalSnackbarMessage = MutableStateFlow<String?>(null)

    private val tvSelectionFlow = combine(
        settingsRepository.pairedTvsFlow,
        settingsRepository.activeTvInfoFlow
    ) { pairedTvs, activeTv ->
        pairedTvs to activeTv
    }

    val uiState: StateFlow<SettingsUiState> = combine(
        discoveryManager.state,
        remoteManager.state,
        pairingManager.currentStep,
        tvSelectionFlow,
        _currentPinInput
    ) { discoveryState, remoteState, pairingStep, tvSelection, pinInput ->
        val (pairedList, activeTvInfo) = tvSelection

        val isLoadingOverall = discoveryState.isDiscovering ||
            remoteState.isLoading ||
            pairingStep is PairingStep.Initiating

        val statusMsgFromDiscovery = if (
            discoveryState.isDiscovering || discoveryState.statusMessage.isNotBlank()
        ) {
            discoveryState.statusMessage
        } else {
            null
        }

        val statusMsgFromRemote = when {
            remoteState.isLoading && remoteState.activeTvName != null ->
                "Connexion à ${remoteState.activeTvName}..."
            remoteState.isLoading -> "Connexion en cours..."
            remoteState.isConnected && remoteState.activeTvName != null ->
                "Connecté à ${remoteState.activeTvName}"
            remoteState.isConnected -> "Connecté"
            activeTvInfo != null -> "Déconnecté de ${activeTvInfo.name ?: activeTvInfo.ipAddress}"
            else -> "Non connecté"
        }

        val statusMsgFromPairing = when (pairingStep) {
            is PairingStep.Initiating -> "Appairage avec ${pairingStep.tvName}..."
            is PairingStep.PinRequested ->
                "PIN requis par ${pairingStep.tvForPinEntry.friendlyName}"
            is PairingStep.VerifyingPin ->
                "Vérification du PIN pour ${pairingStep.tvName}..."
            is PairingStep.PairingSuccessful ->
                "Appairage réussi avec ${pairingStep.pairedTvInfo.name}. Connexion..."
            is PairingStep.Error -> pairingStep.message
            else -> null
        }

        val errorDialogContent = when {
            discoveryState.errorMessage != null -> discoveryState.errorMessage
            pairingStep is PairingStep.Error -> pairingStep.message
            else -> null
        }

        SettingsUiState(
            discoveryState = discoveryState,
            remoteState = remoteState,
            pairingStep = pairingStep,
            pairedTvs = pairedList,
            activeTv = activeTvInfo,
            isLoadingOverall = isLoadingOverall,
            snackbarMessage = _internalSnackbarMessage.value,
            showPinEntryDialogForTv = (pairingStep as? PairingStep.PinRequested)?.tvForPinEntry,
            currentPinInput = pinInput,
            primaryStatusMessage = statusMsgFromPairing ?: statusMsgFromDiscovery ?: statusMsgFromRemote,
            errorDialogContent = errorDialogContent,
            isPinDialogLoading = pairingStep is PairingStep.VerifyingPin
        )
    }.catch { e ->
        Timber.e(e, "Erreur lors de la combinaison des flux pour SettingsUiState")
        _internalSnackbarMessage.value = "Erreur de chargement de l'état: ${e.localizedMessage}"
        emit(SettingsUiState(snackbarMessage = _internalSnackbarMessage.value))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    init {
        remoteManager.initialize(viewModelScope)

        pairingManager.transientError
            .onEach { errorMsg ->
                _internalSnackbarMessage.value = errorMsg
            }
            .launchIn(viewModelScope)
    }

    fun toggleDiscovery() {
        if (uiState.value.discoveryState.isDiscovering) {
            discoveryManager.stopDiscovery()
        } else {
            discoveryManager.startDiscovery(viewModelScope)
        }
    }

    fun onDeviceSelected(tv: DiscoveredTv) {
        val existingPaired = uiState.value.pairedTvs.find { it.ipAddress == tv.ipAddress }
        if (existingPaired != null) {
            setTvAsActive(existingPaired)
        } else {
            pairingManager.startPairingProcess(tv, viewModelScope)
        }
    }

    fun onPinChanged(pin: String) {
        _currentPinInput.value = pin
    }

    fun onSubmitPin() {
        if (uiState.value.pairingStep is PairingStep.PinRequested) {
            pairingManager.submitPin(_currentPinInput.value, viewModelScope)
        }
    }

    fun onCancelPinEntryOrPairing() {
        pairingManager.cancelPairingAttempt(viewModelScope)
        _currentPinInput.value = ""
    }

    fun acknowledgePairingError() {
        pairingManager.acknowledgeError()
    }

    fun forgetTv(tvInfo: PairedTvInfo) {
        viewModelScope.launch {
            _internalSnackbarMessage.value = "Oubli de ${tvInfo.name}..."
            try {
                resetPairingUseCase(tvInfo.keystoreAlias)
                _internalSnackbarMessage.value = "${tvInfo.name} oubliée."
            } catch (e: Exception) {
                Timber.e(e, "Erreur lors de l'oubli de la TV ${tvInfo.name}")
                _internalSnackbarMessage.value = "Erreur d'oubli: ${e.message}"
            }
        }
    }

    fun setTvAsActive(tvInfo: PairedTvInfo) {
        if (
            uiState.value.activeTv?.ipAddress == tvInfo.ipAddress &&
            uiState.value.remoteState.isConnected
        ) {
            _internalSnackbarMessage.value =
                "${tvInfo.name ?: tvInfo.ipAddress} est déjà active et connectée."
            return
        }

        _internalSnackbarMessage.value = "Sélection de ${tvInfo.name ?: tvInfo.ipAddress}..."
        viewModelScope.launch {
            try {
                settingsRepository.saveActiveTvInfo(tvInfo)
            } catch (e: Exception) {
                Timber.e(e, "Erreur lors de la définition de la TV active ${tvInfo.name}")
                _internalSnackbarMessage.value = "Erreur de sélection: ${e.message}"
            }
        }
    }

    fun disconnectActiveTvFromSettings() {
        if (uiState.value.remoteState.isConnected && uiState.value.activeTv != null) {
            remoteManager.disconnect()
        } else {
            _internalSnackbarMessage.value = "Aucune TV n'est actuellement connectée."
        }
    }

    fun clearViewModelSnackbar() {
        _internalSnackbarMessage.value = null
    }

    fun clearDiscoveryErrorMessageFromVm() {
        discoveryManager.clearErrorMessage()
    }

    override fun onCleared() {
        super.onCleared()
        discoveryManager.cleanup()
        remoteManager.cleanup()
        pairingManager.cleanup()
    }
}
