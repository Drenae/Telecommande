package com.telecommande.core.pairing

import com.telecommande.core.AndroidRemoteContext
import com.telecommande.core.event.PairingEvent
import com.telecommande.core.exception.PairingException
import com.telecommande.core.ssl.DummyTrustManager
import com.telecommande.core.ssl.KeyStoreManager
import com.telecommande.core.util.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
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
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.TrustManager

class PairingSession {

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var pairingJob: Job? = null
    private var packetParserJob: Job? = null

    private val pairingMessageManager: PairingMessageManager = PairingMessageManager()
    private val incomingMessagesChannel = Channel<Pairingmessage.PairingMessage>(Channel.BUFFERED)
    private val secretChannel = Channel<String>(Channel.RENDEZVOUS)

    private var sslSocket: SSLSocket? = null
    private var outputStream: OutputStream? = null
    private var inputStream: InputStream? = null

    private val _eventFlow = MutableSharedFlow<PairingEvent>(replay = 1)
    val eventFlow = _eventFlow.asSharedFlow()

    private val androidRemoteContext = AndroidRemoteContext.getInstance()

    suspend fun pair(host: String, port: Int) {
        Timber.i("Tentative d'appairage avec %s:%d", host, port)
        if (pairingJob?.isActive == true) {
            Timber.w("Une session d'appairage est déjà active, annulation de la précédente.")
            pairingJob?.cancel()
        }

        pairingJob = coroutineScope.launch {
            try {
                _eventFlow.emit(PairingEvent.Log("Initialisation de la connexion SSL pour l'appairage."))
                initializeSslSocket(host, port)
                _eventFlow.emit(PairingEvent.SessionCreated)
                Timber.d("Session d'appairage créée, démarrage de l'analyseur de paquets.")

                startPacketParser()

                outputStream?.let { out ->
                    Timber.d("Envoi du message d'appairage initial...")
                    val pairingMessageBytes = pairingMessageManager.createPairingMessage(
                        androidRemoteContext.clientName,
                        androidRemoteContext.serviceName
                    )
                    out.write(pairingMessageBytes)
                    logSendMessage("Message d'appairage initial")
                    val pairingMessageResponse = waitForMessageOrFail()
                    logReceivedMessage("Réponse au message d'appairage: ${pairingMessageResponse.toString().take(200)}")

                    Timber.d("Envoi des options d'appairage...")
                    val pairingOptionBytes = pairingMessageManager.createPairingOption()
                    out.write(pairingOptionBytes)
                    logSendMessage("Option d'appairage")
                    val pairingOptionAck = waitForMessageOrFail()
                    logReceivedMessage("Ack de l'option d'appairage: ${pairingOptionAck.toString().take(200)}")

                    Timber.d("Envoi du message de configuration...")
                    val configMessageBytes = pairingMessageManager.createConfigMessage()
                    out.write(configMessageBytes)
                    logSendMessage("Message de configuration")
                    val pairingConfigAck = waitForMessageOrFail()
                    logReceivedMessage("Ack de la configuration d'appairage: ${pairingConfigAck.toString().take(200)}")

                    _eventFlow.emit(PairingEvent.SecretRequested)
                    Timber.i("En attente du secret fourni par l'utilisateur.")

                    val secretCode = withTimeoutOrNull(60000) {
                        secretChannel.receive()
                    } ?: throw PairingException("Timeout en attente du secret utilisateur.")
                    Timber.d("Secret reçu: %s", "****")

                    val pairingSecretMessageProto = processSecret(secretCode)

                    Timber.d("Envoi du message secret au serveur...")
                    val secretMessageBytes = pairingMessageManager.createSecretMessage(pairingSecretMessageProto)
                    out.write(secretMessageBytes)
                    logSendMessage("Message secret (préparé et envoyé)")
                    val pairingSecretAck = waitForMessageOrFail()
                    logReceivedMessage("Ack du secret d'appairage: ${pairingSecretAck.toString().take(200)}")

                    val peerCertificates = sslSocket?.session?.peerCertificates
                    val serverCertificate = if (peerCertificates?.isNotEmpty() == true && peerCertificates[0] is X509Certificate) {
                        peerCertificates[0] as X509Certificate
                    } else {
                        Timber.w("Certificat du serveur non trouvé ou pas du type X509 après l'appairage.")
                        null
                    }

                    Timber.i("Appairage réussi avec %s.", host)
                    _eventFlow.emit(PairingEvent.Paired(serverCertificate))
                    _eventFlow.emit(PairingEvent.SessionEnded)
                } ?: throw IOException("OutputStream non initialisé.")

            } catch (e: PairingException) {
                Timber.e(e, "Erreur d'appairage: %s", e.message)
                _eventFlow.emit(PairingEvent.Error("Erreur d'appairage: ${e.message}"))
            } catch (e: IOException) {
                Timber.e(e, "Erreur d'E/S pendant l'appairage: %s", e.message)
                _eventFlow.emit(PairingEvent.Error("Erreur d'E/S: ${e.message}"))
            } catch (e: GeneralSecurityException) {
                Timber.e(e, "Erreur de sécurité générale pendant l'appairage: %s", e.message)
                _eventFlow.emit(PairingEvent.Error("Erreur de sécurité: ${e.message}"))
            } catch (e: InterruptedException) {
                Timber.w("Appairage interrompu.")
                _eventFlow.emit(PairingEvent.Error("Appairage interrompu."))
                Thread.currentThread().interrupt()
            } catch (e: Exception) {
                Timber.e(e, "Erreur inattendue pendant l'appairage: %s", e.message)
                _eventFlow.emit(PairingEvent.Error("Erreur inattendue: ${e.message}"))
            } finally {
                Timber.d("Fin de la session d'appairage.")
                _eventFlow.emit(PairingEvent.SessionEnded)
            }
        }
    }

    private suspend fun initializeSslSocket(host: String, port: Int) {
        withContext(Dispatchers.IO) {
            Timber.d("Initialisation du socket SSL pour %s:%d", host, port)
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(KeyStoreManager().getKeyManagers(), arrayOf<TrustManager>(DummyTrustManager()), SecureRandom())
            val sslSocketFactory = sslContext.socketFactory
            val newSocket = sslSocketFactory.createSocket(host, port) as SSLSocket
            sslSocket = newSocket
            outputStream = newSocket.outputStream
            inputStream = newSocket.inputStream
            Timber.i("Socket SSL connecté à %s:%d", host, port)
        }
    }

    private fun startPacketParser() {
        val currentInputStream = inputStream
        if (currentInputStream == null) {
            Timber.e("InputStream est null, impossible de démarrer PairingPacketParser.")
            coroutineScope.launch { _eventFlow.emit(PairingEvent.Error("Erreur interne: InputStream non disponible pour l'analyseur.")) }
            return
        }

        packetParserJob?.cancel()
        packetParserJob = coroutineScope.launch {
            Timber.d("Démarrage de PairingPacketParser sur le thread %s", Thread.currentThread().name)
            try {
                val pairingPacketParser = PairingPacketParser(currentInputStream, incomingMessagesChannel)
                pairingPacketParser.parsePackets()
            } catch (e: IOException) {
                if (isActive) {
                    Timber.e(e, "IOException dans PairingPacketParser")
                    _eventFlow.emit(PairingEvent.Error("Erreur de lecture réseau: ${e.message}"))
                } else {
                    Timber.d("IOException dans PairingPacketParser après annulation, ignorée.")
                }
            } catch (e: Exception) {
                if (isActive) {
                    Timber.e(e, "Erreur inattendue dans PairingPacketParser")
                    _eventFlow.emit(PairingEvent.Error("Erreur interne d'analyseur: ${e.message}"))
                }
            } finally {
                Timber.d("Coroutine de PairingPacketParser.parsePackets terminée.")
                if (!incomingMessagesChannel.isClosedForSend) {
                    incomingMessagesChannel.close()
                }
            }
        }
    }


    private suspend fun waitForMessageOrFail(timeoutMillis: Long = 30000): Pairingmessage.PairingMessage {
        Timber.v("Attente d'un message depuis le canal entrant...")
        val pairingMessage = withTimeoutOrNull(timeoutMillis) {
            incomingMessagesChannel.receive()
        } ?: throw PairingException("Timeout en attente d'un message du serveur.")

        if (pairingMessage.status != Pairingmessage.PairingMessage.Status.STATUS_OK) {
            val errorMessage = "Erreur dans le message d'appairage reçu: ${pairingMessage.status} - ${pairingMessage.toString().take(200)}"
            Timber.w(errorMessage)
            _eventFlow.emit(PairingEvent.Error(errorMessage))
            throw PairingException("Message reçu avec un statut non OK: ${pairingMessage.status}.")
        }
        Timber.v("Message reçu du canal et statut OK.")
        return pairingMessage
    }

    suspend fun provideSecret(secret: String) {
        Timber.d("Tentative de fournir le secret via le canal.")
        try {
            withTimeoutOrNull(5000) {
                secretChannel.send(secret)
                Timber.i("Secret envoyé au processus d'appairage.")
            } ?: Timber.w("Timeout lors de l'envoi du secret au canal.")
        } catch (e: Exception) {
            Timber.e(e, "Erreur lors de l'envoi du secret via le canal")
            _eventFlow.emit(PairingEvent.Error("Impossible de traiter le secret: ${e.message}"))
        }
    }

    private suspend fun processSecret(codeP: String): Pairingmessage.PairingMessage {
        return withContext(Dispatchers.Default) {
            var localCode = codeP
            Timber.d("Traitement du secret original: '%s'", "****")

            if (localCode.length < 2) {
                Timber.e("Code secret trop court pour appliquer substring(2): '%s'", "****")
                _eventFlow.emit(PairingEvent.Error("Code secret fourni trop court."))
                throw PairingException("Code secret fourni trop court.")
            }
            localCode = localCode.substring(2)
            Timber.d("Code après substring(2): '%s'", "****")

            val currentSslSocket = sslSocket
            if (currentSslSocket == null || !currentSslSocket.isConnected || currentSslSocket.isClosed) {
                Timber.e("Impossible de traiter le secret: socket SSL non connecté ou fermé.")
                _eventFlow.emit(PairingEvent.Error("Connexion perdue avant le traitement du secret."))
                throw PairingException("Socket SSL non disponible pour le traitement du secret.")
            }

            val localCert = Utils.getLocalCert(currentSslSocket.session)
            val peerCert = Utils.getPeerCert(currentSslSocket.session)

            if (localCert == null || peerCert == null) {
                Timber.e("Impossible d'obtenir les certificats locaux ou distants de la session SSL.")
                _eventFlow.emit(PairingEvent.Error("Erreur interne: certificats SSL manquants."))
                throw PairingException("Certificats SSL manquants.")
            }

            val pairingChallengeResponse = PairingChallengeResponse(localCert, peerCert)
            val secretBytes: ByteArray
            try {
                if (localCode.isEmpty()) {
                    Timber.e("Code vide après substring(2), impossible de convertir en octets.")
                    _eventFlow.emit(PairingEvent.Error("Code secret traité vide."))
                    throw PairingException("Code secret traité vide.")
                }
                secretBytes = Utils.hexStringToBytes(localCode)
            } catch (e: IllegalArgumentException) {
                Timber.e(e, "Chaîne hexadécimale invalide après substring: '%s'", "****")
                _eventFlow.emit(PairingEvent.Error("Format de code secret invalide après traitement: ${e.message}"))
                throw PairingException("Format de code secret invalide: ${e.message}", e)
            }

            Timber.i("Octets secrets à traiter (après substring(2) et hexToBytes): %s", Utils.bytesToHexString(secretBytes))

            try {
                pairingChallengeResponse.checkGamma(secretBytes)
                Timber.i("checkGamma réussi pour le secret: %s", Utils.bytesToHexString(secretBytes))

                val pairingChallengeResponseAlpha = pairingChallengeResponse.getAlpha(secretBytes)
                Timber.i("Alpha dérivé: %s", Utils.bytesToHexString(pairingChallengeResponseAlpha))

                pairingMessageManager.createSecretMessageProto(pairingChallengeResponseAlpha)
            } catch (e: PairingException) {
                Timber.e(e, "PairingException durant le traitement du secret (checkGamma ou getAlpha): %s", e.message)
                _eventFlow.emit(PairingEvent.Error("Erreur durant le traitement du secret: ${e.message}"))
                throw e
            } catch (e: IllegalArgumentException) {
                Timber.e(e, "IllegalArgumentException durant le traitement du secret (probablement de extractNonce): %s", e.message)
                _eventFlow.emit(PairingEvent.Error("Format de données secret invalide: ${e.message}"))
                throw PairingException("Format de données secret invalide: ${e.message}", e)
            }
        }
    }

    private suspend fun logSendMessage(message: String) {
        Timber.i("Message envoyé : %s", message)
        _eventFlow.emit(PairingEvent.Log("Envoyé: $message"))
    }

    private suspend fun logReceivedMessage(messageSummary: String) {
        Timber.i("Message Reçu : %s", messageSummary)
        _eventFlow.emit(PairingEvent.Log("Reçu: $messageSummary"))
    }

    fun closeSocket() {
        pairingJob?.cancel()
        packetParserJob?.cancel()
        coroutineScope.launch(Dispatchers.IO) {
            sslSocket?.let {
                if (!it.isClosed) {
                    Timber.d("Fermeture du SSLSocket de PairingSession.")
                    try {
                        it.close()
                    } catch (e: IOException) {
                        Timber.e(e, "IOException lors de la fermeture du SSLSocket de PairingSession: %s", e.message)
                    }
                }
            }
            sslSocket = null
            outputStream = null
            inputStream = null
            incomingMessagesChannel.close()
            secretChannel.close()
            Timber.d("SSLSocket de PairingSession fermé et ressources nettoyées.")
        }.invokeOnCompletion {
        }
    }
}

