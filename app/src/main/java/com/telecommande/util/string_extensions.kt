package com.telecommande.util

fun String.containsAnyOf(
    substrings: List<String>,
    ignoreCase: Boolean = false
): Boolean {
    return substrings.any { substring ->
        contains(substring, ignoreCase)
    }
}

fun String.normalizePairingPin(): String {
    return uppercase()
        .filter { character ->
            character in 'A'..'Z' || character in '0'..'9'
        }
}
