package com.telecommande.data.repository.pairing

import android.app.Application
import com.telecommande.core.AndroidRemoteContext
import com.telecommande.core.AndroidRemoteTv
import com.telecommande.core.ssl.TvCertificateStore
import com.telecommande.data.repository.TvCoreEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import com.telecommande.core.event.AndroidTvEvent as CoreAndroidTvEvent

class PairingRepositoryImpl @Inject constructor(
    application: Application,
    private val androidRemoteTv: AndroidRemoteTv
) : PairingRepository {

    private val androidRemoteContext: AndroidRemoteContext = AndroidRemoteContext.getInstance(application)

    override val tvCoreEvents: Flow<TvCoreEvent> = androidRemoteTv.eventFlow
        .map { coreEvent ->
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

    override suspend fun connectForPairing(hostAddress: String, tvKeystoreAlias: String?) {
        androidRemoteTv.connect(hostAddress, tvKeystoreAlias)
    }

    override suspend fun sendSecret(pin: String) {
        androidRemoteTv.sendSecret(pin)
    }

    override fun isKeystorePairedInitially(): Boolean {
        val keystoreFile: File? = androidRemoteContext.keyStoreFile
        return keystoreFile?.exists() == true && keystoreFile.length() > 0
    }

    override suspend fun removePairedTvCertificate(keystoreAlias: String): Boolean {
        return withContext(Dispatchers.IO) {
            TvCertificateStore.removePairedTvCertificate(keystoreAlias)
        }
    }
}
