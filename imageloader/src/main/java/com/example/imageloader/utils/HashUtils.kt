package com.example.imageloader.utils

import java.security.MessageDigest

/**
 * Utility class for hashing strings.
 */
object HashUtils {
    /**
     * Generates an MD5 hash of the given string.
     * Used for generating unique filenames for the disk cache.
     */
    fun md5(s: String): String {
        val digest = MessageDigest.getInstance("MD5")
        digest.update(s.toByteArray())
        val messageDigest = digest.digest()

        val hexString = StringBuilder()
        for (aMessageDigest in messageDigest) {
            var h = Integer.toHexString(0xFF and aMessageDigest.toInt())
            while (h.length < 2) h = "0$h"
            hexString.append(h)
        }
        return hexString.toString()
    }
}
