package com.telecommande.core.event

sealed class RemoteEvent {
    data object Connected : RemoteEvent()
    data object SslError : RemoteEvent()
    data object Disconnected : RemoteEvent()
    data class Error(val message: String) : RemoteEvent()

    data class VolumeStateChanged(
        val level: Int,
        val max: Int,
        val muted: Boolean,
        val deviceName: String?
    ) : RemoteEvent()
}