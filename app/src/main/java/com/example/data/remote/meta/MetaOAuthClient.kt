package com.example.data.remote.meta

import android.net.Uri
import com.example.BuildConfig
import com.example.data.local.security.SecureTokenStore
import com.example.data.model.meta.FacebookPageInfo
import com.example.data.model.meta.InstagramAccountInfo
import com.example.data.model.meta.InstagramAccountType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

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
    val appId: String = resolveAppId(),
    val redirectUri: String = resolveRedirectUri(),
    val requiredScopes: List<String> = listOf(
        "pages_show_list",
        "pages_read_engagement",
        "pages_manage_posts",
        "instagram_basic",
        "instagram_content_publish"
    )
) {
    companion object {
        fun resolveAppId(): String {
            val fromBuildConfig = try {
                BuildConfig.META_APP_ID
            } catch (_: Throwable) {
                null
            }
            if (!fromBuildConfig.isNullOrBlank() &&
                !fromBuildConfig.startsWith("YOUR_") &&
                !fromBuildConfig.startsWith("DEFAULT_")
            ) {
                return fromBuildConfig.trim()
            }

            val fromEnv = try {
                System.getenv("META_APP_ID")
            } catch (_: Throwable) {
                null
            }
            if (!fromEnv.isNullOrBlank() &&
                !fromEnv.startsWith("YOUR_") &&
                !fromEnv.startsWith("DEFAULT_")
            ) {
                return fromEnv.trim()
            }

            return ""
        }

        fun resolveRedirectUri(): String {
            val fromBuildConfig = try {
                BuildConfig.META_REDIRECT_URI
            } catch (_: Throwable) {
                null
            }
            if (!fromBuildConfig.isNullOrBlank()) {
                return fromBuildConfig.trim()
            }
            return "socialagent://meta-callback"
        }
    }

    val isConfigured: Boolean
        get() = appId.isNotBlank() &&
                !appId.startsWith("YOUR_") &&
                !appId.startsWith("DEFAULT_") &&
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
    val config: MetaOAuthConfig = MetaOAuthConfig(),
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
     * Returns detailed diagnostic explanation if unconfigured.
     */
    fun checkConfigurationStatus(): MetaOAuthResult? {
        if (!config.isConfigured) {
            val missingDetail = if (config.appId.isBlank() || config.appId.startsWith("YOUR_") || config.appId.startsWith("DEFAULT_")) {
                "META_APP_ID is not configured in the AI Studio Secrets panel."
            } else {
                "META_REDIRECT_URI is not set."
            }
            return MetaOAuthResult.ConfigurationRequired(
                "Meta Developer Configuration Required: $missingDetail\n\nTo connect live accounts:\n1. Register an app on developers.facebook.com\n2. Add 'Facebook Login for Business'\n3. Set redirect URI to ${config.redirectUri}\n4. Add META_APP_ID to AI Studio Secrets panel."
            )
        }
        return null
    }

    /**
     * Fetches pages managed by user from Meta Graph API using secure token.
     */
    suspend fun fetchPagesFromGraphApi(accessToken: String): Result<List<FacebookPageInfo>> = withContext(Dispatchers.IO) {
        var conn: HttpURLConnection? = null
        try {
            val url = URL("https://graph.facebook.com/v19.0/me/accounts?fields=id,name,access_token&access_token=$accessToken")
            conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 10000
                readTimeout = 15000
                requestMethod = "GET"
            }
            val responseCode = conn.responseCode
            val stream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
            val respText = stream?.let { BufferedReader(InputStreamReader(it)).use { r -> r.readText() } } ?: ""

            if (responseCode !in 200..299) {
                val safeErr = try {
                    JSONObject(respText).optJSONObject("error")?.optString("message") ?: "API Error ($responseCode)"
                } catch (_: Exception) {
                    "API Error ($responseCode)"
                }
                return@withContext Result.failure(Exception("Failed to retrieve Facebook Pages: $safeErr"))
            }

            val data = JSONObject(respText).optJSONArray("data") ?: JSONArray()
            val pages = mutableListOf<FacebookPageInfo>()
            for (i in 0 until data.length()) {
                val item = data.getJSONObject(i)
                val id = item.optString("id")
                val name = item.optString("name")
                val pageToken = item.optString("access_token")
                if (id.isNotBlank() && name.isNotBlank()) {
                    if (pageToken.isNotBlank()) {
                        storePageTokenSafely(id, pageToken)
                    }
                    pages.add(FacebookPageInfo(pageId = id, pageName = name, isConnected = true, hasAccessTokenRef = pageToken.isNotBlank()))
                }
            }
            Result.success(pages)
        } catch (e: Exception) {
            Result.failure(Exception("Network error connecting to Meta Graph API: ${e.localizedMessage ?: "Unknown error"}"))
        } finally {
            conn?.disconnect()
        }
    }

    /**
     * Inspects linked Instagram Professional account for a given Facebook Page.
     */
    suspend fun fetchInstagramForPage(pageId: String, pageToken: String): Result<InstagramAccountInfo?> = withContext(Dispatchers.IO) {
        var conn: HttpURLConnection? = null
        try {
            val url = URL("https://graph.facebook.com/v19.0/$pageId?fields=instagram_business_account{id,username,name}&access_token=$pageToken")
            conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 10000
                readTimeout = 15000
                requestMethod = "GET"
            }
            val responseCode = conn.responseCode
            val stream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
            val respText = stream?.let { BufferedReader(InputStreamReader(it)).use { r -> r.readText() } } ?: ""

            if (responseCode !in 200..299) {
                val safeErr = try {
                    JSONObject(respText).optJSONObject("error")?.optString("message") ?: "API Error ($responseCode)"
                } catch (_: Exception) {
                    "API Error ($responseCode)"
                }
                return@withContext Result.failure(Exception("Failed to check Instagram connection: $safeErr"))
            }

            val root = JSONObject(respText)
            val igObj = root.optJSONObject("instagram_business_account")
            if (igObj != null) {
                val igId = igObj.optString("id")
                val username = igObj.optString("username")
                val info = InstagramAccountInfo(
                    instagramAccountId = igId,
                    username = username,
                    accountType = InstagramAccountType.PROFESSIONAL_BUSINESS,
                    isConnected = true
                )
                Result.success(info)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error checking Instagram account: ${e.localizedMessage ?: "Unknown error"}"))
        } finally {
            conn?.disconnect()
        }
    }

    /**
     * Saves page token reference safely to secure storage without exposing to logs or UI.
     */
    fun storePageTokenSafely(pageId: String, pageToken: String) {
        val key = "${TOKEN_KEY_PAGE_ACCESS}_$pageId"
        tokenStore.saveToken(key, pageToken)
    }

    fun getPageToken(pageId: String): String? {
        val key = "${TOKEN_KEY_PAGE_ACCESS}_$pageId"
        return tokenStore.getToken(key)
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
