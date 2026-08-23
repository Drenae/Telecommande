package com.telecommande.ui.manager

import com.telecommande.core.remote.Remotemessage
import com.telecommande.data.model.PairedTvInfo
import com.telecommande.data.repository.TvCoreEvent
import com.telecommande.domain.usecase.connection.ConnectToTvUseCase
import com.telecommande.domain.usecase.connection.DisconnectFromTvUseCase
import com.telecommande.domain.usecase.connection.ObserveTvConnectionStateUseCase
import com.telecommande.domain.usecase.pairing.GetActiveTvUseCase
import com.telecommande.domain.usecase.remote.LaunchAppUseCase
import com.telecommande.domain.usecase.remote.ObserveRemoteControlEventsUseCase
import com.telecommande.domain.usecase.remote.SendCommandUseCase
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
    private val getActiveTvUseCase: GetActiveTvUseCase,
    private val connectToTvUseCase: ConnectToTvUseCase,
    private val observeTvConnectionStateUseCase: ObserveTvConnectionStateUseCase,
    private val observeRemoteControlEventsUseCase: ObserveRemoteControlEventsUseCase,
    private val disconnectFromTvUseCase: DisconnectFromTvUseCase,
    private val sendCommandUseCase: SendCommandUseCase,
    private val launchAppUseCase: LaunchAppUseCase
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
        Timber.d("RemoteManager : Initialisation")
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
        activeTvJob = getActiveTvUseCase()
            .distinctUntilChanged()
            .onEach { tvInfo ->
                Timber.d("RemoteManager : TV active mise à jour : ${tvInfo?.name}")
                val previousActiveTvIp = activeTvInfo?.ipAddress
                activeTvInfo = tvInfo

                _state.update {
                    it.copy(
                        activeTvName = tvInfo?.name ?: tvInfo?.ipAddress,
                        pairingRequiredOnActiveTv = false
                    )
                }

                when {
                    tvInfo == null -> {
                        Timber.d("RemoteManager : Aucune TV active définie.")
                        connectionJob?.cancel()
                        if (_state.value.isConnected) {
                            disconnect()
                        } else {
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    isConnected = false,
                                    activeTvName = null
                                )
                            }
                        }
                    }

                    previousActiveTvIp != null && tvInfo.ipAddress != previousActiveTvIp -> {
                        Timber.i("RemoteManager : Changement de TV active vers ${tvInfo.ipAddress}.")
                        connectionJob?.cancel()
                        if (_state.value.isConnected) {
                            disconnect()
                        }
                        connect()
                    }

                    !_state.value.isConnected && !_state.value.isLoading -> {
                        Timber.i("RemoteManager : TV active non connectée. Connexion à ${tvInfo.ipAddress}.")
                        connect()
                    }

                    else -> {
                        Timber.d("RemoteManager : Connexion déjà active ou en cours pour ${tvInfo.ipAddress}.")
                    }
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

    private fun observeConnectionState(scope: CoroutineScope) {
        connectionStateJob?.cancel()
        connectionStateJob = observeTvConnectionStateUseCase()
            .distinctUntilChanged()
            .onEach { isConnected ->
                Timber.d("RemoteManager : État de connexion global changé : $isConnected")
                _state.update {
                    it.copy(
                        isConnected = isConnected,
                        isLoading = false
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
        remoteEventsJob = observeRemoteControlEventsUseCase()
            .onEach { event ->
                Timber.d("RemoteManager : Événement distant reçu : $event")
                _state.update { currentState ->
                    when (event) {
                        is TvCoreEvent.SecretRequested -> {
                            Timber.w("RemoteManager : La TV active ${currentState.activeTvName} demande un PIN.")
                            currentState.copy(
                                pairingRequiredOnActiveTv = true,
                                isConnected = false,
                                isLoading = false
                            )
                        }

                        is TvCoreEvent.Connected -> currentState.copy(
                            isLoading = false,
                            isConnected = true,
                            pairingRequiredOnActiveTv = false
                        )

                        is TvCoreEvent.Disconnected -> currentState.copy(
                            isLoading = false,
                            isConnected = false
                        )

                        is TvCoreEvent.Error -> currentState.copy(
                            snackbarMessage = "Erreur TV: ${event.message}",
                            isLoading = false,
                            isConnected = false
                        )

                        is TvCoreEvent.VolumeUpdated -> currentState.copy(
                            volumeLevel = event.level,
                            volumeMax = if (event.max > 0) event.max else 100,
                            isMuted = event.muted
                        )

                        is TvCoreEvent.AppLinkLaunchSent -> currentState.copy(
                            snackbarMessage = "Lancement de l'application ${event.appLink} demandé."
                        )

                        else -> currentState
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

    fun connect() {
        val currentScope = managerScope ?: run {
            Timber.e("RemoteManager : managerScope est nul, impossible de lancer la connexion.")
            _state.update {
                it.copy(
                    snackbarMessage = "Erreur interne : scope manquant pour la connexion.",
                    isLoading = false
                )
            }
            return
        }

        val tv = activeTvInfo ?: run {
            _state.update {
                it.copy(
                    snackbarMessage = "Aucune TV active sélectionnée pour la connexion.",
                    isLoading = false,
                    isConnected = false
                )
            }
            return
        }

        if (_state.value.isConnected) {
            Timber.d("RemoteManager : Déjà connecté à ${tv.ipAddress}.")
            _state.update { it.copy(isLoading = false) }
            return
        }

        if (connectionJob?.isActive == true || _state.value.isLoading) {
            Timber.d("RemoteManager : Une tentative de connexion est déjà en cours pour ${tv.ipAddress}.")
            return
        }

        Timber.i("RemoteManager : Tentative de connexion à ${tv.ipAddress}")
        _state.update {
            it.copy(
                isLoading = true,
                snackbarMessage = "Connexion à ${tv.name ?: tv.ipAddress}..."
            )
        }

        connectionJob = currentScope.launch {
            try {
                connectToTvUseCase(tv.ipAddress)
            } catch (e: Exception) {
                Timber.e(e, "RemoteManager : Échec de connexion à ${tv.ipAddress}")
                _state.update {
                    it.copy(
                        snackbarMessage = "Échec de connexion à ${tv.name ?: tv.ipAddress}.",
                        isLoading = false,
                        isConnected = false
                    )
                }
            } finally {
                connectionJob = null
            }
        }
    }

    fun disconnect() {
        val currentScope = managerScope ?: run {
            Timber.e("RemoteManager : managerScope est nul, impossible de lancer la déconnexion.")
            return
        }

        connectionJob?.cancel()
        connectionJob = null
        Timber.i("RemoteManager : Déconnexion demandée.")
        _state.update { it.copy(isLoading = true) }

        currentScope.launch {
            try {
                disconnectFromTvUseCase()
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
            if (activeTvInfo != null) {
                connect()
            }
            return
        }

        scope.launch {
            try {
                sendCommandUseCase(keyCode, action)
                Timber.d("RemoteManager : Commande ${keyCode.name} envoyée.")
            } catch (e: Exception) {
                Timber.e(e, "RemoteManager : Erreur lors de l'envoi de la commande ${keyCode.name}")
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
                launchAppUseCase(appLink)
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
        Timber.d("RemoteManager : Nettoyage des ressources.")
        connectionJob?.cancel()
        activeTvJob?.cancel()
        connectionStateJob?.cancel()
        remoteEventsJob?.cancel()
        connectionJob = null
        managerScope = null
    }
}
