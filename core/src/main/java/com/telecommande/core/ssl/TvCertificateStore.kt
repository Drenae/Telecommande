package com.telecommande.core.ssl

/**
 * Small public facade for certificate operations that the app layer needs.
 * The actual KeyStore implementation remains internal to the core module.
 */
object TvCertificateStore {
    fun removePairedTvCertificate(keystoreAlias: String): Boolean {
        return KeyStoreManager().removeRemoteCertificate(keystoreAlias) != null
    }
}
