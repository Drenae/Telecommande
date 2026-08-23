package com.telecommande.core.ssl

import timber.log.Timber
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

class DummyTrustManager : X509TrustManager {

    init {
        Timber.d("Initialisation de DummyTrustManager. Aucune vérification de confiance ne sera effectuée.")
    }

    override fun checkClientTrusted(chain: Array<X509Certificate>?, authType: String?) {
        Timber.v("DummyTrustManager: checkClientTrusted appelé pour authType: %s. Aucune action.", authType)
    }

    override fun checkServerTrusted(chain: Array<X509Certificate>?, authType: String?) {
        Timber.v("DummyTrustManager: checkServerTrusted appelé pour authType: %s. Aucune action.", authType)
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> {
        Timber.v("DummyTrustManager: getAcceptedIssuers appelé. Retour d'un tableau vide.")
        return emptyArray()
    }
}