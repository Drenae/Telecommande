package com.telecommande.core.event

import java.security.cert.X509Certificate

sealed class PairingEvent {
    data object SessionCreated : PairingEvent()
    data object SecretRequested : PairingEvent()
    data class Paired(val serverCertificate: X509Certificate?) : PairingEvent()
    data object SessionEnded : PairingEvent()
    data class Error(val message: String) : PairingEvent()
    data class Log(val message: String) : PairingEvent()
}