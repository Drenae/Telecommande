package com.telecommande.domain.usecase.connection

import com.telecommande.data.repository.remote.RemoteRepository
import javax.inject.Inject

class DisconnectFromTvUseCase @Inject constructor(
    private val remoteRepository: RemoteRepository
) {
    suspend operator fun invoke() {
        remoteRepository.disconnectFromTv()
    }
}