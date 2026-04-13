package com.posdata.app.utils

import java.security.MessageDigest

/**
 * Utility object providing cryptographic hashing functions.
 */
object HashUtils {

    /**
     * Computes the SHA-512 hash of the given input string.
     *
     * Used to hash user passwords before storing or transmitting them,
     * ensuring plain-text passwords are never persisted or sent over the network.
     *
     * @param input Plain-text string to hash.
     * @return Lowercase hexadecimal representation of the SHA-512 digest.
     */
    fun sha512(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-512").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}