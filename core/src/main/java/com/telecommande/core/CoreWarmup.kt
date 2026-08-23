package com.telecommande.core

import com.telecommande.core.ssl.KeyStoreManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Prepares expensive core resources while the application UI is starting.
 * This keeps the first TV connection from paying the full KeyStore startup cost.
 */
object CoreWarmup {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var started = false

    fun prewarm() {
        if (started) return
        synchronized(this) {
            if (started) return
            started = true
        }

        scope.launch {
            try {
                KeyStoreManager().hasServerIdentityAlias()
                Timber.d("Core KeyStore prewarmed")
            } catch (e: Exception) {
                // Connection code will retry normally if warmup could not complete.
                Timber.w(e, "Core KeyStore warmup failed")
            }
        }
    }
}