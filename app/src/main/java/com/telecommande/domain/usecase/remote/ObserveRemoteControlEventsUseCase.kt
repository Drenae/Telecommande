package com.telecommande.domain.usecase.remote

import com.telecommande.data.repository.TvCoreEvent
import com.telecommande.data.repository.remote.RemoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveRemoteControlEventsUseCase @Inject constructor(
    private val remoteRepository: RemoteRepository
) {
    operator fun invoke(): Flow<TvCoreEvent> {
        return remoteRepository.tvCoreEvents
    }
}
