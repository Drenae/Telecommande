package com.telecommande.core.ssl

import org.bouncycastle.jce.provider.BouncyCastleProvider
import timber.log.Timber
import java.net.InetAddress
import java.net.Socket
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.Security
import javax.net.ssl.KeyManager
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager

open class SSLSocketFactoryWrapper(
    keyManagers: Array<KeyManager>?,
    trustManagers: Array<TrustManager>?
) : SSLSocketFactory() {

    private val factory: SSLSocketFactory

    companion object {
        @JvmStatic
        fun createWithDummyTrustManager(keyManagers: Array<KeyManager>?): SSLSocketFactoryWrapper {
            Timber.d("Création de SSLSocketFactoryWrapper avec DummyTrustManager.")
            val trustManagers = arrayOf<TrustManager>(DummyTrustManager())
            return SSLSocketFactoryWrapper(keyManagers, trustManagers)
        }
    }

    init {
        Timber.d("Initialisation de SSLSocketFactoryWrapper.")
        try {
            if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
                Security.addProvider(BouncyCastleProvider())
                Timber.i("Fournisseur BouncyCastle ajouté.")
            }

            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(keyManagers, trustManagers, null)
            this.factory = sslContext.socketFactory
            Timber.i("SSLSocketFactoryWrapper initialisé avec succès.")
        } catch (e: NoSuchAlgorithmException) {
            Timber.e(e, "Algorithme TLS non trouvé lors de l'initialisation de SSLSocketFactoryWrapper.")
            throw e
        } catch (e: KeyManagementException) {
            Timber.e(e, "KeyManagementException lors de l'initialisation de SSLSocketFactoryWrapper.")
            throw e
        }
    }

    override fun createSocket(): Socket {
        return factory.createSocket()
    }

    override fun createSocket(host: String?, port: Int): Socket {
        return factory.createSocket(host, port)
    }

    override fun createSocket(host: String?, port: Int, localHost: InetAddress?, localPort: Int): Socket {
        return factory.createSocket(host, port, localHost, localPort)
    }

    override fun createSocket(host: InetAddress?, port: Int): Socket {
        return factory.createSocket(host, port)
    }

    override fun createSocket(address: InetAddress?, port: Int, localAddress: InetAddress?, localPort: Int): Socket {
        return factory.createSocket(address, port, localAddress, localPort)
    }

    override fun createSocket(s: Socket?, host: String?, port: Int, autoClose: Boolean): Socket {
        return factory.createSocket(s, host, port, autoClose)
    }

    override fun getDefaultCipherSuites(): Array<String> {
        return factory.defaultCipherSuites
    }

    override fun getSupportedCipherSuites(): Array<String> {
        return factory.supportedCipherSuites
    }
}