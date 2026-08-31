package com.telecommande.core.discovery

import com.telecommande.core.protocol.TvProtocolType

/**
 * Moteur de découverte propre à une famille de protocole TV.
 *
 * L'orchestrateur peut démarrer plusieurs providers en parallèle sans connaître
 * leur mécanisme réseau (mDNS, SSDP, scan réseau, etc.).
 */
interface TvDiscoveryProvider {
    val protocolType: TvProtocolType
    val name: String

    fun start()
    fun stop()
}
