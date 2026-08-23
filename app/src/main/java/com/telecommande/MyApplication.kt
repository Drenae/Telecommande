package com.telecommande

import android.app.Application
import com.telecommande.core.AndroidRemoteContext
import com.telecommande.core.CoreWarmup
import dagger.hilt.android.HiltAndroidApp
import org.bouncycastle.jce.provider.BouncyCastleProvider
import timber.log.Timber
import java.security.Security

@HiltAndroidApp
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        initializeBouncyCastle()
        AndroidRemoteContext.getInstance(this)
        CoreWarmup.prewarm()
    }

    private fun initializeBouncyCastle() {
        try {
            Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME)
            Security.insertProviderAt(BouncyCastleProvider(), 1)
            Timber.d("BouncyCastle provider initialized")
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize BouncyCastle provider")
        }
    }
}
