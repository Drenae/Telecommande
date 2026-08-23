package com.telecommande.ui.manager

import com.telecommande.core.remote.Remotemessage
import com.telecommande.data.model.PairedTvInfo
import com.telecommande.data.repository.SettingsRepository
import com.telecommande.data.repository.TvCoreEvent
import com.telecommande.data.repository.pairing.PairingRepository
import com.telecommande.data.repository.remote.RemoteRepository
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class RemoteState(
    val isConnected: Boolean = false,
    val isLoading: Boolean = false,
    val activeTvName: String? = null,
    val snackbarMessage: String? = null,
    val volumeLevel: Int = 0,
    val volumeMax: Int = 100,
    val isMuted: Boolean = false,
    val pairingRequiredOnActiveTv: Boolean = false
)

@ViewModelScoped
class RemoteManager @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val pairingRepository: PairingRepository,
    private val remoteRepository: RemoteRepository
) {
    private val _state = MutableStateFlow(RemoteState())
    val state: StateFlow<RemoteState> = _state.asStateFlow()

    private var activeTvInfo: PairedTvInfo? = null
    private var remoteEventsJob: Job? = null
    private var connectionStateJob: Job? = null
    private var activeTvJob: Job? = null
    private var connectionJob: Job? = null
    private var managerScope: CoroutineScope? = null

    fun initialize(scope: CoroutineScope) {
        managerScope = scope
        _state.update {
            it.copy(
                isLoading = false,
                snackbarMessage = null,
                pairingRequiredOnActiveTv = false
            )
        }
        observeActiveTv(scope)
        observeConnectionState(scope)
        observeRemoteEvents(scope)
    }

    private fun observeActiveTv(scope: CoroutineScope) {
        activeTvJob?.cancel()
        activeTvJob = settingsRepository.activeTvInfoFlow
            .distinctUntilChanged()
            .onEach { tvInfo ->
                val previousTv = activeTvInfo
                activeTvInfo = tvInfo

                _state.update {
                    it.copy(
                        activeTvName = tvInfo?.name ?: tvInfo?.ipAddress,
                        pairingRequiredOnActiveTv = false
                    )
                }

                when {
                    tvInfo == null -> handleNoActiveTv()
                    previousTv != null && previousTv.ipAddress != tvInfo.ipAddress ->
                        startConnection(tvInfo, disconnectFirst = true)
                    !_state.value.isConnected && !_state.value.isLoading ->
                        startConnection(tvInfo)
                    else -> Unit
                }
            }
            .catch { e ->
                Timber.e(e, "RemoteManager : Erreur lors de l'observation de la TV active")
                _state.update {
                    it.copy(
                        snackbarMessage = "Erreur de chargement des paramètres TV.",
                        isLoading = false
                    )
                }
            }
            .launchIn(scope)
    }

    private fun handleNoActiveTv() {
        connectionJob?.cancel()
        connectionJob = null

        if (_state.value.isConnected || _state.value.isLoading) {
            disconnect()
        } else {
            _state.update {
                it.copy(
                    isConnected = false,
                    isLoading = false,
                    activeTvName = null
                )
            }
        }
    }

    private fun observeConnectionState(scope: CoroutineScope) {
        connectionStateJob?.cancel()
        connectionStateJob = remoteRepository.isConnected
            .onEach { connected ->
                _state.update {
                    it.copy(
                        isConnected = connected,
                        isLoading = if (connected) false else connectionJob?.isActive == true
                    )
                }
            }
            .catch { e ->
                Timber.e(e, "RemoteManager : Erreur lors de l'observation de l'état de connexion")
                _state.update {
                    it.copy(
                        snackbarMessage = "Erreur de connexion.",
                        isLoading = false,
                        isConnected = false
                    )
                }
            }
            .launchIn(scope)
    }

    private fun observeRemoteEvents(scope: CoroutineScope) {
        remoteEventsJob?.cancel()
        remoteEventsJob = remoteRepository.tvCoreEvents
            .onEach { event ->
                _state.update { current ->
                    when (event) {
                        is TvCoreEvent.SecretRequested -> current.copy(
                            pairingRequiredOnActiveTv = true,
                            isConnected = false,
                            isLoading = false
                        )
                        is TvCoreEvent.Connected -> current.copy(
                            isConnected = true,
                            isLoading = false,
                            pairingRequiredOnActiveTv = false
                        )
                        is TvCoreEvent.Disconnected -> current.copy(
                            isConnected = false,
                            isLoading = connectionJob?.isActive == true
                        )
                        is TvCoreEvent.Error -> current.copy(
                            snackbarMessage = "Erreur TV: ${event.message}",
                            isConnected = false,
                            isLoading = false
                        )
                        is TvCoreEvent.VolumeUpdated -> current.copy(
                            volumeLevel = event.level,
                            volumeMax = if (event.max > 0) event.max else 100,
                            isMuted = event.muted
                        )
                        is TvCoreEvent.AppLinkLaunchSent -> current.copy(
                            snackbarMessage = "Lancement de l'application ${event.appLink} demandé."
                        )
                        else -> current
                    }
                }
            }
            .catch { e ->
                Timber.e(e, "RemoteManager : Erreur lors de l'observation des événements distants")
                _state.update {
                    it.copy(
                        snackbarMessage = "Erreur interne de communication TV.",
                        isLoading = false,
                        isConnected = false
                    )
                }
            }
            .launchIn(scope)
    }

    private fun startConnection(tv: PairedTvInfo, disconnectFirst: Boolean = false) {
        val scope = managerScope ?: run {
            _state.update {
                it.copy(
                    snackbarMessage = "Erreur interne : scope manquant pour la connexion.",
                    isLoading = false
                )
            }
            return
        }

        if (!disconnectFirst && (_state.value.isConnected || connectionJob?.isActive == true || _state.value.isLoading)) {
            return
        }

        connectionJob?.cancel()
        connectionJob = scope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    snackbarMessage = "Connexion à ${tv.name ?: tv.ipAddress}..."
                )
            }

            try {
                if (disconnectFirst && _state.value.isConnected) {
                    remoteRepository.disconnectFromTv()
                }
                pairingRepository.connectForPairing(tv.ipAddress, tv.keystoreAlias)
            } catch (e: Exception) {
                Timber.e(e, "RemoteManager : Échec de connexion à ${tv.ipAddress}")
                _state.update {
                    it.copy(
                        snackbarMessage = "Échec de connexion à ${tv.name ?: tv.ipAddress}.",
                        isConnected = false,
                        isLoading = false
                    )
                }
            } finally {
                connectionJob = null
            }
        }
    }

    fun connect() {
        val tv = activeTvInfo ?: run {
            _state.update {
                it.copy(
                    snackbarMessage = "Aucune TV active sélectionnée pour la connexion.",
                    isConnected = false,
                    isLoading = false
                )
            }
            return
        }
        startConnection(tv)
    }

    fun disconnect() {
        val scope = managerScope ?: return

        connectionJob?.cancel()
        connectionJob = null
        _state.update { it.copy(isLoading = true) }

        scope.launch {
            try {
                remoteRepository.disconnectFromTv()
            } catch (e: Exception) {
                Timber.e(e, "RemoteManager : Erreur pendant la déconnexion")
                _state.update {
                    it.copy(
                        snackbarMessage = "Erreur de déconnexion.",
                        isConnected = false
                    )
                }
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun sendCommand(
        keyCode: Remotemessage.RemoteKeyCode,
        action: Remotemessage.RemoteDirection,
        scope: CoroutineScope
    ) {
        if (!_state.value.isConnected) {
            _state.update { it.copy(snackbarMessage = "Non connecté. Veuillez connecter une TV.") }
            if (activeTvInfo != null) connect()
            return
        }

        scope.launch {
            try {
                remoteRepository.sendCommand(keyCode, action)
            } catch (e: Exception) {
                Timber.e(e, "RemoteManager : Erreur lors de l'envoi de ${keyCode.name}")
                _state.update { it.copy(snackbarMessage = "Erreur d'envoi: ${e.message}") }
            }
        }
    }

    fun launchApp(appLink: String, scope: CoroutineScope) {
        if (appLink.isBlank()) {
            _state.update { it.copy(snackbarMessage = "Lien d'application non valide.") }
            return
        }
        if (!_state.value.isConnected) {
            _state.update { it.copy(snackbarMessage = "Non connecté pour lancer l'application.") }
            return
        }

        scope.launch {
            try {
                remoteRepository.launchApplication(appLink)
            } catch (e: Exception) {
                Timber.e(e, "RemoteManager : Erreur lors du lancement de l'application $appLink")
                _state.update { it.copy(snackbarMessage = "Erreur de lancement: ${e.message}") }
            }
        }
    }

    fun clearSnackbar() {
        _state.update { it.copy(snackbarMessage = null) }
    }

    fun consumePairingRequiredEvent() {
        _state.update { it.copy(pairingRequiredOnActiveTv = false) }
    }

    fun cleanup() {
        connectionJob?.cancel()
        activeTvJob?.cancel()
        connectionStateJob?.cancel()
        remoteEventsJob?.cancel()
        connectionJob = null
        managerScope = null
    }
}
