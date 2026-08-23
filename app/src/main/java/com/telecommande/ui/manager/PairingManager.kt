package com.telecommande.ui.manager

import com.telecommande.core.discovery.DiscoveredTv
import com.telecommande.data.model.PairedTvInfo
import com.telecommande.data.repository.SettingsRepository
import com.telecommande.data.repository.TvCoreEvent
import com.telecommande.data.repository.pairing.PairingRepository
import com.telecommande.data.repository.remote.RemoteRepository
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
    data class Error(
        val message: String,
        val tvName: String?,
        val shouldConsiderReset: Boolean = false
    ) : PairingStep()
}

@ViewModelScoped
class PairingManager @Inject constructor(
    private val pairingRepository: PairingRepository,
    private val remoteRepository: RemoteRepository,
    private val settingsRepository: SettingsRepository
) {
    private val _currentStep = MutableStateFlow<PairingStep>(PairingStep.Idle)
    val currentStep: StateFlow<PairingStep> = _currentStep.asStateFlow()

    private val _transientError = MutableSharedFlow<String>()
    val transientError: SharedFlow<String> = _transientError.asSharedFlow()

    private var pairingEventsJob: Job? = null
    private var connectionAttemptJob: Job? = null
    private var hasActivePairingTarget = false

    fun startPairingProcess(tvToPair: DiscoveredTv, scope: CoroutineScope) {
        cancelCurrentProcess()

        val ipAddress = tvToPair.ipAddress ?: run {
            Timber.w("PairingManager : Tentative d'appairage avec une TV sans adresse IP.")
            _currentStep.value = PairingStep.Error(
                "Adresse IP de la TV manquante.",
                tvToPair.friendlyName
            )
            return
        }
        val tvName = tvToPair.friendlyName

        hasActivePairingTarget = true
        _currentStep.value = PairingStep.Initiating(tvName)
        observePairingEvents(ipAddress, tvName, tvToPair, scope)

        connectionAttemptJob = scope.launch {
            try {
                pairingRepository.connectForPairing(ipAddress)
            } catch (e: Exception) {
                Timber.e(e, "PairingManager : Échec de l'initiation de l'appairage $ipAddress")
                _currentStep.value = PairingStep.Error(
                    "Échec de l'initiation de l'appairage: ${e.message ?: "Inconnue"}",
                    tvName
                )
                cleanupAfterError()
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
        pairingEventsJob = pairingRepository.tvCoreEvents
            .distinctUntilChanged()
            .onEach { event ->
                val eventIsPotentiallyForThisTarget = when (event) {
                    is TvCoreEvent.Paired -> event.host == currentIp
                    is TvCoreEvent.Error -> true
                    is TvCoreEvent.SecretRequested,
                    is TvCoreEvent.SessionCreated,
                    is TvCoreEvent.ConnectingToRemote -> true
                    else -> false
                }

                if (!eventIsPotentiallyForThisTarget) return@onEach

                when (event) {
                    is TvCoreEvent.SessionCreated -> Unit
                    is TvCoreEvent.SecretRequested -> {
                        _currentStep.value = PairingStep.PinRequested(currentDiscoveredTv)
                    }
                    is TvCoreEvent.Paired -> {
                        if (event.host != currentIp) return@onEach

                        val newPairedTv = PairedTvInfo(
                            ipAddress = event.host,
                            name = currentTvName,
                            macAddress = currentDiscoveredTv.getMacAddressFromAttributesOrNull(),
                            keystoreAlias = event.tvKeystoreAlias
                        )

                        scope.launch {
                            settingsRepository.addPairedTv(newPairedTv)
                            settingsRepository.saveActiveTvInfo(newPairedTv)
                        }

                        _currentStep.value = PairingStep.PairingSuccessful(newPairedTv)
                        cancelCurrentProcess(clearTargetInfo = true)
                    }
                    is TvCoreEvent.Error -> {
                        val errorMessage = event.message ?: "Erreur d'appairage inconnue"
                        val isSslOrKeystoreIssue = errorMessage.containsAnyOf(
                            listOf("SSL", "certificate", "EACCES", "trust anchor"),
                            ignoreCase = true
                        )
                        val considerReset = isSslOrKeystoreIssue && pairingRepository.isKeystorePairedInitially()

                        _currentStep.value = PairingStep.Error(
                            errorMessage,
                            currentTvName,
                            considerReset
                        )
                        cleanupAfterError()
                    }
                    is TvCoreEvent.ConnectingToRemote -> Unit
                    else -> Unit
                }
            }
            .catch { e ->
                Timber.e(e, "PairingManager : Erreur critique du flux d'appairage $currentIp")
                _currentStep.value = PairingStep.Error(
                    "Erreur de communication: ${e.message ?: "Inconnue"}",
                    currentTvName
                )
                cleanupAfterError()
            }
            .launchIn(scope)
    }

    fun submitPin(pin: String, scope: CoroutineScope) {
        val step = _currentStep.value
        if (step !is PairingStep.PinRequested) {
            scope.launch {
                _transientError.emit("Impossible de soumettre le PIN pour le moment.")
            }
            return
        }

        val tvName = step.tvForPinEntry.friendlyName
        _currentStep.value = PairingStep.VerifyingPin(tvName)

        scope.launch {
            try {
                pairingRepository.sendSecret(pin)
            } catch (e: Exception) {
                Timber.e(e, "PairingManager : Erreur lors de la soumission du PIN")
                _currentStep.value = PairingStep.Error(
                    "Erreur PIN: ${e.message ?: "Erreur inconnue"}",
                    tvName
                )
                cleanupAfterError()
            }
        }
    }

    fun cancelPairingAttempt(scope: CoroutineScope) {
        val shouldDisconnect = hasActivePairingTarget
        cancelCurrentProcess(clearTargetInfo = true)

        scope.launch {
            if (shouldDisconnect) {
                try {
                    remoteRepository.disconnectFromTv()
                } catch (e: Exception) {
                    Timber.w(e, "PairingManager : Erreur pendant l'annulation de l'appairage")
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

    private fun cleanupAfterError() {
        pairingEventsJob?.cancel()
        connectionAttemptJob?.cancel()
        pairingEventsJob = null
        connectionAttemptJob = null
    }

    private fun cancelCurrentProcess(clearTargetInfo: Boolean = false) {
        pairingEventsJob?.cancel()
        connectionAttemptJob?.cancel()
        pairingEventsJob = null
        connectionAttemptJob = null

        if (clearTargetInfo) {
            hasActivePairingTarget = false
        }
    }

    fun cleanup() {
        cancelCurrentProcess(clearTargetInfo = true)
    }
}