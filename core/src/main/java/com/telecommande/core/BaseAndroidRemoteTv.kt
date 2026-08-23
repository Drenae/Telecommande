package com.telecommande.core

import java.io.File

abstract class BaseAndroidRemoteTv {
    protected val androidRemoteContext: AndroidRemoteContext = AndroidRemoteContext.getInstance()

    var serviceName: String
        get() = androidRemoteContext.serviceName
        set(value) {
            androidRemoteContext.serviceName = value
        }

    var clientName: String
        get() = androidRemoteContext.clientName
        set(value) {
            androidRemoteContext.clientName = value
        }

    var keyStoreFile: File
        get() = androidRemoteContext.keyStoreFile
        set(value) {
            androidRemoteContext.keyStoreFile = value
        }

    var keyStorePass: CharArray
        get() = androidRemoteContext.keyStorePass
        set(value) {
            androidRemoteContext.keyStorePass = value
        }

    fun setKeyStorePassAsString(keyStorePassString: String) {
        androidRemoteContext.keyStorePass = keyStorePassString.toCharArray()
    }
}
