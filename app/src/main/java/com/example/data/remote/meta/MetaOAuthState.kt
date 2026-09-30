package com.example.data.remote.meta

import com.example.data.local.security.SecureTokenStore
import java.security.SecureRandom
import java.util.Base64

/**
 * OAuth state management for CSRF attack prevention.
 *
 * CRITICAL SECURITY:
 * - State is cryptographically random
 * - State is stored securely before OAuth redirect
 * - Callback state is validated exactly
 * - State is cleared after use or failure
 * - Mismatched state is rejected
 */
class MetaOAuthState(private val tokenStore: SecureTokenStore) {

    companion object {
        private const val STATE_KEY = "oauth_state_nonce"
        private const val STATE_LENGTH_BYTES = 32 // 256 bits
    }

    /**
     * Generate a cryptographically secure random state and store it.
     * Returns the generated state for inclusion in OAuth URL.
     *
     * CRITICAL: This state MUST be included in the authorization request
     * and validated in the callback before accepting any authorization code.
     */
    fun generateAndStoreState(): String {
        val random = SecureRandom()
        val stateBytes = ByteArray(STATE_LENGTH_BYTES)
        random.nextBytes(stateBytes)
        
        // URL-safe base64 encoding for OAuth parameter
        val state = Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(stateBytes)
        
        // Store securely for later validation
        tokenStore.saveToken(STATE_KEY, state)
        
        return state
    }

    /**
     * Validate that the callback state matches the stored pending state.
     *
     * Returns true if:
     * - A stored state exists (OAuth request was initiated)
     * - The received state matches exactly
     *
     * Returns false if:
     * - No stored state exists (suspicious/unsolicited callback)
     * - State mismatch (possible CSRF attack)
     *
     * After validation (success or failure), state is cleared to prevent replay attacks.
     */
    fun validateAndClearState(receivedState: String?): Boolean {
        val storedState = tokenStore.getToken(STATE_KEY)
        
        // Clear state regardless of validation result (prevent replay)
        tokenStore.deleteToken(STATE_KEY)
        
        // State validation
        if (storedState.isNullOrBlank()) {
            // No pending OAuth request - reject this callback
            return false
        }
        
        if (receivedState.isNullOrBlank()) {
            // Missing state parameter - reject
            return false
        }
        
        // Constant-time comparison to prevent timing attacks
        return secureEquals(storedState, receivedState)
    }

    /**
     * Constant-time string comparison to prevent timing attacks.
     */
    private fun secureEquals(a: String, b: String): Boolean {
        if (a.length != b.length) {
            return false
        }
        
        var result = 0
        for (i in a.indices) {
            result = result or (a[i].code xor b[i].code)
        }
        
        return result == 0
    }

    /**
     * Clear the stored state without validation.
     * Use this on permanent failure or user cancellation.
     */
    fun clearState() {
        tokenStore.deleteToken(STATE_KEY)
    }
}
