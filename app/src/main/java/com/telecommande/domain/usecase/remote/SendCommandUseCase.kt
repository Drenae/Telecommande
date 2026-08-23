package com.telecommande.domain.usecase.remote

import com.telecommande.core.remote.Remotemessage
import com.telecommande.data.repository.remote.RemoteRepository
import javax.inject.Inject

class SendCommandUseCase @Inject constructor(
    private val remoteRepository: RemoteRepository
) {
    suspend operator fun invoke(
        keyCode: Remotemessage.RemoteKeyCode,
        action: Remotemessage.RemoteDirection = Remotemessage.RemoteDirection.SHORT
    ) {
        remoteRepository.sendCommand(keyCode, action)
    }
}