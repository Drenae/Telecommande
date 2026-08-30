package com.telecommande.core

import com.telecommande.core.event.AndroidTvEvent
import com.telecommande.core.event.PairingEvent
import com.telecommande.core.event.RemoteEvent
import com.telecommande.core.pairing.PairingSession
import com.telecommande.core.remote.RemoteSession
import com.telecommande.core.remote.Remotemessage
import com.telecommande.core.ssl.KeyStoreManager
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID

class AndroidRemoteTv : BaseAndroidRemoteTv() {
    private val _eventFlow = MutableSharedFlow<AndroidTvEvent>(
        replay = 0,
        extraBufferCapacity = 16
    )
    val eventFlow = _eventFlow.asSharedFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected = _isConnected.asStateFlow()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Timber.e(throwable, "Erreur non interceptée dans une coroutine de AndroidRemoteTv")
        _eventFlow.tryEmit(
            AndroidTvEvent.Error(
                "Erreur interne inattendue: ${throwable.message ?: "Cause inconnue"}"
            )
        )
        _isConnected.value = false
        clearConnectionTarget()
    }

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob() + exceptionHandler)

    private var currentPairingSession: PairingSession? = null
    private var currentRemoteSession: RemoteSession? = null

    private var pairingCollectorJob: Job? = null
    private var remoteCollectorJob: Job? = null

    private val connectionTargetLock = Any()
    private var currentTargetHost: String? = null
    private var connectionAttemptInProgress = false

    private val keyStoreManager: KeyStoreManager by lazy {
        KeyStoreManager()
    }

    fun connect(host: String, expectedTvKeystoreAlias: String? = null) {
        val shouldStart = synchronized(connectionTargetLock) {
            val sameTarget = currentTargetHost == host
            val alreadyBusyWithSameTarget = sameTarget && (
                connectionAttemptInProgress ||
                    _isConnected.value ||
                    currentPairingSession != null ||
                    currentRemoteSession != null
                )

            if (alreadyBusyWithSameTarget) {
                false
            } else {
                currentTargetHost = host
                connectionAttemptInProgress = true
                true
            }
        }

        if (!shouldStart) {
            Timber.i(
                "AndroidRemoteTv: connexion vers %s ignorée car une connexion/session vers cette TV est déjà active.",
                host
            )
            return
        }

        Timber.i("AndroidRemoteTv: Tentative de connexion à l'hôte : %s", host)
        cleanupPreviousSessions()
        _isConnected.value = false

        coroutineScope.launch {
            _eventFlow.emit(AndroidTvEvent.ConnectingToRemote)
            try {
                val canConnectSecurely =
                    !expectedTvKeystoreAlias.isNullOrBlank() &&
                        keyStoreManager.hasServerIdentityAlias() &&
                        keyStoreManager.hasRemoteCertificate(expectedTvKeystoreAlias)

                if (canConnectSecurely) {
                    Timber.i("Certificat connu pour %s, connexion distante sécurisée.", host)
                    initializeAndConnectRemoteSession(host, expectedTvKeystoreAlias!!)
                } else {
                    Timber.i("Aucun certificat correspondant à la TV sélectionnée, lancement de l'appairage avec %s:6467", host)
                    initializeAndPairSession(host)
                }
            } catch (e: Exception) {
                Timber.e(e, "Échec global de la tentative de connexion ou d'appairage à %s", host)
                _eventFlow.emit(
                    AndroidTvEvent.Error(
                        "Échec de la connexion/appairage: ${e.message ?: "Cause inconnue"}"
                    )
                )
                _isConnected.value = false
                clearConnectionTargetIf(host)
            }
        }
    }

    private suspend fun initializeAndConnectRemoteSession(
        host: String,
        expectedTvKeystoreAlias: String,
        port: Int = 6466
    ) {
        Timber.tag("AndroidRemoteTv").d("Initialisation et connexion sécurisée pour %s:%d", host, port)
        cleanupRemoteSession()

        val newSession = RemoteSession(host, port, expectedTvKeystoreAlias)
        currentRemoteSession = newSession

        remoteCollectorJob = coroutineScope.launch {
            newSession.eventFlow.collect { event ->
                if (currentRemoteSession !== newSession) {
                    return@collect
                }

                when (event) {
                    is RemoteEvent.Connected -> {
                        Timber.i("RemoteSession sécurisée connectée à %s.", host)
                        _isConnected.value = true
                        synchronized(connectionTargetLock) {
                            if (currentTargetHost == host) {
                                connectionAttemptInProgress = false
                            }
                        }
                        _eventFlow.emit(AndroidTvEvent.Connected)
                    }

                    is RemoteEvent.SslError -> {
                        Timber.e("Validation SSL échouée pour %s. Réappairage nécessaire.", host)
                        _isConnected.value = false
                        cleanupRemoteSession()

                        keyStoreManager.removeRemoteCertificate(expectedTvKeystoreAlias)
                        _eventFlow.emit(
                            AndroidTvEvent.Error(
                                "Le certificat de la TV a changé ou n'est plus valide. Nouvel appairage requis."
                            )
                        )
                        initializeAndPairSession(host)
                    }

                    is RemoteEvent.Disconnected -> {
                        if (currentRemoteSession === newSession) {
                            val wasConnected = _isConnected.value
                            _isConnected.value = false
                            if (wasConnected) {
                                _eventFlow.emit(AndroidTvEvent.Disconnected)
                            }
                            cleanupRemoteSession()
                            clearConnectionTargetIf(host)
                        }
                    }

                    is RemoteEvent.Error -> {
                        if (currentRemoteSession === newSession) {
                            val wasConnected = _isConnected.value
                            _isConnected.value = false
                            if (wasConnected) {
                                _eventFlow.emit(AndroidTvEvent.Disconnected)
                            }
                            _eventFlow.emit(AndroidTvEvent.Error("Erreur Remote Session: ${event.message}"))
                            cleanupRemoteSession()
                            clearConnectionTargetIf(host)
                        }
                    }

                    is RemoteEvent.VolumeStateChanged -> {
                        _eventFlow.emit(
                            AndroidTvEvent.VolumeUpdated(event.level, event.max, event.muted)
                        )
                    }
                }
            }
        }

        newSession.connect()
    }

    private suspend fun initializeAndPairSession(host: String, pairingPort: Int = 6467) {
        Timber.d("Initialisation et démarrage de PairingSession pour %s:%d", host, pairingPort)
        cleanupPairingSession()

        val session = PairingSession()
        currentPairingSession = session

        pairingCollectorJob = coroutineScope.launch {
            session.eventFlow.collect { event ->
                if (currentPairingSession !== session && event !is PairingEvent.SessionEnded) {
                    return@collect
                }

                when (event) {
                    is PairingEvent.SessionCreated -> {
                        _eventFlow.emit(AndroidTvEvent.SessionCreated)
                    }

                    is PairingEvent.SecretRequested -> {
                        _eventFlow.emit(AndroidTvEvent.SecretRequested)
                    }

                    is PairingEvent.Paired -> {
                        if (event.serverCertificate != null) {
                            val tvKeystoreAlias = UUID.randomUUID().toString()
                            try {
                                keyStoreManager.storeRemoteCertificate(
                                    event.serverCertificate,
                                    tvKeystoreAlias
                                )
                                _eventFlow.emit(AndroidTvEvent.Paired(host, tvKeystoreAlias))

                                if (currentPairingSession === session) {
                                    session.closeSocket()
                                    currentPairingSession = null
                                }

                                _eventFlow.emit(AndroidTvEvent.ConnectingToRemote)
                                initializeAndConnectRemoteSession(host, tvKeystoreAlias)
                            } catch (e: Exception) {
                                Timber.e(e, "Échec du stockage du certificat TV pour %s après appairage.", host)
                                _eventFlow.emit(
                                    AndroidTvEvent.Error(
                                        "Échec du stockage du certificat après appairage: ${e.message}"
                                    )
                                )
                                clearConnectionTargetIf(host)
                            }
                        } else {
                            _eventFlow.emit(
                                AndroidTvEvent.Error(
                                    "Appairage réussi mais certificat TV manquant."
                                )
                            )
                            clearConnectionTargetIf(host)
                        }

                        pairingCollectorJob?.cancel()
                        pairingCollectorJob = null
                    }

                    is PairingEvent.SessionEnded -> {
                        if (currentPairingSession === session) {
                            cleanupPairingSession()
                        }
                    }

                    is PairingEvent.Error -> {
                        _eventFlow.emit(
                            AndroidTvEvent.Error("Erreur d'appairage: ${event.message}")
                        )
                        if (currentPairingSession === session) {
                            cleanupPairingSession()
                        }
                        clearConnectionTargetIf(host)
                    }

                    is PairingEvent.Log -> Unit
                }
            }
        }

        session.pair(host, pairingPort)
    }

    fun sendCommand(
        remoteKeyCode: Remotemessage.RemoteKeyCode,
        remoteDirection: Remotemessage.RemoteDirection
    ) {
        val session = currentRemoteSession
        if (session == null || !_isConnected.value) {
            _eventFlow.tryEmit(
                AndroidTvEvent.Error("Impossible d'envoyer la commande: session non connectée")
            )
            return
        }

        coroutineScope.launch {
            try {
                session.sendCommand(remoteKeyCode, remoteDirection)
            } catch (e: Exception) {
                _eventFlow.emit(
                    AndroidTvEvent.Error("Erreur lors de l'envoi de la commande: ${e.message}")
                )
            }
        }
    }

    fun launchApplication(appLink: String) {
        val session = currentRemoteSession
        if (session == null || !_isConnected.value) {
            _eventFlow.tryEmit(
                AndroidTvEvent.Error("Impossible de lancer l'application: session non connectée")
            )
            return
        }

        if (appLink.isBlank()) {
            _eventFlow.tryEmit(
                AndroidTvEvent.Error("Lien d'application non fourni pour le lancement.")
            )
            return
        }

        coroutineScope.launch {
            try {
                session.sendAppLinkLaunchRequest(appLink)
                _eventFlow.emit(AndroidTvEvent.AppLinkLaunchSent(appLink))
            } catch (e: Exception) {
                _eventFlow.emit(
                    AndroidTvEvent.Error(
                        "Erreur lors du lancement de l'application: ${e.message ?: "Cause inconnue"}"
                    )
                )
            }
        }
    }

    fun sendSecret(code: String) {
        val session = currentPairingSession
        if (session == null) {
            _eventFlow.tryEmit(
                AndroidTvEvent.Error(
                    "Impossible d'envoyer le secret: session d'appairage non active."
                )
            )
            return
        }

        coroutineScope.launch {
            try {
                session.provideSecret(code)
            } catch (e: Exception) {
                _eventFlow.emit(
                    AndroidTvEvent.Error("Erreur lors de l'envoi du secret: ${e.message}")
                )
            }
        }
    }

    private fun cleanupPreviousSessions() {
        cleanupPairingSession()
        cleanupRemoteSession()
    }

    private fun cleanupPairingSession() {
        pairingCollectorJob?.cancel()
        pairingCollectorJob = null
        currentPairingSession?.closeSocket()
        currentPairingSession = null
    }

    private fun cleanupRemoteSession() {
        remoteCollectorJob?.cancel()
        remoteCollectorJob = null
        currentRemoteSession?.close()
        currentRemoteSession = null
    }

    private fun clearConnectionTargetIf(host: String) {
        synchronized(connectionTargetLock) {
            if (currentTargetHost == host) {
                currentTargetHost = null
                connectionAttemptInProgress = false
            }
        }
    }

    private fun clearConnectionTarget() {
        synchronized(connectionTargetLock) {
            currentTargetHost = null
            connectionAttemptInProgress = false
        }
    }

    fun disconnect() {
        val wasConnected = _isConnected.value
        _isConnected.value = false
        cleanupPreviousSessions()
        clearConnectionTarget()

        if (wasConnected) {
            _eventFlow.tryEmit(AndroidTvEvent.Disconnected)
        }
    }

    fun cleanup() {
        disconnect()
        coroutineScope.cancel()
    }
}
