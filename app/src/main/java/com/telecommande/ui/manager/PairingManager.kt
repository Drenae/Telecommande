package com.telecommande.ui.manager

import com.telecommande.core.discovery.DiscoveredTv
import com.telecommande.data.model.PairedTvInfo
import com.telecommande.data.repository.TvCoreEvent
import com.telecommande.domain.usecase.connection.ConnectToTvUseCase
import com.telecommande.domain.usecase.connection.DisconnectFromTvUseCase
import com.telecommande.domain.usecase.pairing.AddPairedTvUseCase
import com.telecommande.domain.usecase.pairing.IsKeystorePairedUseCase
import com.telecommande.domain.usecase.pairing.ObservePairingEventsUseCase
import com.telecommande.domain.usecase.pairing.SetActiveTvUseCase
import com.telecommande.domain.usecase.pairing.SubmitPinUseCase
import com.telecommande.util.containsAnyOf
import com.telecommande.util.getMacAddressFromAttributesOrNull
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

sealed class PairingStep {
    data object Idle : PairingStep()
    data class Initiating(val tvName: String) : PairingStep()
    data class PinRequested(val tvForPinEntry: DiscoveredTv) : PairingStep()
    data class VerifyingPin(val tvName: String) : PairingStep()
    data class PairingSuccessful(val pairedTvInfo: PairedTvInfo) : PairingStep()
    data class Error(val message: String, val tvName: String?, val shouldConsiderReset: Boolean = false) : PairingStep()
}

@ViewModelScoped
class PairingManager @Inject constructor(
    private val observePairingEventsUseCase: ObservePairingEventsUseCase,
    private val connectToTvUseCase: ConnectToTvUseCase,
    private val submitPinUseCase: SubmitPinUseCase,
    private val addPairedTvUseCase: AddPairedTvUseCase,
    private val setActiveTvUseCase: SetActiveTvUseCase,
    private val disconnectFromTvUseCase: DisconnectFromTvUseCase,
    private val isKeystorePairedUseCase: IsKeystorePairedUseCase
) {
    private val _currentStep = MutableStateFlow<PairingStep>(PairingStep.Idle)
    val currentStep: StateFlow<PairingStep> = _currentStep.asStateFlow()

    private val _transientError = MutableSharedFlow<String>()
    val transientError: SharedFlow<String> = _transientError.asSharedFlow()

    private var pairingEventsJob: Job? = null
    private var connectionAttemptJob: Job? = null

    private var targetTvForPairing: DiscoveredTv? = null
    private var targetIpForConnection: String? = null
    private var managerScope: CoroutineScope? = null

    fun initialize(scope: CoroutineScope) {
        managerScope = scope
    }

    fun startPairingProcess(tvToPair: DiscoveredTv, scope: CoroutineScope) {
        managerScope = scope
        cancelCurrentProcess()

        val ipAddress = tvToPair.ipAddress ?: run {
            Timber.w("PairingManager : Tentative d'appairage avec une TV sans adresse IP.")
            _currentStep.value = PairingStep.Error("Adresse IP de la TV manquante.", tvToPair.friendlyName)
            return
        }
        val tvName = tvToPair.friendlyName

        targetTvForPairing = tvToPair
        targetIpForConnection = ipAddress

        _currentStep.value = PairingStep.Initiating(tvName)
        observePairingEvents(ipAddress, tvName, tvToPair, scope)

        connectionAttemptJob = scope.launch {
            try {
                Timber.d("PairingManager : Appel à connectToTvUseCase pour $ipAddress (appairage)")
                connectToTvUseCase(ipAddress)
            } catch (e: Exception) {
                Timber.e(e, "PairingManager : Échec de l'initiation de la connexion pour l'appairage $ipAddress")
                _currentStep.value = PairingStep.Error("Échec de l'initiation de l'appairage: ${e.message ?: "Inconnue"}", tvName)
                cleanupAfterError(scope)
            }
        }
    }

    private fun observePairingEvents(
        currentIp: String,
        currentTvName: String,
        currentDiscoveredTv: DiscoveredTv,
        scope: CoroutineScope
    ) {
        pairingEventsJob?.cancel()
        pairingEventsJob = observePairingEventsUseCase()
            .distinctUntilChanged()
            .onEach { event ->
                Timber.d("PairingManager : Événement d'appairage pour $currentIp (cible): $event")

                val eventIsPotentiallyForThisTarget = when (event) {
                    is TvCoreEvent.Paired -> event.host == currentIp
                    is TvCoreEvent.Error -> true
                    is TvCoreEvent.SecretRequested, is TvCoreEvent.SessionCreated, is TvCoreEvent.ConnectingToRemote -> true
                    else -> false
                }

                if (!eventIsPotentiallyForThisTarget) {
                    Timber.v("PairingManager : Événement $event ignoré, non pertinent pour le flux d'appairage de $currentIp.")
                    return@onEach
                }

                when (event) {
                    is TvCoreEvent.SessionCreated -> {
                        Timber.d("PairingManager : Session d'appairage créée pour $currentTvName.")
                    }
                    is TvCoreEvent.SecretRequested -> {
                        _currentStep.value = PairingStep.PinRequested(currentDiscoveredTv)
                    }
                    is TvCoreEvent.Paired -> {
                        if (event.host != currentIp) {
                            Timber.w("PairingManager : Événement 'Paired' reçu pour ${event.host} mais la cible est $currentIp. Ignoré.")
                            return@onEach
                        }
                        val newPairedTv = PairedTvInfo(
                            ipAddress = event.host,
                            name = currentTvName,
                            macAddress = currentDiscoveredTv.getMacAddressFromAttributesOrNull(),
                            keystoreAlias = event.tvKeystoreAlias
                        )
                        scope.launch {
                            addPairedTvUseCase(newPairedTv)
                            setActiveTvUseCase(newPairedTv)
                        }
                        _currentStep.value = PairingStep.PairingSuccessful(newPairedTv)
                        cancelCurrentProcess(clearTargetInfo = true)
                    }
                    is TvCoreEvent.Error -> {
                        val errorMessage = event.message ?: "Erreur d'appairage inconnue"
                        val isSslOrKeystoreIssue = errorMessage.containsAnyOf(listOf("SSL", "certificate", "EACCES", "trust anchor"), ignoreCase = true)
                        val considerReset = isSslOrKeystoreIssue && isKeystorePairedUseCase()

                        _currentStep.value = PairingStep.Error(errorMessage, currentTvName, considerReset)
                        cleanupAfterError(scope)
                    }
                    is TvCoreEvent.ConnectingToRemote -> {
                        Timber.d("PairingManager : Tentative de connexion distante pour l'appairage de $currentTvName.")
                    }
                    else -> { Timber.v("PairingManager : Événement $event non traité spécifiquement.") }
                }
            }
            .catch { e ->
                Timber.e(e, "PairingManager : Erreur critique lors de l'observation des événements d'appairage pour $currentIp")
                _currentStep.value = PairingStep.Error("Erreur de communication: ${e.message ?: "Inconnue"}", currentTvName)
                cleanupAfterError(scope)
            }
            .launchIn(scope)
    }

    fun submitPin(pin: String, scope: CoroutineScope) {
        val step = _currentStep.value
        if (step !is PairingStep.PinRequested) {
            Timber.w("PairingManager : Impossible de soumettre le PIN, pas dans l'état PinRequis. État actuel: $step")
            scope.launch { _transientError.emit("Impossible de soumettre le PIN pour le moment.") }
            return
        }
        val tvName = step.tvForPinEntry.friendlyName
        _currentStep.value = PairingStep.VerifyingPin(tvName)
        scope.launch {
            try {
                submitPinUseCase(pin)
            } catch (e: Exception) {
                Timber.e(e, "PairingManager : Erreur lors de la soumission du PIN")
                val errorMsg = e.message ?: "Erreur inconnue"
                _currentStep.value = PairingStep.Error("Erreur PIN: $errorMsg", tvName)
                cleanupAfterError(scope)
            }
        }
    }

    fun cancelPairingAttempt(scope: CoroutineScope) {
        val tvNameToLog = targetTvForPairing?.friendlyName ?: targetIpForConnection
        Timber.d("PairingManager : Annulation de la tentative d'appairage pour $tvNameToLog")

        val ipToDisconnect = targetIpForConnection
        cancelCurrentProcess(clearTargetInfo = true)

        scope.launch {
            if (ipToDisconnect != null) {
                try {
                    disconnectFromTvUseCase()
                } catch (e: Exception) {
                    Timber.w(e, "PairingManager : Exception pendant la déconnexion lors de l'annulation pour $ipToDisconnect.")
                }
            }
            _currentStep.value = PairingStep.Idle
        }
    }

    fun acknowledgeError() {
        if (_currentStep.value is PairingStep.Error) {
            _currentStep.value = PairingStep.Idle
        }
    }

    private fun cleanupAfterError(scope: CoroutineScope, delayMs: Long = 500) {
        scope.launch {
            if (_currentStep.value is PairingStep.Error) {
            }
            pairingEventsJob?.cancel()
            connectionAttemptJob?.cancel()
            pairingEventsJob = null
            connectionAttemptJob = null
        }
    }

    private fun cancelCurrentProcess(clearTargetInfo: Boolean = false) {
        pairingEventsJob?.cancel()
        connectionAttemptJob?.cancel()
        pairingEventsJob = null
        connectionAttemptJob = null
        if (clearTargetInfo) {
            targetTvForPairing = null
            targetIpForConnection = null
        }
    }

    fun cleanup() {
        Timber.d("PairingManager : Nettoyage des ressources.")
        cancelCurrentProcess(clearTargetInfo = true)
        managerScope = null
    }
}