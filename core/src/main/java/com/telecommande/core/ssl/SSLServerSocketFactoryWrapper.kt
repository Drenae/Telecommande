package com.telecommande.core.ssl

import timber.log.Timber
import java.net.InetAddress
import java.net.ServerSocket
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import javax.net.ssl.KeyManager
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLServerSocketFactory
import javax.net.ssl.TrustManager

open class SSLServerSocketFactoryWrapper(
    keyManagers: Array<KeyManager>?,
    trustManagers: Array<TrustManager>?
) : SSLServerSocketFactory() {

    private val factory: SSLServerSocketFactory

    companion object {
        @JvmStatic
        fun createWithDummyTrustManager(keyManagers: Array<KeyManager>?): SSLServerSocketFactoryWrapper {
            Timber.d("Création de SSLServerSocketFactoryWrapper avec DummyTrustManager.")
            val trustManagers = arrayOf<TrustManager>(DummyTrustManager())
            return SSLServerSocketFactoryWrapper(keyManagers, trustManagers)
        }
    }

    init {
        Timber.d("Initialisation de SSLServerSocketFactoryWrapper.")
        try {
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(keyManagers, trustManagers, null)
            this.factory = sslContext.serverSocketFactory
            Timber.i("SSLServerSocketFactoryWrapper initialisé avec succès.")
        } catch (e: NoSuchAlgorithmException) {
            Timber.e(e, "Algorithme TLS non trouvé lors de l'initialisation de SSLServerSocketFactoryWrapper.")
            throw e
        } catch (e: KeyManagementException) {
            Timber.e(e, "KeyManagementException lors de l'initialisation de SSLServerSocketFactoryWrapper.")
            throw e
        }
    }

    override fun createServerSocket(port: Int): ServerSocket {
        return factory.createServerSocket(port)
    }

    override fun createServerSocket(port: Int, backlog: Int): ServerSocket {
        return factory.createServerSocket(port, backlog)
    }

    override fun createServerSocket(port: Int, backlog: Int, ifAddress: InetAddress?): ServerSocket {
        return factory.createServerSocket(port, backlog, ifAddress)
    }

    override fun getDefaultCipherSuites(): Array<String> {
        return factory.defaultCipherSuites
    }

    override fun getSupportedCipherSuites(): Array<String> {
        return factory.supportedCipherSuites
    }
}