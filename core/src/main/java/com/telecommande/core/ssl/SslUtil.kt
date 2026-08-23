package com.telecommande.core.ssl

import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier
import org.bouncycastle.asn1.x509.BasicConstraints
import org.bouncycastle.asn1.x509.ExtendedKeyUsage
import org.bouncycastle.asn1.x509.Extension
import org.bouncycastle.asn1.x509.GeneralName
import org.bouncycastle.asn1.x509.GeneralNames
import org.bouncycastle.asn1.x509.KeyPurposeId
import org.bouncycastle.asn1.x509.KeyUsage
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509v1CertificateBuilder
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import timber.log.Timber
import java.io.FileInputStream
import java.io.IOException
import java.math.BigInteger
import java.security.GeneralSecurityException
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.NoSuchAlgorithmException
import java.security.PublicKey
import java.security.Security
import java.security.cert.Certificate
import java.security.cert.X509Certificate
import java.util.Calendar
import java.util.Date
import java.util.Random
import javax.net.ssl.KeyManager
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.security.auth.x500.X500Principal

object SslUtil {

    private const val BC_PROVIDER = BouncyCastleProvider.PROVIDER_NAME
    private const val SIGNATURE_ALGORITHM = "SHA256WithRSAEncryption"

    init {
        if (Security.getProvider(BC_PROVIDER) == null) {
            Security.addProvider(BouncyCastleProvider())
            Timber.i("Fournisseur BouncyCastle enregistré.")
        } else {
            Timber.d("Fournisseur BouncyCastle déjà enregistré.")
        }
    }

    fun generateRsaKeyPair(keySize: Int = 2048): KeyPair {
        Timber.d("Génération d'une paire de clés RSA (taille : %d)...", keySize)
        val kg = KeyPairGenerator.getInstance("RSA")
        kg.initialize(keySize)
        val kp = kg.generateKeyPair()
        Timber.i("Paire de clés RSA générée avec succès.")
        return kp
    }

    fun getEmptyKeyStore(type: String = KeyStore.getDefaultType(), passwordChars: CharArray? = null): KeyStore {
        Timber.d("Création d'un KeyStore vide (type : %s)...", type)
        val ks = KeyStore.getInstance(type)
        ks.load(null, passwordChars)
        Timber.i("KeyStore vide créé.")
        return ks
    }

    fun generateX509V1Certificate(keyPair: KeyPair, distinguishedName: String): X509Certificate {
        Timber.d("Génération d'un certificat X.509 v1 pour DN='%s'...", distinguishedName)
        val principal = X500Principal(distinguishedName)
        val now = Date()
        val calendar = Calendar.getInstance().apply { time = now }
        val startDate = calendar.time
        calendar.add(Calendar.YEAR, 20)
        val expiryDate = calendar.time
        val serialNumber = BigInteger(128, Random())

        val certBuilder = JcaX509v1CertificateBuilder(
            principal,
            serialNumber,
            startDate,
            expiryDate,
            principal,
            keyPair.public
        )
        val contentSigner = JcaContentSignerBuilder(SIGNATURE_ALGORITHM).build(keyPair.private)
        val holder = certBuilder.build(contentSigner)
        val cert = JcaX509CertificateConverter().setProvider(BC_PROVIDER).getCertificate(holder)
        Timber.i("Certificat X.509 v1 généré avec succès pour DN='%s'.", distinguishedName)
        return cert
    }

    @JvmOverloads
    fun generateX509V3Certificate(
        keyPair: KeyPair,
        distinguishedName: String,
        notBefore: Date? = null,
        notAfter: Date? = null,
        serialNumber: BigInteger? = null,
        subjectAlternativeNames: List<GeneralName>? = null
    ): X509Certificate {
        Timber.d("Génération d'un certificat X.509 v3 pour DN='%s'...", distinguishedName)
        val issuerPrincipalX500Name = X500Name(distinguishedName)
        val subjectPrincipalX500 = X500Principal(distinguishedName)

        val cal = Calendar.getInstance()
        val actualNotBefore = notBefore ?: cal.time.also { cal.add(Calendar.YEAR, -1) }
        val actualNotAfter = notAfter ?: run {
            cal.time = Date()
            cal.add(Calendar.YEAR, 20)
            cal.time
        }
        val actualSerialNumber = serialNumber ?: BigInteger(128, Random())

        val certBuilder = JcaX509v3CertificateBuilder(
            issuerPrincipalX500Name,
            actualSerialNumber,
            actualNotBefore,
            actualNotAfter,
            X500Name(subjectPrincipalX500.name),
            keyPair.public
        )

        certBuilder.addExtension(Extension.basicConstraints, true, BasicConstraints(false))
        certBuilder.addExtension(
            Extension.keyUsage, true,
            KeyUsage(KeyUsage.digitalSignature or KeyUsage.keyEncipherment or KeyUsage.keyCertSign)
        )
        certBuilder.addExtension(
            Extension.extendedKeyUsage, true,
            ExtendedKeyUsage(arrayOf(KeyPurposeId.id_kp_serverAuth, KeyPurposeId.id_kp_clientAuth))
        )

        val authorityKeyId = createAuthorityKeyIdentifier(keyPair.public, issuerPrincipalX500Name, actualSerialNumber)
        certBuilder.addExtension(Extension.authorityKeyIdentifier, false, authorityKeyId)

        if (!subjectAlternativeNames.isNullOrEmpty()) {
            certBuilder.addExtension(Extension.subjectAlternativeName, false, GeneralNames(subjectAlternativeNames.toTypedArray()))
        } else {
            val defaultSan = GeneralNames(GeneralName(GeneralName.rfc822Name, "device@example.com"))
            certBuilder.addExtension(Extension.subjectAlternativeName, false, defaultSan)
        }

        val contentSigner = JcaContentSignerBuilder(SIGNATURE_ALGORITHM).setProvider(BC_PROVIDER).build(keyPair.private)
        val holder = certBuilder.build(contentSigner)
        val cert = JcaX509CertificateConverter().setProvider(BC_PROVIDER).getCertificate(holder)
        Timber.i("Certificat X.509 v3 généré avec succès pour DN='%s'.", distinguishedName)
        return cert
    }

    private fun createAuthorityKeyIdentifier(
        publicKey: PublicKey,
        issuerName: X500Name,
        issuerSerialNumber: BigInteger
    ): AuthorityKeyIdentifier {
        return try {
            val spki = SubjectPublicKeyInfo.getInstance(publicKey.encoded)
            AuthorityKeyIdentifier(spki.publicKeyData.bytes, GeneralNames(GeneralName(issuerName)), issuerSerialNumber)
        } catch (e: IOException) {
            Timber.e(e, "IOException lors de la création de AuthorityKeyIdentifier.")
            throw e
        }
    }

    fun generateTestSslContext(keyManagerPassword: String = "testpassword"): SSLContext {
        Timber.d("Génération d'un contexte SSL de test...")
        val keyManagers = generateTestServerKeyManagerArray(keyEntryPassword = keyManagerPassword)
        val trustManagers = arrayOf<TrustManager>(DummyTrustManager())
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(keyManagers, trustManagers, null)
        Timber.i("Contexte SSL de test généré avec succès.")
        return sslContext
    }

    fun loadFileBackedKeyManagers(
        keyManagerInstanceName: String = KeyManagerFactory.getDefaultAlgorithm(),
        keyStoreFileName: String,
        keyStorePasswordChars: CharArray
    ): Array<KeyManager> {
        Timber.d("Chargement des KeyManagers depuis le fichier KeyStore : %s", keyStoreFileName)
        val kmf = KeyManagerFactory.getInstance(keyManagerInstanceName)
        val ks = KeyStore.getInstance(KeyStore.getDefaultType())
        FileInputStream(keyStoreFileName).use { fis ->
            ks.load(fis, keyStorePasswordChars)
        }
        kmf.init(ks, keyStorePasswordChars)
        val keyManagers = kmf.keyManagers
        Timber.i("KeyManagers chargés avec succès depuis %s.", keyStoreFileName)
        return keyManagers
    }

    fun generateTestServerKeyManagerArray(
        keyManagerInstanceName: String = KeyManagerFactory.getDefaultAlgorithm(),
        keyEntryPassword: String
    ): Array<KeyManager> {
        Timber.d("Génération de KeyManagers de serveur de test...")
        val kmf = KeyManagerFactory.getInstance(keyManagerInstanceName)
        val keyPair = generateRsaKeyPair()
        val cert = generateX509V1Certificate(keyPair, "CN=Test Server Cert, O=Test Org")
        val chain = arrayOf<Certificate>(cert)
        val ks = getEmptyKeyStore()
        ks.setKeyEntry("test-server-alias", keyPair.private, keyEntryPassword.toCharArray(), chain)
        kmf.init(ks, keyEntryPassword.toCharArray())
        val keyManagers = kmf.keyManagers
        Timber.i("KeyManagers de serveur de test générés avec succès.")
        return keyManagers
    }
}