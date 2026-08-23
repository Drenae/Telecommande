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
    }

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob() + exceptionHandler)

    private var currentPairingSession: PairingSession? = null
    private var currentRemoteSession: RemoteSession? = null

    private var pairingCollectorJob: Job? = null
    private var remoteCollectorJob: Job? = null

    private val keyStoreManager: KeyStoreManager by lazy {
        KeyStoreManager()
    }

    fun connect(host: String) {
        Timber.i("AndroidRemoteTv: Tentative de connexion à l'hôte : %s", host)
        cleanupPreviousSessions()
        _isConnected.value = false

        coroutineScope.launch {
            _eventFlow.emit(AndroidTvEvent.ConnectingToRemote)
            try {
                if (
                    androidRemoteContext.keyStoreFile.exists() &&
                    androidRemoteContext.keyStoreFile.length() > 0 &&
                    keyStoreManager.hasServerIdentityAlias()
                ) {
                    Timber.i(
                        "Keystore trouvé et identité serveur présente, tentative de connexion directe à distance à %s:6466",
                        host
                    )
                    initializeAndConnectRemoteSession(host)
                } else {
                    Timber.i(
                        "Aucun Keystore valide trouvé ou identité serveur manquante, lancement de l'appairage avec %s:6467",
                        host
                    )
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
            }
        }
    }

    private suspend fun initializeAndConnectRemoteSession(host: String, port: Int = 6466) {
        Timber.tag("AndroidRemoteTv").d(
            "Initialisation et connexion de RemoteSession pour %s:%d",
            host,
            port
        )

        cleanupRemoteSession()

        val newSession = RemoteSession(host, port)
        currentRemoteSession = newSession
        Timber.tag("AndroidRemoteTv").d("Nouvelle RemoteSession créée: %s", newSession.hashCode())

        remoteCollectorJob = coroutineScope.launch {
            Timber.tag("AndroidRemoteTv").d(
                "Lancement de la collecte pour RemoteSession: %s",
                newSession.hashCode()
            )
            newSession.eventFlow.collect { event ->
                if (currentRemoteSession !== newSession) {
                    Timber.tag("AndroidRemoteTv").w(
                        "Événement RemoteSession reçu pour une session obsolète (%s), ignoré. Session actuelle: %s",
                        newSession.hashCode(),
                        currentRemoteSession?.hashCode()
                    )
                    return@collect
                }

                Timber.tag("AndroidRemoteTv_RemoteEvent").d(
                    "Reçu: %s (Session: %s)",
                    event,
                    newSession.hashCode()
                )

                when (event) {
                    is RemoteEvent.Connected -> {
                        Timber.tag("AndroidRemoteTv").i(
                            "RemoteSession %s connectée à %s.",
                            newSession.hashCode(),
                            host
                        )
                        _isConnected.value = true
                        _eventFlow.emit(AndroidTvEvent.Connected)
                    }

                    is RemoteEvent.SslError -> {
                        Timber.tag("AndroidRemoteTv").e(
                            "Erreur SSL pour RemoteSession %s. Tentative d'appairage.",
                            newSession.hashCode()
                        )
                        _isConnected.value = false
                        _eventFlow.emit(
                            AndroidTvEvent.Error("Erreur SSL. Le Keystore pourrait être invalide.")
                        )
                        cleanupRemoteSession()
                        try {
                            androidRemoteContext.keyStoreFile.delete()
                            Timber.i(
                                "Keystore supprimé en raison d'une erreur SSL. Lancement de l'appairage."
                            )
                            initializeAndPairSession(host)
                        } catch (secEx: SecurityException) {
                            Timber.e(
                                secEx,
                                "Impossible de supprimer le keystore après une erreur SSL."
                            )
                            _eventFlow.emit(
                                AndroidTvEvent.Error(
                                    "Impossible de supprimer le keystore. Appairage impossible."
                                )
                            )
                        }
                    }

                    is RemoteEvent.Disconnected -> {
                        Timber.tag("AndroidRemoteTv").i(
                            "RemoteSession %s déconnectée de %s.",
                            newSession.hashCode(),
                            host
                        )
                        if (currentRemoteSession === newSession) {
                            val wasConnected = _isConnected.value
                            _isConnected.value = false
                            if (wasConnected) {
                                _eventFlow.emit(AndroidTvEvent.Disconnected)
                            }
                            cleanupRemoteSession()
                        }
                    }

                    is RemoteEvent.Error -> {
                        Timber.tag("AndroidRemoteTv").e(
                            "Erreur RemoteSession %s pour %s: %s",
                            newSession.hashCode(),
                            host,
                            event.message
                        )
                        if (currentRemoteSession === newSession) {
                            val wasConnected = _isConnected.value
                            _isConnected.value = false
                            if (wasConnected) {
                                _eventFlow.emit(AndroidTvEvent.Disconnected)
                            }
                            _eventFlow.emit(
                                AndroidTvEvent.Error("Erreur Remote Session: ${event.message}")
                            )
                            cleanupRemoteSession()
                        }
                    }

                    is RemoteEvent.VolumeStateChanged -> {
                        Timber.i(
                            "État du volume mis à jour : Niveau=%d, Max=%d, Muet=%b, Appareil=%s",
                            event.level,
                            event.max,
                            event.muted,
                            event.deviceName ?: "N/A"
                        )
                        _eventFlow.emit(
                            AndroidTvEvent.VolumeUpdated(event.level, event.max, event.muted)
                        )
                    }
                }
            }
        }

        Timber.tag("AndroidRemoteTv").d(
            "Appel de connect() sur RemoteSession: %s",
            newSession.hashCode()
        )
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
                    Timber.d("Événement reçu pour une PairingSession obsolète, ignoré: %s", event)
                    return@collect
                }

                Timber.v("Événement PairingSession reçu: %s", event)
                when (event) {
                    is PairingEvent.SessionCreated -> {
                        Timber.d("Session d'appairage créée avec %s.", host)
                        _eventFlow.emit(AndroidTvEvent.SessionCreated)
                    }

                    is PairingEvent.SecretRequested -> {
                        Timber.d("Secret demandé par %s pour l'appairage.", host)
                        _eventFlow.emit(AndroidTvEvent.SecretRequested)
                    }

                    is PairingEvent.Paired -> {
                        if (event.serverCertificate != null) {
                            val tvKeystoreAlias = UUID.randomUUID().toString()
                            Timber.i(
                                "Appairage réussi avec %s. Certificat TV reçu. Alias Keystore généré: %s",
                                host,
                                tvKeystoreAlias
                            )
                            try {
                                keyStoreManager.storeRemoteCertificate(
                                    event.serverCertificate,
                                    tvKeystoreAlias
                                )
                                Timber.i(
                                    "Certificat TV pour %s stocké avec l'alias: %s",
                                    host,
                                    tvKeystoreAlias
                                )
                                _eventFlow.emit(AndroidTvEvent.Paired(host, tvKeystoreAlias))
                            } catch (e: Exception) {
                                Timber.e(
                                    e,
                                    "Échec du stockage du certificat TV pour %s après appairage.",
                                    host
                                )
                                _eventFlow.emit(
                                    AndroidTvEvent.Error(
                                        "Échec du stockage du certificat après appairage: ${e.message}"
                                    )
                                )
                            }
                        } else {
                            Timber.w(
                                "Appairage réussi avec %s, mais aucun certificat serveur n'a été fourni dans l'événement.",
                                host
                            )
                            _eventFlow.emit(
                                AndroidTvEvent.Error(
                                    "Appairage réussi mais certificat TV manquant."
                                )
                            )
                        }

                        if (currentPairingSession === session) {
                            session.closeSocket()
                            currentPairingSession = null
                        }

                        _eventFlow.emit(AndroidTvEvent.ConnectingToRemote)
                        initializeAndConnectRemoteSession(host)

                        pairingCollectorJob?.cancel()
                        pairingCollectorJob = null
                    }

                    is PairingEvent.SessionEnded -> {
                        Timber.d("Session d'appairage terminée avec %s.", host)
                        if (currentPairingSession === session) {
                            cleanupPairingSession()
                        }
                    }

                    is PairingEvent.Error -> {
                        Timber.e(
                            "Erreur d'appairage pour %s:%d: %s",
                            host,
                            pairingPort,
                            event.message
                        )
                        _eventFlow.emit(
                            AndroidTvEvent.Error("Erreur d'appairage: ${event.message}")
                        )
                        if (currentPairingSession === session) {
                            cleanupPairingSession()
                        }
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
            Timber.w("Impossible d'envoyer la commande, RemoteSession est nulle ou non connectée.")
            _eventFlow.tryEmit(
                AndroidTvEvent.Error("Impossible d'envoyer la commande: session non connectée")
            )
            return
        }

        coroutineScope.launch {
            try {
                session.sendCommand(remoteKeyCode, remoteDirection)
                Timber.d(
                    "Commande envoyée via RemoteSession: %s, %s",
                    remoteKeyCode,
                    remoteDirection
                )
            } catch (e: Exception) {
                Timber.w(e, "Impossible d'envoyer la commande")
                _eventFlow.emit(
                    AndroidTvEvent.Error("Erreur lors de l'envoi de la commande: ${e.message}")
                )
            }
        }
    }

    fun launchApplication(appLink: String) {
        val session = currentRemoteSession
        if (session == null || !_isConnected.value) {
            Timber.w("Impossible de lancer l'application, RemoteSession est nulle ou non connectée.")
            _eventFlow.tryEmit(
                AndroidTvEvent.Error("Impossible de lancer l'application: session non connectée")
            )
            return
        }

        if (appLink.isBlank()) {
            Timber.w("Tentative de lancement d'une application avec un appLink vide.")
            _eventFlow.tryEmit(
                AndroidTvEvent.Error("Lien d'application non fourni pour le lancement.")
            )
            return
        }

        Timber.i("Demande de lancement de l'application avec le lien : %s", appLink)
        coroutineScope.launch {
            try {
                session.sendAppLinkLaunchRequest(appLink)
                _eventFlow.emit(AndroidTvEvent.AppLinkLaunchSent(appLink))
            } catch (e: Exception) {
                Timber.w(
                    e,
                    "Erreur interceptée par AndroidRemoteTv lors de l'appel à session.sendAppLinkLaunchRequest"
                )
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
            Timber.w("Impossible d'envoyer le secret, PairingSession est nulle.")
            _eventFlow.tryEmit(
                AndroidTvEvent.Error(
                    "Impossible d'envoyer le secret: session d'appairage non active."
                )
            )
            return
        }

        coroutineScope.launch {
            try {
                Timber.d("Appel de PairingSession.provideSecret avec le code.")
                session.provideSecret(code)
            } catch (e: Exception) {
                Timber.w(e, "Impossible d'envoyer le secret")
                _eventFlow.emit(
                    AndroidTvEvent.Error("Erreur lors de l'envoi du secret: ${e.message}")
                )
            }
        }
    }

    private fun cleanupPreviousSessions() {
        Timber.d("Nettoyage des sessions précédentes (Pairing et Remote).")
        cleanupPairingSession()
        cleanupRemoteSession()
    }

    private fun cleanupPairingSession() {
        pairingCollectorJob?.cancel()
        pairingCollectorJob = null
        currentPairingSession?.closeSocket()
        currentPairingSession = null
        Timber.d("PairingSession nettoyée.")
    }

    private fun cleanupRemoteSession() {
        Timber.tag("AndroidRemoteTv").d(
            "cleanupRemoteSession appelée. Job actuel: %s, Session actuelle: %s",
            remoteCollectorJob?.hashCode(),
            currentRemoteSession?.hashCode()
        )
        remoteCollectorJob?.cancel()
        remoteCollectorJob = null
        currentRemoteSession?.close()
        currentRemoteSession = null
        Timber.tag("AndroidRemoteTv").d("RemoteSession et son job de collecte nettoyés.")
    }

    fun disconnect() {
        Timber.i("AndroidRemoteTv.disconnect() appelé.")
        val wasConnected = _isConnected.value
        _isConnected.value = false
        cleanupPreviousSessions()

        if (wasConnected) {
            _eventFlow.tryEmit(AndroidTvEvent.Disconnected)
            Timber.i("Événement Disconnected émis après déconnexion manuelle.")
        }
    }

    fun cleanup() {
        Timber.i("Nettoyage complet de AndroidRemoteTv.")
        disconnect()
        coroutineScope.cancel()
        Timber.i("CoroutineScope de AndroidRemoteTv annulé.")
    }
}
