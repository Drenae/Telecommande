package com.telecommande.core.protocol

/**
 * Familles de protocoles prises en charge par le core.
 *
 * Une marque de TV n'est pas un protocole : plusieurs fabricants peuvent utiliser Android TV,
 * Google TV, Roku, Fire TV ou leur propre OS selon le modèle.
 */
enum class TvProtocolType {
    ANDROID_TV_REMOTE_V2,
    ANDROID_TV_REMOTE_V1,
    SAMSUNG_TIZEN,
    LG_WEBOS,
    ROKU_ECP,
    FIRE_TV,
    APPLE_TV,
    UNKNOWN
}

/** Capacités qu'un adaptateur peut exposer à l'UI. */
enum class TvCapability {
    NAVIGATION,
    VOLUME,
    MUTE,
    POWER_OFF,
    POWER_ON,
    MEDIA,
    TEXT_INPUT,
    APPLICATIONS,
    SOURCES
}

/** Commandes communes indépendantes du protocole réseau sous-jacent. */
enum class TvCommand {
    UP, DOWN, LEFT, RIGHT, OK,
    BACK, HOME,
    VOLUME_UP, VOLUME_DOWN, MUTE,
    POWER,
    PLAY_PAUSE, STOP, REWIND, FAST_FORWARD
}

/**
 * Contrat d'un protocole TV.
 *
 * Les implémentations Samsung/LG/Roku pourront être ajoutées sans faire dépendre l'application
 * de WebSocket, SSDP, HTTP ECP ou de leurs commandes propriétaires.
 */
interface TvProtocol {
    val type: TvProtocolType
    val capabilities: Set<TvCapability>

    fun supports(capability: TvCapability): Boolean = capability in capabilities
}

/** Informations persistables permettant de choisir l'adaptateur adapté à une TV. */
data class TvProtocolDescriptor(
    val type: TvProtocolType,
    val stableId: String? = null,
    val host: String? = null,
    val port: Int? = null
)
