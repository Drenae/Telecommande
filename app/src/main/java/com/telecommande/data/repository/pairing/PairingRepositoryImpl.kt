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

    override suspend fun connectForPairing(hostAddress: String) {
        Timber.d("PairingRepo: Appel de androidRemoteTv.connect() avec l'adresse: %s", hostAddress)
        try {
            androidRemoteTv.connect(hostAddress)
        } catch (e: Exception) {
            Timber.e(e, "PairingRepo: Exception during connect to %s: %s", hostAddress, e.message)
        }
    }

    override suspend fun sendSecret(pin: String) {
        Timber.d("PairingRepo: Appel de androidRemoteTv.sendSecret() avec le PIN.")
        try {
            androidRemoteTv.sendSecret(pin)
        } catch (e: Exception) {
            Timber.e(e, "PairingRepo: Exception during sendSecret: %s", e.message)
        }
    }

    override fun isKeystorePairedInitially(): Boolean {
        val keystoreFile: File? = androidRemoteContext.keyStoreFile
        val isPaired = keystoreFile?.exists() == true && keystoreFile.length() > 0
        Timber.d("PairingRepo: isKeystorePairedInitially: %s", isPaired)
        return isPaired
    }

    override suspend fun deleteKeystoreForReset(): Boolean {
        return withContext(Dispatchers.IO) {
            val keystoreFile = androidRemoteContext.keyStoreFile
            if (keystoreFile.exists()) {
                try {
                    Timber.d("PairingRepo: Tentative de suppression du keystore.")
                    val deleted = keystoreFile.delete()
                    if (deleted) {
                        Timber.i("PairingRepo: Keystore supprimé avec succès.")
                    } else {
                        Timber.e("PairingRepo: Échec de la suppression du keystore.")
                    }
                    deleted
                } catch (e: SecurityException) {
                    Timber.e(e, "PairingRepo: Erreur de sécurité lors de la suppression du keystore: %s", e.message)
                    false
                }
            } else {
                Timber.i("PairingRepo: Keystore non trouvé, suppression non nécessaire.")
                true
            }
        }
    }
}