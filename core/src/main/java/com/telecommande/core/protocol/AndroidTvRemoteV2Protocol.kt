package com.telecommande.core.protocol

/**
 * Adaptateur de capacités du protocole Android TV / Google TV Remote Service v2 déjà utilisé
 * par l'application. Cette première étape n'altère pas le chemin de connexion V1 existant :
 * elle fournit le contrat commun sur lequel les prochains protocoles viendront se brancher.
 */
object AndroidTvRemoteV2Protocol : TvProtocol {
    override val type = TvProtocolType.ANDROID_TV_REMOTE_V2

    override val capabilities: Set<TvCapability> = setOf(
        TvCapability.NAVIGATION,
        TvCapability.VOLUME,
        TvCapability.MUTE,
        TvCapability.POWER_OFF,
        TvCapability.MEDIA,
        TvCapability.APPLICATIONS
    )
}
