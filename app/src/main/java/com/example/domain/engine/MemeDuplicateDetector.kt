package com.example.domain.engine

import java.security.MessageDigest
import java.util.Locale

object MemeDuplicateDetector {

    /**
     * Computes a deterministic SHA-256 hash based on normalized topic, setupText, and punchlineText.
     */
    fun computeHash(topic: String, setupText: String, punchlineText: String): String {
        val normTopic = normalize(topic)
        val normSetup = normalize(setupText)
        val normPunchline = normalize(punchlineText)

        val combined = "$normTopic|$normSetup|$normPunchline"
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(combined.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun normalize(text: String): String {
        return text.lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9\\s]"), "") // remove punctuation
            .replace(Regex("\\s+"), " ")        // collapse whitespace
            .trim()
    }
}
