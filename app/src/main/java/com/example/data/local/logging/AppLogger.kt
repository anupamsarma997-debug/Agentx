package com.example.data.local.logging

import android.util.Log
import com.example.data.local.dao.AppLogDao
import com.example.data.local.entity.AppLogEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.regex.Pattern

/**
 * Production In-App Logging utility with security-enforced data scrubbing.
 *
 * CRITICAL SECURITY INVARIANT:
 * No secrets, Facebook access tokens, Gemini API keys, refresh tokens, or passwords
 * are EVER stored in the database or displayed on screen.
 */
object AppLogger {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var dao: AppLogDao? = null

    // Patterns matching sensitive tokens
    private val META_TOKEN_PATTERN = Pattern.compile("EAA[A-Za-z0-9_-]{20,}")
    private val GOOGLE_API_KEY_PATTERN = Pattern.compile("AIzaSy[A-Za-z0-9_-]{25,}")
    private val GENERIC_BEARER_PATTERN = Pattern.compile("(?i)Bearer\\s+[A-Za-z0-9._~+/-]{15,}")
    private val ACCESS_TOKEN_KV_PATTERN = Pattern.compile("(?i)(access_token|refresh_token|client_secret|password|api_key)=[^&\\s]+")

    fun init(appLogDao: AppLogDao) {
        dao = appLogDao
    }

    fun info(feature: String, operation: String, message: String) {
        log("INFO", feature, operation, message, null)
    }

    fun warn(feature: String, operation: String, message: String, throwable: Throwable? = null) {
        log("WARN", feature, operation, message, throwable)
    }

    fun error(feature: String, operation: String, message: String, throwable: Throwable? = null) {
        log("ERROR", feature, operation, message, throwable)
    }

    private fun log(
        level: String,
        feature: String,
        operation: String,
        message: String,
        throwable: Throwable?
    ) {
        val sanitizedMsg = sanitize(message)
        val fullMsg = if (throwable != null) {
            val exMsg = sanitize(throwable.localizedMessage ?: throwable::class.java.simpleName)
            "$sanitizedMsg | Cause: $exMsg"
        } else {
            sanitizedMsg
        }

        // Standard Android Logcat output (scrubbed)
        when (level) {
            "ERROR" -> Log.e("SocialAgent::$feature", "[$operation] $fullMsg")
            "WARN" -> Log.w("SocialAgent::$feature", "[$operation] $fullMsg")
            else -> Log.i("SocialAgent::$feature", "[$operation] $fullMsg")
        }

        // Bounded database persistence (max 200 logs to prevent unbounded DB growth)
        val currentDao = dao ?: return
        scope.launch {
            try {
                currentDao.insert(
                    AppLogEntity(
                        level = level,
                        feature = feature,
                        operation = operation,
                        message = fullMsg
                    )
                )
                // Keep database size strictly bounded
                currentDao.trimLogs(keepCount = 200)
            } catch (_: Exception) {
                // Logging failure must never crash the app
            }
        }
    }

    /**
     * Aggressively scrubs secrets, access tokens, API keys and credentials.
     */
    fun sanitize(input: String): String {
        if (input.isBlank()) return ""
        var result = input
        result = META_TOKEN_PATTERN.matcher(result).replaceAll("[META_TOKEN_REDACTED]")
        result = GOOGLE_API_KEY_PATTERN.matcher(result).replaceAll("[API_KEY_REDACTED]")
        result = GENERIC_BEARER_PATTERN.matcher(result).replaceAll("Bearer [REDACTED]")
        result = ACCESS_TOKEN_KV_PATTERN.matcher(result).replaceAll("$1=[REDACTED]")
        return result
    }
}
