package com.telecommande.core.remote

import com.telecommande.core.event.RemoteEvent
import com.telecommande.core.exception.PairingException
import com.telecommande.core.ssl.DummyTrustManager
import com.telecommande.core.ssl.KeyStoreManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.security.GeneralSecurityException
import java.security.SecureRandom
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLException
import javax.net.ssl.SSLSocket
import javax.net.ssl.TrustManager

class RemoteSession(
    private val host: String,
    private val port: Int
) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var connectionJob: Job? = null
    private var packetParserJob: Job? = null

    private val incomingMessagesChannel = Channel<Remotemessage.RemoteMessage>(Channel.BUFFERED)
    private val remoteMessageManager: RemoteMessageManager = RemoteMessageManager()

    private var sslSocket: SSLSocket? = null
    private var outputStream: OutputStream? = null
    private var inputStream: InputStream? = null

    private val _eventFlow = MutableSharedFlow<RemoteEvent>(replay = 1)
    val eventFlow = _eventFlow.asSharedFlow()

    private var retryCount: Int = 0

    suspend fun connect() {
        Timber.i("Tentative de connexion à distance à %s:%d", host, port)
        if (connectionJob?.isActive == true) {
            Timber.w("Une session de connexion à distance est déjà active ou en cours, annulation de la précédente.")
            connectionJob?.cancel()
            packetParserJob?.cancel()
        }

        connectionJob = coroutineScope.launch {
            try {
                initializeSslSocketAndStreams()
                startRemotePacketParser()
                Timber.d("Socket SSL et parseur initialisés pour la session distante.")

                val firstMessage = waitForMessageOrFail("Attente du premier message de configuration distante")
                Timber.i("Premier message distant reçu: %s", firstMessage.toString().take(200))

                outputStream?.let { out ->
                    val remoteConfigure = remoteMessageManager.createRemoteConfigure(
                        622,
                        "RefactoredClient",
                        "KotlinClient",
                        1,
                        "1"
                    )
                    out.write(remoteConfigure)
                    Timber.d("Message RemoteConfigure envoyé.")

                    val configureAck = waitForMessageOrFail("Attente de l'ack de RemoteConfigure")
                    Timber.i("Ack de RemoteConfigure reçu: %s", configureAck.toString().take(200))

                    val remoteActive = remoteMessageManager.createRemoteActive(622)
                    out.write(remoteActive)
                    Timber.d("Message RemoteActive envoyé.")

                } ?: throw IOException("OutputStream non initialisé avant la configuration distante.")

            } catch (e: SSLException) {
                Timber.e(e, "SSLException durant la connexion distante à %s:%d : %s", host, port, e.message)
                _eventFlow.emit(RemoteEvent.SslError)
                closeSocketInternal()
                _eventFlow.emit(RemoteEvent.Disconnected)
            } catch (e: IOException) {
                Timber.e(e, "IOException durant la connexion distante à %s:%d : %s", host, port, e.message)
                _eventFlow.emit(RemoteEvent.Error("Erreur d'E/S: ${e.message}"))
                closeSocketInternal()
                _eventFlow.emit(RemoteEvent.Disconnected)
            } catch (e: GeneralSecurityException) {
                Timber.e(e, "GeneralSecurityException durant la connexion distante à %s:%d : %s", host, port, e.message)
                _eventFlow.emit(RemoteEvent.Error("Erreur de sécurité: ${e.message}"))
                closeSocketInternal()
            } catch (e: PairingException) {
                Timber.e(e, "PairingException (inattendue) durant la connexion distante à %s:%d : %s", host, port, e.message)
                _eventFlow.emit(RemoteEvent.Error("Erreur inattendue de type appairage: ${e.message}"))
                closeSocketInternal()
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) {
                    Timber.i("Connexion distante annulée pour %s:%d", host, port)
                } else {
                    Timber.e(e, "Erreur inattendue durant la connexion distante à %s:%d : %s", host, port, e.message)
                    _eventFlow.emit(RemoteEvent.Error("Erreur Inattendue: ${e.message}"))
                    closeSocketInternal()
                    _eventFlow.emit(RemoteEvent.Disconnected)
                }
            }
        }
    }

    private suspend fun initializeSslSocketAndStreams() {
        withContext(Dispatchers.IO) {
            Timber.d("Initialisation du socket SSL distant pour %s:%d", host, port)
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(KeyStoreManager().getKeyManagers(), arrayOf<TrustManager>(DummyTrustManager()), SecureRandom())
            val sslSocketFactory = sslContext.socketFactory
            val newSocket = sslSocketFactory.createSocket(host, port) as SSLSocket

            newSocket.needClientAuth = true
            newSocket.useClientMode = true
            newSocket.keepAlive = true
            newSocket.tcpNoDelay = true

            Timber.d("Démarrage du handshake SSL...")
            newSocket.startHandshake()
            Timber.i("Handshake SSL distant réussi pour %s:%d.", host, port)

            sslSocket = newSocket
            outputStream = newSocket.outputStream
            inputStream = newSocket.inputStream

            if (outputStream == null) {
                Timber.e("Échec de l'obtention de l'outputStream même si le socket SSL distant n'est pas nul.")
                throw IOException("Échec de l'initialisation de l'outputStream distant.")
            }
            Timber.d("Socket SSL distant et flux initialisés.")
        }
    }

    private fun startRemotePacketParser() {
        val currentInputStream = inputStream
        val currentOutputStream = outputStream

        if (currentInputStream == null || currentOutputStream == null) {
            Timber.e("InputStream ou OutputStream distant est nul, impossible de démarrer RemotePacketParser.")
            coroutineScope.launch { _eventFlow.emit(RemoteEvent.Error("Erreur interne: Flux non disponibles pour l'analyseur distant.")) }
            return
        }

        packetParserJob?.cancel()
        packetParserJob = coroutineScope.launch {
            Timber.d("Lancement de RemotePacketParser.parsePackets sur le thread %s", Thread.currentThread().name)
            try {
                val remotePacketParser = RemotePacketParser(
                    currentInputStream,
                    currentOutputStream,
                    incomingMessagesChannel,
                    _eventFlow
                )
                remotePacketParser.parsePackets()
            } catch (e: IOException) {
                if (isActive) {
                    Timber.e(e, "IOException dans RemotePacketParser: %s", e.message)
                    _eventFlow.emit(RemoteEvent.Error("Erreur de lecture réseau distante: ${e.message}"))
                    _eventFlow.emit(RemoteEvent.Disconnected)
                } else {
                    Timber.d("IOException dans RemotePacketParser après annulation, ignorée: %s", e.message)
                }
            } catch (e: Exception) {
                if (isActive) {
                    Timber.e(e, "Erreur inattendue dans RemotePacketParser: %s", e.message)
                    _eventFlow.emit(RemoteEvent.Error("Erreur interne de l'analyseur distant: ${e.message}"))
                    _eventFlow.emit(RemoteEvent.Disconnected)
                }
            } finally {
                Timber.d("Coroutine de RemotePacketParser.parsePackets terminée.")
                if (isActive) {
                    _eventFlow.emit(RemoteEvent.Disconnected)
                }
            }
        }
    }

    private suspend fun waitForMessageOrFail(contextMessage: String, timeoutMillis: Long = 30000): Remotemessage.RemoteMessage {
        Timber.v("Attente d'un message distant depuis le canal (%s)...", contextMessage)
        return withTimeoutOrNull(timeoutMillis) {
            incomingMessagesChannel.receive()
        } ?: throw PairingException("Timeout en attente d'un message distant: $contextMessage")
    }

    fun attemptToReconnect() {
        retryCount++
        Timber.i("Tentative de reconnexion #%d", retryCount)
        coroutineScope.launch {
            closeSocketInternal()
            connect()
        }
    }

    suspend fun sendCommand(remoteKeyCode: Remotemessage.RemoteKeyCode, remoteDirection: Remotemessage.RemoteDirection) {
        val currentOutStream = outputStream
        if (currentOutStream == null || sslSocket?.isClosed == true || sslSocket?.isConnected == false) {
            Timber.w("Impossible d'envoyer la commande, outputStream est nul ou socket non connecté.")
            _eventFlow.emit(RemoteEvent.Error("Impossible d'envoyer la commande: session non connectée."))
            return
        }

        try {
            withContext(Dispatchers.IO) {
                currentOutStream.write(remoteMessageManager.createKeyCommand(remoteKeyCode, remoteDirection))
                Timber.d("Commande envoyée: %s, %s", remoteKeyCode, remoteDirection)
            }
        } catch (e: IOException) {
            Timber.e(e, "IOException lors de l'envoi de la commande: %s", e.message)
            _eventFlow.emit(RemoteEvent.Error("Erreur d'E/S lors de l'envoi de la commande: ${e.message}"))
            closeSocketInternal()
            _eventFlow.emit(RemoteEvent.Disconnected)
        }
    }

    suspend fun sendAppLinkLaunchRequest(appLink: String) {
        val currentOutStream = outputStream
        if (currentOutStream == null || sslSocket?.isClosed == true || sslSocket?.isConnected == false) {
            Timber.w("Impossible d'envoyer la requête de lancement d'application, outputStream est nul ou socket non connecté.")
            _eventFlow.emit(RemoteEvent.Error("Impossible d'envoyer la requête de lancement d'app: session non connectée."))
            return
        }

        if (appLink.isBlank()) {
            Timber.w("Tentative d'envoi d'une requête de lancement d'application avec un appLink vide. Ignoré.")
            _eventFlow.emit(RemoteEvent.Error("Lien d'application (appLink) non fourni."))
            return
        }

        try {
            withContext(Dispatchers.IO) {
                Timber.i("Envoi de la requête de lancement d'application pour : %s", appLink)
                currentOutStream.write(remoteMessageManager.createAppLinkLaunchRequest(appLink))
                Timber.d("Requête RemoteAppLinkLaunchRequest envoyée pour: %s", appLink)
            }
        } catch (e: IOException) {
            Timber.e(e, "IOException lors de l'envoi de la requête de lancement d'application: %s", e.message)
            _eventFlow.emit(RemoteEvent.Error("Erreur d'E/S lors du lancement de l'app: ${e.message}"))
        } catch (e: Exception) {
            Timber.e(e, "Erreur inattendue lors de l'envoi de la requête de lancement d'application: %s", e.message)
            _eventFlow.emit(RemoteEvent.Error("Erreur inattendue lors du lancement de l'app: ${e.message}"))
        }
    }

    private suspend fun closeSocketInternal() {
        Timber.d("Fermeture interne du socket et des flux distants.")
        packetParserJob?.cancel()

        withContext(Dispatchers.IO) {
            outputStream?.closeCatching()
            inputStream?.closeCatching()
            sslSocket?.closeCatching()
        }
        outputStream = null
        inputStream = null
        sslSocket = null
        Timber.d("Socket et flux distants nettoyés.")
    }

    fun close() {
        Timber.i("Fermeture demandée pour RemoteSession.")
        connectionJob?.cancel()
        packetParserJob?.cancel()

        coroutineScope.launch(Dispatchers.IO) {
            closeSocketInternal()
            _eventFlow.emit(RemoteEvent.Disconnected)
            Timber.i("RemoteSession fermée et événement de déconnexion émis.")
        }
    }

    fun cleanup() {
        Timber.i("Nettoyage complet de RemoteSession.")
        close()
        coroutineScope.cancel()
        Timber.i("CoroutineScope de RemoteSession annulé.")
    }

    fun isConnected(): Boolean {
        return sslSocket?.isConnected == true && sslSocket?.isClosed == false && outputStream != null
    }
}

private fun java.io.Closeable.closeCatching() {
    try {
        close()
    } catch (e: IOException) {
        Timber.w(e, "IOException lors de la fermeture de %s (ignorée).", this.javaClass.simpleName)
    }
}