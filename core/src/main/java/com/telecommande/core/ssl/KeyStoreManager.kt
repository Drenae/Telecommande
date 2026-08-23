package com.telecommande.core.ssl

import com.telecommande.core.AndroidRemoteContext
import timber.log.Timber
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.cert.Certificate
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.Enumeration
import java.util.Locale
import java.util.UUID
import javax.net.ssl.KeyManager
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

internal class KeyStoreManager {

    private val dynamicTrustManager: DynamicTrustManager
    private val keyStore: KeyStore
    private val androidRemoteContext: AndroidRemoteContext = AndroidRemoteContext.getInstance()

    companion object {
        private const val ANDROID_KEYSTORE_TYPE = "AndroidKeyStore"
        private const val LOCAL_IDENTITY_ALIAS = "androidtv-remote"
        private const val REMOTE_IDENTITY_ALIAS_PATTERN = "androidtv-remote-%s"
        private const val SERVER_IDENTITY_ALIAS = "androidtv-local"

        private fun createAlias(identifier: String): String {
            return REMOTE_IDENTITY_ALIAS_PATTERN.format(identifier)
        }

        private val certificateName: String
            get() = getCertificateNameForUID(UUID.randomUUID().toString())

        private fun getCertificateNameForUID(uid: String): String {
            return "CN=androidtv/$uid"
        }

        private fun getSubjectDN(certificate: Certificate): String? {
            return (certificate as? X509Certificate)?.subjectX500Principal?.name
        }
    }

    private class DynamicTrustManager(keyStore: KeyStore) : X509TrustManager {
        private lateinit var trustManager: X509TrustManager

        init {
            reloadTrustManager(keyStore)
        }

        override fun getAcceptedIssuers(): Array<X509Certificate> {
            return trustManager.acceptedIssuers ?: emptyArray()
        }

        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
            try {
                trustManager.checkClientTrusted(chain, authType)
                Timber.v("DynamicTrustManager: Client certifié pour authType: %s", authType)
            } catch (e: CertificateException) {
                Timber.w(e, "DynamicTrustManager: Échec de la certification client pour authType: %s", authType)
                throw e
            }
        }

        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
            try {
                trustManager.checkServerTrusted(chain, authType)
                Timber.v("DynamicTrustManager: Serveur certifié pour authType: %s", authType)
            } catch (e: CertificateException) {
                Timber.w(e, "DynamicTrustManager: Échec de la certification serveur pour authType: %s", authType)
                throw e
            }
        }

        fun reloadTrustManager(keyStoreToTrust: KeyStore) {
            Timber.d("Rechargement du TrustManager avec le nouveau KeyStore.")
            try {
                val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
                trustManagerFactory.init(keyStoreToTrust)
                val foundTm = trustManagerFactory.trustManagers.filterIsInstance<X509TrustManager>().firstOrNull()
                if (foundTm != null) {
                    this.trustManager = foundTm
                    Timber.i("TrustManager rechargé avec succès.")
                } else {
                    Timber.e("Aucun X509TrustManager trouvé après le rechargement.")
                    throw IllegalStateException("Aucun X509TrustManager trouvé")
                }
            } catch (e: Exception) {
                Timber.e(e, "Échec du rechargement du TrustManager.")

            }
        }
    }

    init {
        Timber.d("Initialisation de KeyStoreManager.")
        val loadedKeyStore = loadOrCreateKeyStore()
        this.keyStore = loadedKeyStore
        this.dynamicTrustManager = DynamicTrustManager(loadedKeyStore)
    }

    private fun clearKeyStoreEntries() {
        Timber.d("Effacement de toutes les entrées du KeyStore.")
        try {
            val aliases: Enumeration<String> = this.keyStore.aliases()
            while (aliases.hasMoreElements()) {
                val alias = aliases.nextElement()
                this.keyStore.deleteEntry(alias)
                Timber.v("Entrée supprimée: %s", alias)
            }
        } catch (e: KeyStoreException) {
            Timber.e(e, "Erreur KeyStore lors de l'effacement des entrées.")
        }
        persistKeyStore()
    }

    private fun createAndSetIdentity(keyStoreInstance: KeyStore, alias: String = SERVER_IDENTITY_ALIAS, uidForCertName: String = UUID.randomUUID().toString()) {
        Timber.d("Création et configuration de l'identité pour l'alias: %s, UID: %s", alias, uidForCertName)
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        val keyPair = keyPairGenerator.generateKeyPair()
        val certificate = SslUtil.generateX509V3Certificate(keyPair, getCertificateNameForUID(uidForCertName))

        try {
            keyStoreInstance.setKeyEntry(alias, keyPair.private, null, arrayOf(certificate))
        } catch (e: IllegalArgumentException) {
            Timber.w(e, "IllegalArgumentException lors de setKeyEntry, tentative avec Locale Anglais.")
            val originalLocale = Locale.getDefault()
            setSystemLocale(Locale.ENGLISH)
            try {
                keyStoreInstance.setKeyEntry(alias, keyPair.private, null, arrayOf(certificate))
            } finally {
                setSystemLocale(originalLocale)
            }
        }
        Timber.i("Identité créée et configurée pour l'alias: %s", alias)
    }

    private fun setSystemLocale(locale: Locale) {
        try {
            Locale.setDefault(locale)
            Timber.d("Locale système configurée sur: %s", locale.toLanguageTag())
        } catch (e: SecurityException) {
            Timber.e(e, "SecurityException lors de la configuration de la locale système.")
        } catch (e: Exception) {
            Timber.w(e, "Exception lors de la configuration de la locale système.")
        }
    }

    private fun createNewIdentityKeyStore(): KeyStore {
        Timber.d("Création d'un nouveau KeyStore d'identité.")
        val ksInstance: KeyStore
        if (!useAndroidKeyStore()) {
            ksInstance = KeyStore.getInstance(KeyStore.getDefaultType())
            try {
                ksInstance.load(null, androidRemoteContext.keyStorePass)
            } catch (e: IOException) {
                Timber.e(e, "Impossible de créer un KeyStore vide (type par défaut).")
                throw GeneralSecurityException("Impossible de créer un KeyStore vide", e)
            }
        } else {
            ksInstance = KeyStore.getInstance(ANDROID_KEYSTORE_TYPE)
            try {
                ksInstance.load(null, null)
            } catch (e: IOException) {
                Timber.e(e, "Impossible de créer un KeyStore vide (type AndroidKeyStore).")
                throw GeneralSecurityException("Impossible de créer un KeyStore vide", e)
            }
        }
        createAndSetIdentity(ksInstance)
        return ksInstance
    }

    private fun hasServerIdentity(keyStoreInstance: KeyStore): Boolean {
        return try {
            val hasAlias = keyStoreInstance.containsAlias(SERVER_IDENTITY_ALIAS)
            Timber.v("Le KeyStore contient l'alias serveur (%s): %s", SERVER_IDENTITY_ALIAS, hasAlias)
            hasAlias
        } catch (e: KeyStoreException) {
            Timber.w(e, "Erreur KeyStore lors de la vérification de l'alias serveur.")
            false
        }
    }

    private fun loadOrCreateKeyStore(): KeyStore {
        Timber.d("Chargement ou création du KeyStore.")
        try {
            val ks: KeyStore
            if (!useAndroidKeyStore()) {
                ks = KeyStore.getInstance(KeyStore.getDefaultType())
                val keyStoreFile = androidRemoteContext.keyStoreFile
                if (keyStoreFile.exists() && keyStoreFile.length() > 0) {
                    Timber.i("Chargement du KeyStore depuis le fichier: %s", keyStoreFile.absolutePath)
                    FileInputStream(keyStoreFile).use { inputStream ->
                        ks.load(inputStream, androidRemoteContext.keyStorePass)
                    }
                } else {
                    Timber.i("Aucun fichier KeyStore existant ou fichier vide, initialisation d'un nouveau KeyStore.")
                    ks.load(null, androidRemoteContext.keyStorePass)
                }
            } else {
                Timber.i("Utilisation du type de KeyStore Android.")
                ks = KeyStore.getInstance(ANDROID_KEYSTORE_TYPE)
                ks.load(null, null)
            }

            if (!hasServerIdentity(ks)) {
                Timber.w("L'identité serveur est manquante dans le KeyStore chargé, création d'une nouvelle identité.")
                createAndSetIdentity(ks)
                persistKeyStore(ks)
            }
            Timber.i("KeyStore chargé avec succès.")
            return ks
        } catch (e: Exception) {
            Timber.e(e, "Échec critique lors du chargement ou de l'initialisation du KeyStore. Tentative de création d'un nouveau KeyStore.")
            try {
                val newKs = createNewIdentityKeyStore()
                persistKeyStore(newKs)
                return newKs
            } catch (ge: GeneralSecurityException) {
                Timber.e(ge, "Échec de la création d'un nouveau KeyStore d'identité après un échec de chargement.")
                throw IllegalStateException("Impossible de créer ou charger le KeyStore d'identité.", ge)
            }
        }
    }

    private fun persistKeyStore(keyStoreToPersist: KeyStore = this.keyStore) {
        if (!useAndroidKeyStore()) {
            Timber.d("Persistance du KeyStore dans le fichier: %s", androidRemoteContext.keyStoreFile.absolutePath)
            try {
                FileOutputStream(androidRemoteContext.keyStoreFile).use { outputStream ->
                    keyStoreToPersist.store(outputStream, androidRemoteContext.keyStorePass)
                }
                Timber.i("KeyStore persisté avec succès.")
            } catch (e: Exception) {
                Timber.e(e, "Échec de la persistance du KeyStore.")
                when (e) {
                    is IOException, is GeneralSecurityException -> throw IllegalStateException("Impossible de persister le KeyStore", e)
                    else -> throw e
                }
            }
        } else {
            Timber.d("Utilisation du KeyStore Android, persistance explicite non requise/supportée de cette manière.")
        }
    }

    private fun useAndroidKeyStore(): Boolean {
        Timber.v("Vérification de l'utilisation d'AndroidKeyStore: false")
        return false
    }

    fun clearAllAndReinitialize() {
        Timber.i("Effacement complet et réinitialisation du KeyStore.")
        clearKeyStoreEntries()
        try {
            createAndSetIdentity(this.keyStore)
        } catch (e: GeneralSecurityException) {
            Timber.e(e, "Erreur lors de la création de l'identité après effacement.")
        }
        persistKeyStore()
    }

    fun getKeyManagers(): Array<KeyManager> {
        Timber.d("Récupération des KeyManagers.")
        return try {
            val keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
            keyManagerFactory.init(this.keyStore, "".toCharArray())
            keyManagerFactory.keyManagers
        } catch (e: GeneralSecurityException) {
            Timber.e(e, "Erreur lors de la récupération des KeyManagers.")
            throw e
        }
    }

    fun getTrustManagers(): Array<TrustManager> {
        Timber.d("Récupération des TrustManagers.")
        return try {
            arrayOf(this.dynamicTrustManager)
        } catch (e: Exception) {
            Timber.e(e, "Erreur lors de la récupération des TrustManagers (DynamicTrustManager).")
            throw GeneralSecurityException("Impossible de récupérer le DynamicTrustManager", e)
        }
    }

    fun hasServerIdentityAlias(): Boolean {
        return hasServerIdentity(this.keyStore)
    }

    fun initializeLocalIdentity(uidForCertName: String = UUID.randomUUID().toString()) {
        Timber.i("Initialisation de l'identité locale avec UID: %s", uidForCertName)
        try {
            createAndSetIdentity(this.keyStore, LOCAL_IDENTITY_ALIAS, uidForCertName)
            persistKeyStore()
        } catch (e: GeneralSecurityException) {
            Timber.e(e, "Erreur lors de l'initialisation de l'identité locale.")
            throw IllegalStateException("Impossible de créer l'identité locale du KeyStore", e)
        }
    }

    fun removeRemoteCertificate(identifierForAlias: String): Certificate? {
        Timber.d("Tentative de suppression du certificat distant pour l'identifiant: %s", identifierForAlias)
        return try {
            val aliasToRemove = createAlias(identifierForAlias)
            if (!this.keyStore.containsAlias(aliasToRemove)) {
                Timber.w("Certificat non trouvé pour suppression, alias: %s", aliasToRemove)
                null
            } else {
                val certificate = this.keyStore.getCertificate(aliasToRemove)
                this.keyStore.deleteEntry(aliasToRemove)
                persistKeyStore()
                Timber.i("Certificat supprimé pour l'alias: %s", aliasToRemove)
                certificate
            }
        } catch (e: KeyStoreException) {
            Timber.e(e, "Erreur KeyStore lors de la suppression du certificat pour l'identifiant: %s", identifierForAlias)
            null
        }
    }

    fun storeRemoteCertificate(certificate: Certificate, identifierForAlias: String) {
        Timber.i("Stockage du certificat distant pour l'identifiant: %s", identifierForAlias)
        try {
            val aliasToStore = createAlias(identifierForAlias)
            val subjectDNOfNewCert = getSubjectDN(certificate)
            Timber.d("Alias pour le nouveau certificat: %s, Sujet DN: %s", aliasToStore, subjectDNOfNewCert)

            if (this.keyStore.containsAlias(aliasToStore)) {
                Timber.d("Alias existant trouvé (%s), suppression de l'entrée précédente.", aliasToStore)
                this.keyStore.deleteEntry(aliasToStore)
            }

            if (subjectDNOfNewCert != null) {
                val aliases = this.keyStore.aliases()
                val entriesToDelete = mutableListOf<String>()
                while (aliases.hasMoreElements()) {
                    val currentAlias = aliases.nextElement()
                    if (currentAlias == aliasToStore) continue

                    val currentCert = this.keyStore.getCertificate(currentAlias)
                    if (currentCert != null) {
                        val subjectDNOfCurrentCert = getSubjectDN(currentCert)
                        if (subjectDNOfNewCert == subjectDNOfCurrentCert) {
                            Timber.d("Ancien certificat trouvé avec le même sujet DN (%s) sous l'alias %s. Marquage pour suppression.", subjectDNOfCurrentCert, currentAlias)
                            entriesToDelete.add(currentAlias)
                        }
                    }
                }
                entriesToDelete.forEach { entryAlias ->
                    Timber.d("Suppression de l'entrée dupliquée pour le sujet DN: %s", entryAlias)
                    this.keyStore.deleteEntry(entryAlias)
                }
            }
            this.keyStore.setCertificateEntry(aliasToStore, certificate)
            Timber.i("Certificat stocké sous l'alias: %s", aliasToStore)
            persistKeyStore()
            this.dynamicTrustManager.reloadTrustManager(this.keyStore)
        } catch (e: KeyStoreException) {
            Timber.e(e, "Erreur KeyStore lors du stockage du certificat pour l'identifiant: %s", identifierForAlias)
        }
    }
}