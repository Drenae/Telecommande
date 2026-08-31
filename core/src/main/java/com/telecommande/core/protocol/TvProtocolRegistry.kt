package com.telecommande.core.protocol

/** Point central de résolution des adaptateurs de protocole. */
object TvProtocolRegistry {
    private val protocols: Map<TvProtocolType, TvProtocol> = listOf(
        AndroidTvRemoteV2Protocol
    ).associateBy { it.type }

    fun get(type: TvProtocolType): TvProtocol? = protocols[type]

    fun require(type: TvProtocolType): TvProtocol =
        protocols[type] ?: error("Protocole TV non implémenté : $type")

    fun supportedTypes(): Set<TvProtocolType> = protocols.keys
}
