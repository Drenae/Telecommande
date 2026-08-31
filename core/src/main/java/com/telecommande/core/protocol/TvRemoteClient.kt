package com.telecommande.core.protocol

import kotlinx.coroutines.flow.StateFlow

/**
 * Contrat commun de contrôle d'une TV, indépendant du transport utilisé par son protocole.
 *
 * L'appairage/authentification reste volontairement spécifique aux adaptateurs : Samsung, LG,
 * Android TV, etc. n'ont pas le même mécanisme. Une fois la session prête, l'application peut
 * en revanche piloter la TV avec cette API commune.
 */
interface TvRemoteClient {
    val protocol: TvProtocol
    val isConnected: StateFlow<Boolean>

    fun connect(host: String, credentialId: String? = null)
    fun disconnect()
    fun sendCommand(command: TvCommand)
    fun launchApplication(appLink: String)
    fun cleanup()
}
