package com.telecommande.domain.usecase.remote

import com.telecommande.data.repository.remote.RemoteRepository
import javax.inject.Inject

class LaunchAppUseCase @Inject constructor(
    private val remoteRepository: RemoteRepository
) {
    operator fun invoke(appLink: String) {
        remoteRepository.launchApplication(appLink)
    }
}