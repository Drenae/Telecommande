package com.telecommande

import android.app.Application
import com.telecommande.core.AndroidRemoteContext
import dagger.hilt.android.HiltAndroidApp
import org.bouncycastle.jce.provider.BouncyCastleProvider
import timber.log.Timber
import java.io.File
import java.security.Security

@HiltAndroidApp
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
        }

        try {
            Timber.i("Avant initialisation BouncyCastle.")
            val startTime = System.currentTimeMillis()
            Security.removeProvider("BC")
            Security.insertProviderAt(BouncyCastleProvider(), 1)
            val endTime = System.currentTimeMillis()
            Timber.i("Fournisseur BouncyCastle inséré. Durée: %d ms", endTime - startTime)
            Security.getProvider("BC")?.let {
                Timber.i("BC Provider version: %s", it.version)
            } ?: Timber.w("BC Provider est nul après insertion.")
        } catch (e: Exception) {
            Timber.e(e, "Erreur lors de l'insertion du fournisseur BouncyCastle")
        }

        Timber.i("Accès à AndroidRemoteContext dans MyApplication.onCreate().")
        val remoteContext = AndroidRemoteContext.getInstance(this)

        Timber.i("Nom du client initialisé : %s", remoteContext.clientName)

        val expectedKeystoreFile = File(this.applicationContext.filesDir, "androidtv_secure.keystore")
        if (remoteContext.keyStoreFile.absolutePath != expectedKeystoreFile.absolutePath) {
            Timber.w("Le chemin du Keystore par défaut a été surchargé ou est différent. Attendu: %s, Actuel: %s",
                expectedKeystoreFile.absolutePath, remoteContext.keyStoreFile.absolutePath)
        }
        Timber.i("Chemin du Keystore utilisé : %s", remoteContext.keyStoreFile.absolutePath)
    }
}
