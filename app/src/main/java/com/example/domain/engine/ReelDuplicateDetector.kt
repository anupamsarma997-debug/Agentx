package com.example.domain.engine

import java.security.MessageDigest
import java.util.Locale

object ReelDuplicateDetector {

    /**
     * Computes a deterministic SHA-256 hash for a Reel concept using:
     * - normalized source ID
     * - reel type
     * - hook
     * - main topic
     */
    fun computeHash(
        sourceId: String?,
        reelType: String,
        hook: String,
        topic: String
    ): String {
        val normSource = (sourceId ?: "").trim().lowercase(Locale.ROOT)
        val normType = reelType.trim().uppercase(Locale.ROOT)
        val normHook = normalizeText(hook)
        val normTopic = normalizeText(topic)

        val raw = "$normSource|$normType|$normHook|$normTopic"
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(raw.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun normalizeText(text: String): String {
        return text
            .lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
