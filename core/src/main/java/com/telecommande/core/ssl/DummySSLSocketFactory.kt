package com.telecommande.core.ssl

import timber.log.Timber
import javax.net.ssl.KeyManager
import javax.net.ssl.TrustManager

class DummySSLSocketFactory internal constructor(
    keyManagers: Array<KeyManager>?,
    trustManagers: Array<TrustManager>?
) : SSLSocketFactoryWrapper(keyManagers, trustManagers) {

    companion object {
        @JvmStatic
        fun fromKeyManagers(keyManagers: Array<KeyManager>?): DummySSLSocketFactory {
            Timber.d("Création d'une DummySSLSocketFactory à partir de KeyManagers.")
            val trustManagers = arrayOf<TrustManager>(DummyTrustManager())
            return DummySSLSocketFactory(keyManagers, trustManagers)
        }
    }
}

