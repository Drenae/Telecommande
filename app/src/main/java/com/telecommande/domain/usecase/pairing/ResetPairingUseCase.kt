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

        val tvIpAddress = tvToReset.ipAddress
        val activeTv = settingsRepository.getActiveTvInfo()

        if (activeTv?.keystoreAlias == keystoreAlias && remoteRepository.isConnected.value) {
            Timber.d("Réinitialisation de l'appairage pour la TV active et connectée ($tvIpAddress), déconnexion...")
            remoteRepository.disconnectFromTv()
        }

        Timber.d("Suppression du keystore client pour réinitialisation de l'appairage.")
        val keystoreDeleted = pairingRepository.deleteKeystoreForReset()
        if (!keystoreDeleted) {
            Timber.e("Échec de la suppression du keystore pendant la réinitialisation de l'appairage pour $keystoreAlias.")
        }

        settingsRepository.removePairedTvByKeystoreAlias(keystoreAlias)
        Timber.d("TV avec alias $keystoreAlias supprimée de la base de données.")

        if (activeTv?.keystoreAlias == keystoreAlias) {
            Timber.d("Nettoyage de la TV active car elle a été réinitialisée.")
            settingsRepository.clearActiveTvInfo()
        }
    }
}