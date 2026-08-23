package com.telecommande.domain.usecase.pairing

import com.telecommande.data.repository.SettingsRepository
import com.telecommande.data.repository.pairing.PairingRepository
import com.telecommande.data.repository.remote.RemoteRepository
import timber.log.Timber
import javax.inject.Inject

class ResetPairingUseCase @Inject constructor(
    private val remoteRepository: RemoteRepository,
    private val pairingRepository: PairingRepository,
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(keystoreAlias: String) {
        val tvToReset = settingsRepository.getPairedTvByKeystoreAlias(keystoreAlias)

        if (tvToReset == null) {
            Timber.w("Tentative de réinitialisation pour un alias de keystore inconnu : $keystoreAlias")
            return
        }

        val activeTv = settingsRepository.getActiveTvInfo()

        if (activeTv?.keystoreAlias == keystoreAlias && remoteRepository.isConnected.value) {
            remoteRepository.disconnectFromTv()
        }

        val certificateRemoved = pairingRepository.removePairedTvCertificate(keystoreAlias)
        if (!certificateRemoved) {
            Timber.w("Aucun certificat trouvé pour la TV $keystoreAlias lors de la réinitialisation.")
        }

        settingsRepository.removePairedTvByKeystoreAlias(keystoreAlias)

        if (activeTv?.keystoreAlias == keystoreAlias) {
            settingsRepository.clearActiveTvInfo()
        }
    }
}
