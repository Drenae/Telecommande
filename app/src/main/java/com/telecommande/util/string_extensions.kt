package com.telecommande.util

fun String.containsAnyOf(substrings: List<String>, ignoreCase: Boolean = false): Boolean {
    return substrings.any { this.contains(it, ignoreCase) }
}