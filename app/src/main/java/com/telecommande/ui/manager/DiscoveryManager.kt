package com.telecommande.ui.manager

import com.telecommande.core.discovery.DiscoveredTv
import com.telecommande.data.repository.DiscoveryEvent
import com.telecommande.data.repository.discovery.DiscoveryRepository
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import timber.log.Timber
import javax.inject.Inject

data class DiscoveryState(
    val discoveredTvs: List<DiscoveredTv> = emptyList(),
    val isDiscovering: Boolean = false,
    val statusMessage: String = "",
    val errorMessage: String? = null
)

@ViewModelScoped
class DiscoveryManager @Inject constructor(
    private val discoveryRepository: DiscoveryRepository
) {
    private val _state = MutableStateFlow(DiscoveryState())
    val state: StateFlow<DiscoveryState> = _state.asStateFlow()

    private var discoveryEventsJob: Job? = null

    fun startDiscovery(scope: CoroutineScope) {
        if (state.value.isDiscovering) return

        _state.update {
            it.copy(
                isDiscovering = true,
                discoveredTvs = emptyList(),
                statusMessage = "Recherche de TV...",
                errorMessage = null
            )
        }

        discoveryEventsJob?.cancel()
        discoveryEventsJob = discoveryRepository.discoveryEvents
            .onEach { event ->
                _state.update { currentState ->
                    when (event) {
                        is DiscoveryEvent.TvFound -> {
                            val newList = currentState.discoveredTvs
                                .filterNot { it.serviceName == event.tv.serviceName }
                                .toMutableList()
                            newList.add(0, event.tv)
                            currentState.copy(discoveredTvs = newList)
                        }

                        is DiscoveryEvent.TvLost -> currentState.copy(
                            discoveredTvs = currentState.discoveredTvs
                                .filterNot { it.serviceName == event.tv.serviceName }
                        )

                        is DiscoveryEvent.DiscoveryStarted -> currentState.copy(
                            isDiscovering = true,
                            statusMessage = "Recherche en cours..."
                        )

                        is DiscoveryEvent.DiscoveryStopped -> currentState.copy(
                            isDiscovering = false,
                            statusMessage = "Recherche arrêtée."
                        )

                        is DiscoveryEvent.Error -> currentState.copy(
                            isDiscovering = false,
                            statusMessage = "Erreur de recherche: ${event.message ?: "Inconnue"}",
                            errorMessage = event.message ?: "Erreur de recherche inconnue"
                        )
                    }
                }
            }
            .catch { e ->
                Timber.e(e, "DiscoveryManager : Erreur critique lors de la découverte")
                _state.update {
                    it.copy(
                        isDiscovering = false,
                        statusMessage = "Erreur critique de recherche.",
                        errorMessage = e.localizedMessage ?: "Erreur inconnue"
                    )
                }
            }
            .launchIn(scope)

        discoveryRepository.startTvDiscovery()
    }

    fun stopDiscovery() {
        discoveryRepository.stopTvDiscovery()
        discoveryEventsJob?.cancel()
        discoveryEventsJob = null

        if (_state.value.isDiscovering) {
            _state.update {
                it.copy(
                    isDiscovering = false,
                    statusMessage = "Recherche arrêtée."
                )
            }
        }
    }

    fun clearErrorMessage() {
        _state.update { it.copy(errorMessage = null) }
    }

    fun cleanup() {
        discoveryEventsJob?.cancel()
        discoveryEventsJob = null
    }
}
