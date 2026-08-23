package com.telecommande.domain.usecase.connection

import com.telecommande.data.repository.remote.RemoteRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class ObserveTvConnectionStateUseCase @Inject constructor(
    private val remoteRepository: RemoteRepository
) {
    operator fun invoke(): StateFlow<Boolean> {
        return remoteRepository.isConnected
    }
}