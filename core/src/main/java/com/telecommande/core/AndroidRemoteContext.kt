package com.telecommande.core

import android.content.Context
import android.os.Build
import timber.log.Timber
import java.io.File
import java.util.Locale

class AndroidRemoteContext private constructor(applicationContext: Context) {

    var serviceName: String = "com.telecommande.core/Telecommande"
    var clientName: String = defaultDeviceName
    var keyStoreFile: File = File(applicationContext.filesDir, "androidtv.keystore")
    var keyStorePass: CharArray = "KeyStore_Password".toCharArray()

    init {
        Timber.d("AndroidRemoteContext initialisé. Client: '%s', Fichier Keystore: '%s'", clientName, keyStoreFile.absolutePath)
    }

    companion object {
        @Volatile
        private var instance: AndroidRemoteContext? = null

        fun getInstance(applicationContext: Context): AndroidRemoteContext {
            return instance ?: synchronized(this) {
                instance ?: AndroidRemoteContext(applicationContext.applicationContext).also {
                    Timber.i("Nouvelle instance de AndroidRemoteContext créée et configurée.")
                    instance = it
                }
            }
        }

        fun getInstance(): AndroidRemoteContext {
            return instance ?: throw IllegalStateException(
                "AndroidRemoteContext non initialisé. Appelez getInstance(context) au moins une fois."
            )
        }

        private val defaultDeviceName: String
            get() {
                val manufacturer = Build.MANUFACTURER
                val model = Build.MODEL
                val deviceName = if (model.startsWith(manufacturer, ignoreCase = true)) {
                    capitalize(model)
                } else {
                    "${capitalize(manufacturer)} ${capitalize(model)}"
                }
                Timber.v("Nom de l'appareil par défaut déterminé : '%s'", deviceName)
                return deviceName
            }

        private fun capitalize(s: String?): String {
            if (s.isNullOrEmpty()) {
                return ""
            }
            val firstChar = s[0]
            return if (Character.isUpperCase(firstChar)) {
                s
            } else {
                s.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                }
            }
        }
    }
}