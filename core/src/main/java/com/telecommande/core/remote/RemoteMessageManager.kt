package com.telecommande.core.remote

import com.telecommande.core.wire.MessageManager
import timber.log.Timber

class RemoteMessageManager : MessageManager() {

    fun createRemoteConfigure(
        code: Int,
        model: String,
        vendor: String,
        unknown1: Int,
        unknown2: String
    ): ByteArray {
        Timber.d("Création du message RemoteConfigure: code=%d, model=%s, vendor=%s", code, model, vendor)
        val deviceInfo = Remotemessage.RemoteDeviceInfo.newBuilder()
            .setModel(model)
            .setVendor(vendor)
            .setUnknown1(unknown1)
            .setUnknown2(unknown2)
            .setPackageName("androidtv-remote")
            .setAppVersion("1.0.0")
            .build()
        val remoteConfigureProto = Remotemessage.RemoteConfigure.newBuilder()
            .setCode1(code)
            .setDeviceInfo(deviceInfo)
            .build()
        return createRemoteConfigureMessage(remoteConfigureProto)
    }

    private fun createRemoteConfigureMessage(remoteConfigureProto: Remotemessage.RemoteConfigure): ByteArray {
        val remoteMessageProto = Remotemessage.RemoteMessage.newBuilder()
            .setRemoteConfigure(remoteConfigureProto)
            .build()
        return addLengthAndCreate(remoteMessageProto.toByteArray())
    }

    fun createRemoteActive(code: Int): ByteArray {
        Timber.d("Création du message RemoteActive: code=%d", code)
        val remoteMessageProto = Remotemessage.RemoteMessage.newBuilder()
            .setRemoteSetActive(
                Remotemessage.RemoteSetActive.newBuilder()
                    .setActive(code)
            )
            .build()
        return addLengthAndCreate(remoteMessageProto.toByteArray())
    }

    fun createPingResponse(val1: Int): ByteArray {
        Timber.v("Création de la réponse Ping: val1=%d", val1)
        val remotePingResponseProto = Remotemessage.RemotePingResponse.newBuilder()
            .setVal1(val1)
            .build()
        val remoteMessageProto = Remotemessage.RemoteMessage.newBuilder()
            .setRemotePingResponse(remotePingResponseProto)
            .build()
        return addLengthAndCreate(remoteMessageProto.toByteArray())
    }

    fun createPowerCommand(): ByteArray {
        Timber.d("Création de la commande Power.")
        return createKeyCommand(Remotemessage.RemoteKeyCode.KEYCODE_POWER, Remotemessage.RemoteDirection.SHORT)
    }

    @Deprecated("This message seems to be empty or incomplete in the original. Review protocol if used.")
    fun createAdjustVolumeLevelCommand(volume: Int): ByteArray {
        Timber.w("Création de la commande AdjustVolumeLevel (potentiellement vide/incomplète): volume=%d", volume)
        val remoteMessageProto = Remotemessage.RemoteMessage.newBuilder()
            .setRemoteAdjustVolumeLevel(
                Remotemessage.RemoteAdjustVolumeLevel.newBuilder()
            )
            .build()
        return addLengthAndCreate(remoteMessageProto.toByteArray())
    }

    fun createKeyCommand(keyCode: Remotemessage.RemoteKeyCode, remoteDirection: Remotemessage.RemoteDirection): ByteArray {
        Timber.d("Création de la commande Key: keyCode=%s, direction=%s", keyCode, remoteDirection)
        val remoteKeyInjectProto = Remotemessage.RemoteKeyInject.newBuilder()
            .setKeyCode(keyCode)
            .setDirection(remoteDirection)
            .build()
        val remoteMessageProto = Remotemessage.RemoteMessage.newBuilder()
            .setRemoteKeyInject(remoteKeyInjectProto)
            .build()
        return addLengthAndCreate(remoteMessageProto.toByteArray())
    }

    fun createAppLinkLaunchRequest(appLink: String): ByteArray {
        Timber.d("Création du message RemoteAppLinkLaunchRequest: appLink='%s'", appLink)
        val appLinkLaunchRequestProto = Remotemessage.RemoteAppLinkLaunchRequest.newBuilder()
            .setAppLink(appLink)
            .build()
        val remoteMessageProto = Remotemessage.RemoteMessage.newBuilder()
            .setRemoteAppLinkLaunchRequest(appLinkLaunchRequestProto)
            .build()
        return addLengthAndCreate(remoteMessageProto.toByteArray())
    }
}
