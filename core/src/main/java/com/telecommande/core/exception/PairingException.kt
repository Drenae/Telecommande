package com.telecommande.core.exception

import timber.log.Timber

class PairingException : Exception {
    constructor(message: String) : super(message) {
        Timber.v("PairingException créée: %s", message)
    }
    constructor(message: String, cause: Throwable) : super(message, cause) {
        Timber.v(cause, "PairingException créée avec cause: %s", message)
    }
}