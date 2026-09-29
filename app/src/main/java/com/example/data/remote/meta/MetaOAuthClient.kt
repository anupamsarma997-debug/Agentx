package com.example.data.remote.meta

import android.net.Uri
import com.example.data.local.security.SecureTokenStore
import com.example.data.model.meta.FacebookPageInfo
import com.example.data.model.meta.InstagramAccountInfo
import com.example.data.model.meta.InstagramAccountType

/**
 * Official Meta OAuth configuration requirements:
 * 1. Meta Developer App registered on https://developers.facebook.com
 * 2. Facebook Login for Business product enabled
 * 3. Required permissions requested:
 *    - pages_show_list
 *    - pages_read_engagement
 *    - pages_manage_posts
 *    - instagram_basic
 *    - instagram_content_publish
 * 4. Valid redirect URI matching app manifest intent-filter: socialagent://meta-callback
 */
data class MetaOAuthConfig(
    val appId: String = "",
    val redirectUri: String = "socialagent://meta-callback",
    val requiredScopes: List<String> = listOf(
        "pages_show_list",
        "pages_read_engagement",
        "pages_manage_posts",
        "instagram_basic",
        "instagram_content_publish"
    )
) {
    val isConfigured: Boolean
        get() = appId.isNotBlank() &&
                !appId.startsWith("YOUR_") &&
                redirectUri.isNotBlank()
}

sealed interface MetaOAuthResult {
    data class Success(
        val facebookPage: FacebookPageInfo,
        val instagramAccount: InstagramAccountInfo?
    ) : MetaOAuthResult

    data class Error(val message: String) : MetaOAuthResult
    data object PermissionDenied : MetaOAuthResult
    data class ConfigurationRequired(val message: String) : MetaOAuthResult
    data object UserCancelled : MetaOAuthResult
}

class MetaOAuthClient(
    private val config: MetaOAuthConfig = MetaOAuthConfig(),
    private val tokenStore: SecureTokenStore
) {

    companion object {
        const val OAUTH_DIALOG_URL = "https://www.facebook.com/v19.0/dialog/oauth"
        const val TOKEN_KEY_PAGE_ACCESS = "meta_page_access_token"
        const val TOKEN_KEY_USER_ACCESS = "meta_user_access_token"
    }

    /**
     * Constructs the official Meta OAuth authorization URL.
     * Returns null if Meta App ID is missing.
     */
    fun buildAuthorizationUri(): Uri? {
        if (!config.isConfigured) {
            return null
        }
        val scopeString = config.requiredScopes.joinToString(separator = ",")
        return Uri.parse(OAUTH_DIALOG_URL)
            .buildUpon()
            .appendQueryParameter("client_id", config.appId)
            .appendQueryParameter("redirect_uri", config.redirectUri)
            .appendQueryParameter("scope", scopeString)
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("state", generateSecureStateNonce())
            .build()
    }

    /**
     * Validates callback parameters from official redirect URI.
     */
    fun parseCallbackUri(uri: Uri): OAuthCallbackOutcome {
        val error = uri.getQueryParameter("error")
        val errorReason = uri.getQueryParameter("error_reason")
        val errorCode = uri.getQueryParameter("error_code")

        if (error != null) {
            return if (error == "access_denied" || errorReason == "user_denied") {
                OAuthCallbackOutcome.PermissionDenied
            } else {
                OAuthCallbackOutcome.Error("Meta OAuth error: $error (code: $errorCode)")
            }
        }

        val code = uri.getQueryParameter("code")
        return if (!code.isNullOrBlank()) {
            OAuthCallbackOutcome.CodeReceived(code)
        } else {
            OAuthCallbackOutcome.Error("No authorization code received in callback.")
        }
    }

    /**
     * Checks if Meta Developer App credentials have been configured.
     */
    fun checkConfigurationStatus(): MetaOAuthResult? {
        if (!config.isConfigured) {
            return MetaOAuthResult.ConfigurationRequired(
                "Meta App configuration is required. Please set META_APP_ID in your configuration and configure Facebook Login for Business in the Meta Developer Console."
            )
        }
        return null
    }

    /**
     * Saves page token reference safely to secure storage without exposing to logs or UI.
     */
    fun storePageTokenSafely(pageId: String, pageToken: String) {
        val key = "${TOKEN_KEY_PAGE_ACCESS}_$pageId"
        tokenStore.saveToken(key, pageToken)
    }

    fun hasPageToken(pageId: String): Boolean {
        val key = "${TOKEN_KEY_PAGE_ACCESS}_$pageId"
        return tokenStore.hasToken(key)
    }

    fun clearTokens() {
        tokenStore.clearAll()
    }

    private fun generateSecureStateNonce(): String {
        return "sa_" + (100000..999999).random()
    }
}

sealed interface OAuthCallbackOutcome {
    data class CodeReceived(val code: String) : OAuthCallbackOutcome
    data object PermissionDenied : OAuthCallbackOutcome
    data class Error(val message: String) : OAuthCallbackOutcome
}
