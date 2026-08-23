package com.telecommande.data.repository.pairing

import android.app.Application
import com.telecommande.core.AndroidRemoteContext
import com.telecommande.core.AndroidRemoteTv
import com.telecommande.data.repository.TvCoreEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import com.telecommande.core.event.AndroidTvEvent as CoreAndroidTvEvent

class PairingRepositoryImpl @Inject constructor(
    private val application: Application,
    private val androidRemoteTv: AndroidRemoteTv
) : PairingRepository {

    private val androidRemoteContext: AndroidRemoteContext = AndroidRemoteContext.getInstance(application)

    override val tvCoreEvents: Flow<TvCoreEvent> = androidRemoteTv.eventFlow
        .map { coreEvent ->
            Timber.d("PairingRepo: Raw CoreAndroidTvEvent: %s", coreEvent)
            when (coreEvent) {
                is CoreAndroidTvEvent.SessionCreated -> TvCoreEvent.SessionCreated
                is CoreAndroidTvEvent.SecretRequested -> TvCoreEvent.SecretRequested
                is CoreAndroidTvEvent.Paired -> TvCoreEvent.Paired(coreEvent.host, coreEvent.tvKeystoreAlias)
                is CoreAndroidTvEvent.ConnectingToRemote -> TvCoreEvent.ConnectingToRemote
                is CoreAndroidTvEvent.Error -> TvCoreEvent.Error(coreEvent.message)
                is CoreAndroidTvEvent.Disconnected -> TvCoreEvent.Disconnected
                is CoreAndroidTvEvent.Connected -> null
                is CoreAndroidTvEvent.VolumeUpdated -> null
                is CoreAndroidTvEvent.AppLinkLaunchSent -> null
            }
        }
        .filterNotNull()
        .onEach { Timber.d("PairingRepo: Emitting Filtered TvCoreEvent: %s", it.javaClass.simpleName) }

    override suspend fun connectForPairing(hostAddress: String, tvKeystoreAlias: String?) {
        Timber.d("PairingRepo: connexion vers %s (alias connu: %s)", hostAddress, tvKeystoreAlias != null)
        try {
            androidRemoteTv.connect(hostAddress, tvKeystoreAlias)
        } catch (e: Exception) {
            Timber.e(e, "PairingRepo: Exception during connect to %s: %s", hostAddress, e.message)
        }
    }

    override suspend fun sendSecret(pin: String) {
        try {
            androidRemoteTv.sendSecret(pin)
        } catch (e: Exception) {
            Timber.e(e, "PairingRepo: Exception during sendSecret: %s", e.message)
        }
    }

    override fun isKeystorePairedInitially(): Boolean {
        val keystoreFile: File? = androidRemoteContext.keyStoreFile
        return keystoreFile?.exists() == true && keystoreFile.length() > 0
    }

    override suspend fun deleteKeystoreForReset(): Boolean {
        return withContext(Dispatchers.IO) {
            val keystoreFile = androidRemoteContext.keyStoreFile
            if (keystoreFile.exists()) {
                try {
                    keystoreFile.delete()
                } catch (e: SecurityException) {
                    Timber.e(e, "PairingRepo: Erreur de sécurité lors de la suppression du keystore: %s", e.message)
                    false
                }
            } else {
                true
            }
        }
    }
}
