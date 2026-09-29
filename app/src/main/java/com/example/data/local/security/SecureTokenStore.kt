package com.example.data.local.security

/**
 * Secure token storage abstraction.
 *
 * CRITICAL SECURITY CONSTRAINTS:
 * 1. Tokens must never be stored in plain DataStore preferences or git files.
 * 2. Tokens must never be printed to logs, crash reports, or UI strings.
 * 3. In production, this binds to Android Keystore / EncryptedSharedPreferences.
 */
interface SecureTokenStore {
    fun saveToken(key: String, token: String)
    fun getToken(key: String): String?
    fun hasToken(key: String): Boolean
    fun deleteToken(key: String)
    fun clearAll()
}

/**
 * Development token storage implementation.
 *
 * NOTE: This is an isolated, in-memory reference store used during development phases.
 * It strictly holds tokens in private JVM heap memory, never serializes to disk,
 * and suppresses all logging to prevent accidental token leakage.
 */
class InMemorySecureTokenStore : SecureTokenStore {

    private val tokenVault = mutableMapOf<String, String>()

    override fun saveToken(key: String, token: String) {
        if (key.isNotBlank() && token.isNotBlank()) {
            tokenVault[key] = token
        }
    }

    override fun getToken(key: String): String? {
        return tokenVault[key]
    }

    override fun hasToken(key: String): Boolean {
        return tokenVault.containsKey(key) && !tokenVault[key].isNullOrBlank()
    }

    override fun deleteToken(key: String) {
        tokenVault.remove(key)
    }

    override fun clearAll() {
        tokenVault.clear()
    }
}
