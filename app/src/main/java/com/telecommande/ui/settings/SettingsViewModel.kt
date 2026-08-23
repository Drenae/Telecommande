package com.telecommande.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telecommande.core.discovery.DiscoveredTv
import com.telecommande.data.model.PairedTvInfo
import com.telecommande.domain.usecase.pairing.GetActiveTvUseCase
import com.telecommande.domain.usecase.pairing.GetPairedTvsUseCase
import com.telecommande.domain.usecase.pairing.ResetPairingUseCase
import com.telecommande.domain.usecase.pairing.SetActiveTvUseCase
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
    private val getPairedTvsUseCase: GetPairedTvsUseCase,
    private val getActiveTvUseCase: GetActiveTvUseCase,
    private val setActiveTvUseCase: SetActiveTvUseCase,
    private val resetPairingUseCase: ResetPairingUseCase
) : ViewModel() {

    private val _currentPinInput = MutableStateFlow("")
    private val _internalSnackbarMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SettingsUiState> = combine(
        discoveryManager.state,
        remoteManager.state,
        pairingManager.currentStep,
        getPairedTvsUseCase(),
        getActiveTvUseCase(),
        _currentPinInput
    ) { flows ->
        val discoveryState = flows[0] as DiscoveryState
        val remoteState = flows[1] as RemoteState
        val pairingStep = flows[2] as PairingStep
        val pairedList = flows[3] as List<PairedTvInfo>
        val activeTvInfo = flows[4] as PairedTvInfo?
        val pinInput = flows[5] as String

        val isLoadingOverall = discoveryState.isDiscovering || remoteState.isLoading ||
                (pairingStep is PairingStep.Initiating)

        val isPinDialogActuallyLoading = pairingStep is PairingStep.VerifyingPin

        val statusMsgFromDiscovery = if (discoveryState.isDiscovering || discoveryState.statusMessage.isNotBlank()) discoveryState.statusMessage else null
        val statusMsgFromRemote = when {
            remoteState.isLoading && remoteState.activeTvName != null -> "Connexion à ${remoteState.activeTvName}..."
            remoteState.isLoading -> "Connexion en cours..."
            remoteState.isConnected && remoteState.activeTvName != null -> "Connecté à ${remoteState.activeTvName}"
            remoteState.isConnected -> "Connecté"
            activeTvInfo != null && !remoteState.isConnected -> "Déconnecté de ${activeTvInfo.name}"
            else -> if (remoteState.isConnected) "Connecté" else "Non connecté"
        }
        val statusMsgFromPairing = when (pairingStep) {
            is PairingStep.Initiating -> "Appairage avec ${pairingStep.tvName}..."
            is PairingStep.PinRequested -> "PIN requis par ${pairingStep.tvForPinEntry.friendlyName}"
            is PairingStep.VerifyingPin -> "Vérification du PIN pour ${pairingStep.tvName}..."
            is PairingStep.PairingSuccessful -> "Appairage réussi avec ${pairingStep.pairedTvInfo.name}. Connexion..."
            is PairingStep.Error -> pairingStep.message
            else -> null
        }

        val calculatedPrimaryStatusMessage = statusMsgFromPairing ?: statusMsgFromDiscovery ?: statusMsgFromRemote

        val calculatedErrorDialogContent = when {
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
            showPinEntryDialogForTv = if (pairingStep is PairingStep.PinRequested) pairingStep.tvForPinEntry else null,
            currentPinInput = pinInput,
            primaryStatusMessage = calculatedPrimaryStatusMessage,
            errorDialogContent = calculatedErrorDialogContent,
            isPinDialogLoading = isPinDialogActuallyLoading
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
        Timber.d("SettingsViewModel : Initialisation avec Hilt")
        discoveryManager.initialize(viewModelScope)
        remoteManager.initialize(viewModelScope)
        pairingManager.initialize(viewModelScope)

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
            Timber.d("TV ${tv.friendlyName} déjà appairée. Définition comme active.")
            setTvAsActive(existingPaired)
        } else {
            Timber.d("Lancement du processus d'appairage pour ${tv.friendlyName}.")
            pairingManager.startPairingProcess(tv, viewModelScope)
        }
    }

    fun onPinChanged(pin: String) {
        _currentPinInput.value = pin
    }

    fun onSubmitPin() {
        if (uiState.value.pairingStep is PairingStep.PinRequested) {
            pairingManager.submitPin(_currentPinInput.value, viewModelScope)
        } else {
            Timber.w("Tentative de soumission de PIN alors que non requis.")
        }
    }

    fun onCancelPinEntryOrPairing() {
        pairingManager.cancelPairingAttempt(viewModelScope)
        _currentPinInput.value = ""
    }

    fun acknowledgePairingError() {
        pairingManager.acknowledgeError()
        val currentPairingState = uiState.value.pairingStep
        if (currentPairingState is PairingStep.Error && currentPairingState.shouldConsiderReset) {
            val tvName = currentPairingState.tvName
            Timber.i("L'utilisateur a acquitté une erreur d'appairage avec une suggestion de réinitialisation pour $tvName.")
        }
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
        if (uiState.value.activeTv?.ipAddress == tvInfo.ipAddress && uiState.value.remoteState.isConnected) {
            _internalSnackbarMessage.value = "${tvInfo.name ?: tvInfo.ipAddress} est déjà active et connectée."
            return
        }
        _internalSnackbarMessage.value = "Sélection de ${tvInfo.name ?: tvInfo.ipAddress}..."
        viewModelScope.launch {
            try {
                setActiveTvUseCase(tvInfo)
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
        Timber.d("SettingsViewModel : onCleared")
    }
}