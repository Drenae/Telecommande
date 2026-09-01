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

    /**
     * Émis lorsque la TV ouvre un champ de saisie et demande un clavier distant.
     * Les compteurs/positions viennent directement du protocole Android TV Remote v2
     * et seront réutilisés pour synchroniser l'IME du téléphone avec celui de la TV.
     */
    data class TextInputRequested(
        val value: String,
        val selectionStart: Int,
        val selectionEnd: Int,
        val fieldCounter: Int,
        val label: String?
    ) : RemoteEvent()
}