package com.telecommande.core.ssl

import timber.log.Timber
import javax.net.ssl.KeyManager
import javax.net.ssl.TrustManager

class DummySSLServerSocketFactory internal constructor(
    keyManagers: Array<KeyManager>?,
    trustManagers: Array<TrustManager>?
) : SSLServerSocketFactoryWrapper(keyManagers, trustManagers) {

    companion object {
        @JvmStatic
        fun fromKeyManagers(keyManagers: Array<KeyManager>?): DummySSLServerSocketFactory {
            Timber.d("Création d'une DummySSLServerSocketFactory à partir de KeyManagers.")
            val trustManagers = arrayOf<TrustManager>(DummyTrustManager())
            return DummySSLServerSocketFactory(keyManagers, trustManagers)
        }
    }
}
