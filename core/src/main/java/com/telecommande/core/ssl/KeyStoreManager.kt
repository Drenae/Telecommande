package com.telecommande.core.ssl

import com.telecommande.core.AndroidRemoteContext
import timber.log.Timber
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.GeneralSecurityException
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.MessageDigest
import java.security.cert.Certificate
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.Locale
import java.util.UUID
import javax.net.ssl.KeyManager
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

internal class KeyStoreManager {

    private val androidRemoteContext: AndroidRemoteContext = AndroidRemoteContext.getInstance()
    private val keyStore: KeyStore = getOrLoadSharedKeyStore()
    private val dynamicTrustManager = DynamicTrustManager(keyStore)

    companion object {
        private const val ANDROID_KEYSTORE_TYPE = "AndroidKeyStore"
        private const val LOCAL_IDENTITY_ALIAS = "androidtv-remote"
        private const val REMOTE_IDENTITY_ALIAS_PATTERN = "androidtv-remote-%s"
        private const val SERVER_IDENTITY_ALIAS = "androidtv-local"

        private val sharedLock = Any()

        @Volatile
        private var sharedKeyStore: KeyStore? = null

        private fun createAlias(identifier: String): String {
            return REMOTE_IDENTITY_ALIAS_PATTERN.format(identifier)
        }

        private fun getCertificateNameForUID(uid: String): String {
            return "CN=androidtv/$uid"
        }

        private fun getSubjectDN(certificate: Certificate): String? {
            return (certificate as? X509Certificate)?.subjectX500Principal?.name
        }
    }

    private class DynamicTrustManager(keyStore: KeyStore) : X509TrustManager {
        @Volatile
        private var trustManager: X509TrustManager = createTrustManager(keyStore)

        override fun getAcceptedIssuers(): Array<X509Certificate> =
            trustManager.acceptedIssuers ?: emptyArray()

        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
            trustManager.checkClientTrusted(chain, authType)
        }

        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
            trustManager.checkServerTrusted(chain, authType)
        }

        fun reloadTrustManager(keyStoreToTrust: KeyStore) {
            trustManager = createTrustManager(keyStoreToTrust)
        }

        companion object {
            private fun createTrustManager(keyStore: KeyStore): X509TrustManager {
                val factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
                factory.init(keyStore)
                return factory.trustManagers
                    .filterIsInstance<X509TrustManager>()
                    .firstOrNull()
                    ?: throw IllegalStateException("Aucun X509TrustManager disponible")
            }
        }
    }

    private class PinnedRemoteTrustManager(
        private val expectedCertificate: X509Certificate
    ) : X509TrustManager {
        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf(expectedCertificate)

        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
            throw CertificateException("Validation de certificat client non supportée ici")
        }

        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
            val presented = chain.firstOrNull()
                ?: throw CertificateException("Aucun certificat serveur présenté")

            if (!MessageDigest.isEqual(expectedCertificate.encoded, presented.encoded)) {
                throw CertificateException("Le certificat présenté ne correspond pas à la TV appairée")
            }
        }
    }

    private fun getOrLoadSharedKeyStore(): KeyStore {
        sharedKeyStore?.let { return it }

        synchronized(sharedLock) {
            sharedKeyStore?.let { return it }
            val loaded = loadOrCreateKeyStore()
            sharedKeyStore = loaded
            return loaded
        }
    }

    private fun loadOrCreateKeyStore(): KeyStore {
        try {
            val keyStore = if (useAndroidKeyStore()) {
                KeyStore.getInstance(ANDROID_KEYSTORE_TYPE).apply {
                    load(null, null)
                }
            } else {
                KeyStore.getInstance(KeyStore.getDefaultType()).apply {
                    val file = androidRemoteContext.keyStoreFile
                    if (file.exists() && file.length() > 0) {
                        FileInputStream(file).use { input ->
                            load(input, androidRemoteContext.keyStorePass)
                        }
                    } else {
                        load(null, androidRemoteContext.keyStorePass)
                    }
                }
            }

            if (!hasServerIdentity(keyStore)) {
                createAndSetIdentity(keyStore)
                persistKeyStore(keyStore)
            }

            Timber.d("KeyStore ready")
            return keyStore
        } catch (e: Exception) {
            Timber.e(e, "Impossible de charger le KeyStore, recréation")
            val fresh = createNewIdentityKeyStore()
            persistKeyStore(fresh)
            return fresh
        }
    }

    private fun createNewIdentityKeyStore(): KeyStore {
        val keyStore = if (useAndroidKeyStore()) {
            KeyStore.getInstance(ANDROID_KEYSTORE_TYPE).apply {
                load(null, null)
            }
        } else {
            KeyStore.getInstance(KeyStore.getDefaultType()).apply {
                load(null, androidRemoteContext.keyStorePass)
            }
        }
        createAndSetIdentity(keyStore)
        return keyStore
    }

    private fun createAndSetIdentity(
        keyStoreInstance: KeyStore,
        alias: String = SERVER_IDENTITY_ALIAS,
        uidForCertName: String = UUID.randomUUID().toString()
    ) {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        val keyPair = keyPairGenerator.generateKeyPair()
        val certificate = SslUtil.generateX509V3Certificate(
            keyPair,
            getCertificateNameForUID(uidForCertName)
        )

        try {
            keyStoreInstance.setKeyEntry(alias, keyPair.private, null, arrayOf(certificate))
        } catch (e: IllegalArgumentException) {
            val originalLocale = Locale.getDefault()
            Locale.setDefault(Locale.ENGLISH)
            try {
                keyStoreInstance.setKeyEntry(alias, keyPair.private, null, arrayOf(certificate))
            } finally {
                Locale.setDefault(originalLocale)
            }
        }
    }

    private fun hasServerIdentity(keyStoreInstance: KeyStore): Boolean {
        return try {
            keyStoreInstance.containsAlias(SERVER_IDENTITY_ALIAS)
        } catch (e: KeyStoreException) {
            Timber.w(e, "Impossible de vérifier l'identité locale du KeyStore")
            false
        }
    }

    private fun persistKeyStore(keyStoreToPersist: KeyStore = keyStore) {
        if (useAndroidKeyStore()) return

        try {
            FileOutputStream(androidRemoteContext.keyStoreFile).use { output ->
                keyStoreToPersist.store(output, androidRemoteContext.keyStorePass)
            }
        } catch (e: Exception) {
            throw IllegalStateException("Impossible de persister le KeyStore", e)
        }
    }

    private fun useAndroidKeyStore(): Boolean = false

    fun clearAllAndReinitialize() {
        synchronized(sharedLock) {
            try {
                val aliases = keyStore.aliases().toList()
                aliases.forEach { keyStore.deleteEntry(it) }
                createAndSetIdentity(keyStore)
                persistKeyStore()
                dynamicTrustManager.reloadTrustManager(keyStore)
            } catch (e: Exception) {
                Timber.e(e, "Échec de la réinitialisation du KeyStore")
                throw IllegalStateException("Impossible de réinitialiser le KeyStore", e)
            }
        }
    }

    fun getKeyManagers(): Array<KeyManager> {
        val factory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
        factory.init(keyStore, "".toCharArray())
        return factory.keyManagers
    }

    fun getTrustManagers(): Array<TrustManager> = arrayOf(dynamicTrustManager)

    fun getPinnedTrustManagers(identifierForAlias: String): Array<TrustManager> {
        val certificate = getRemoteCertificate(identifierForAlias)
            ?: throw GeneralSecurityException("Aucun certificat enregistré pour la TV sélectionnée")
        return arrayOf(PinnedRemoteTrustManager(certificate))
    }

    fun hasRemoteCertificate(identifierForAlias: String): Boolean {
        return getRemoteCertificate(identifierForAlias) != null
    }

    private fun getRemoteCertificate(identifierForAlias: String): X509Certificate? {
        return synchronized(sharedLock) {
            try {
                keyStore.getCertificate(createAlias(identifierForAlias)) as? X509Certificate
            } catch (e: KeyStoreException) {
                Timber.e(e, "Erreur lors de la lecture du certificat distant")
                null
            }
        }
    }

    fun hasServerIdentityAlias(): Boolean = hasServerIdentity(keyStore)

    fun initializeLocalIdentity(uidForCertName: String = UUID.randomUUID().toString()) {
        synchronized(sharedLock) {
            try {
                createAndSetIdentity(keyStore, LOCAL_IDENTITY_ALIAS, uidForCertName)
                persistKeyStore()
                dynamicTrustManager.reloadTrustManager(keyStore)
            } catch (e: GeneralSecurityException) {
                throw IllegalStateException("Impossible de créer l'identité locale du KeyStore", e)
            }
        }
    }

    fun removeRemoteCertificate(identifierForAlias: String): Certificate? {
        synchronized(sharedLock) {
            return try {
                val alias = createAlias(identifierForAlias)
                if (!keyStore.containsAlias(alias)) {
                    null
                } else {
                    val certificate = keyStore.getCertificate(alias)
                    keyStore.deleteEntry(alias)
                    persistKeyStore()
                    dynamicTrustManager.reloadTrustManager(keyStore)
                    certificate
                }
            } catch (e: KeyStoreException) {
                Timber.e(e, "Erreur lors de la suppression du certificat distant")
                null
            }
        }
    }

    fun storeRemoteCertificate(certificate: Certificate, identifierForAlias: String) {
        synchronized(sharedLock) {
            try {
                val aliasToStore = createAlias(identifierForAlias)
                val newSubject = getSubjectDN(certificate)

                if (keyStore.containsAlias(aliasToStore)) {
                    keyStore.deleteEntry(aliasToStore)
                }

                if (newSubject != null) {
                    val duplicates = mutableListOf<String>()
                    val aliases = keyStore.aliases()
                    while (aliases.hasMoreElements()) {
                        val alias = aliases.nextElement()
                        if (alias == aliasToStore) continue
                        val current = keyStore.getCertificate(alias) ?: continue
                        if (getSubjectDN(current) == newSubject) {
                            duplicates += alias
                        }
                    }
                    duplicates.forEach { keyStore.deleteEntry(it) }
                }

                keyStore.setCertificateEntry(aliasToStore, certificate)
                persistKeyStore()
                dynamicTrustManager.reloadTrustManager(keyStore)
            } catch (e: KeyStoreException) {
                Timber.e(e, "Erreur lors du stockage du certificat distant")
                throw e
            }
        }
    }
}
