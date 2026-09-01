package com.telecommande.core.event

sealed class AndroidTvEvent {
    data object ConnectingToRemote : AndroidTvEvent()
    data object Connected : AndroidTvEvent()
    data object SessionCreated : AndroidTvEvent()
    data object SecretRequested : AndroidTvEvent()
    data class Paired(val host: String, val tvKeystoreAlias: String) : AndroidTvEvent()
    data object Disconnected : AndroidTvEvent()
    data class Error(val message: String) : AndroidTvEvent()
    data class VolumeUpdated(val level: Int, val max: Int, val muted: Boolean) : AndroidTvEvent()
    data class AppLinkLaunchSent(val appLink: String) : AndroidTvEvent()

    data class TextInputRequested(
        val value: String,
        val selectionStart: Int,
        val selectionEnd: Int,
        val fieldCounter: Int,
        val label: String?
    ) : AndroidTvEvent()
}