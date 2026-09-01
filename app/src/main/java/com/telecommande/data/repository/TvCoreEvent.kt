package com.telecommande.data.repository

sealed class TvCoreEvent {
    data object SessionCreated : TvCoreEvent()
    data object SecretRequested : TvCoreEvent()
    data class Paired(val host: String, val tvKeystoreAlias: String) : TvCoreEvent()
    data object ConnectingToRemote : TvCoreEvent()
    data object Connected : TvCoreEvent()
    data object Disconnected : TvCoreEvent()
    data class Error(val message: String) : TvCoreEvent()
    data class VolumeUpdated(val level: Int, val max: Int, val muted: Boolean) : TvCoreEvent()
    data class AppLinkLaunchSent(val appLink: String) : TvCoreEvent()
    data class TextInputRequested(
        val value: String,
        val selectionStart: Int,
        val selectionEnd: Int,
        val fieldCounter: Int,
        val label: String?
    ) : TvCoreEvent()
}
