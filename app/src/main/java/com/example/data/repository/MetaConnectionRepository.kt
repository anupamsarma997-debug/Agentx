package com.example.data.repository

import android.content.Context
import com.example.data.local.security.SecureTokenStore
import com.example.data.model.meta.FacebookPageInfo
import com.example.data.model.meta.InstagramAccountInfo
import com.example.data.model.meta.InstagramAccountType
import com.example.data.model.meta.MetaConnectionState
import com.example.data.model.meta.MetaConnectionStatus
import com.example.data.remote.meta.MetaOAuthClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MetaConnectionRepository(
    private val oauthClient: MetaOAuthClient,
    private val tokenStore: SecureTokenStore,
    private val context: Context? = null
) {
    private val _connectionState = MutableStateFlow(MetaConnectionState())
    val connectionState: StateFlow<MetaConnectionState> = _connectionState.asStateFlow()

    private val prefs = context?.applicationContext?.getSharedPreferences(
        "social_agent_meta_connection_store",
        Context.MODE_PRIVATE
    )
    private val legacyPrefs = context?.applicationContext?.getSharedPreferences(
        "meta_connection_store",
        Context.MODE_PRIVATE
    )

    init {
        // 1. First restore previously connected Facebook Page and Instagram accounts from persistent storage
        restoreSavedConnection()

        // 2. Initial check: verify if Meta Developer credentials are provided ONLY if not already connected
        if (_connectionState.value.status != MetaConnectionStatus.CONNECTED && !hasSavedConnection()) {
            val configCheck = oauthClient.checkConfigurationStatus()
            if (configCheck != null) {
                _connectionState.value = MetaConnectionState(
                    status = MetaConnectionStatus.CONFIGURATION_REQUIRED,
                    errorMessage = "Meta Developer configuration required (App ID / Redirect URI)."
                )
            }
        }
    }

    fun hasSavedConnection(): Boolean {
        val pConnected = prefs?.getBoolean("is_connected", false) == true
        val pPageId = prefs?.getString("page_id", null)?.takeIf { it.isNotBlank() }
        if (pConnected && !pPageId.isNullOrBlank()) return true

        val legConnected = legacyPrefs?.getBoolean("is_connected", false) == true
        val legPageId = legacyPrefs?.getString("page_id", null)?.takeIf { it.isNotBlank() }
        if (legConnected && !legPageId.isNullOrBlank()) return true

        val tokenPageId = tokenStore.getToken("meta_connected_page_id")?.takeIf { it.isNotBlank() }
        val tokenIsConnected = tokenStore.getToken("meta_is_connected") == "true"
        if (tokenIsConnected && !tokenPageId.isNullOrBlank()) return true
        if (!tokenPageId.isNullOrBlank()) return true
        if (!pPageId.isNullOrBlank()) return true
        return false
    }

    fun restoreSavedConnection() {
        val savedPageId = prefs?.getString("page_id", null)?.takeIf { it.isNotBlank() }
            ?: legacyPrefs?.getString("page_id", null)?.takeIf { it.isNotBlank() }
            ?: tokenStore.getToken("meta_connected_page_id")?.takeIf { it.isNotBlank() }
        val savedPageName = prefs?.getString("page_name", null)?.takeIf { it.isNotBlank() }
            ?: legacyPrefs?.getString("page_name", null)?.takeIf { it.isNotBlank() }
            ?: tokenStore.getToken("meta_connected_page_name")?.takeIf { it.isNotBlank() }
        val isConnected = (prefs?.getBoolean("is_connected", false) == true) ||
            (legacyPrefs?.getBoolean("is_connected", false) == true) ||
            (tokenStore.getToken("meta_is_connected") == "true") ||
            (!savedPageId.isNullOrBlank())
        val isDemoSandbox = (prefs?.getBoolean("is_demo_sandbox", false) == true) ||
            (legacyPrefs?.getBoolean("is_demo_sandbox", false) == true) ||
            (tokenStore.getToken("meta_is_demo_sandbox") == "true")
        val savedPageToken = prefs?.getString("page_token", null)?.takeIf { it.isNotBlank() }
            ?: legacyPrefs?.getString("page_token", null)?.takeIf { it.isNotBlank() }
            ?: tokenStore.getToken("meta_connected_page_token")?.takeIf { it.isNotBlank() }
            ?: tokenStore.getToken("meta_page_access_token")?.takeIf { it.isNotBlank() }

        if (isConnected && !savedPageId.isNullOrBlank()) {
            val pageId = savedPageId
            val pageName = savedPageName ?: "Connected Facebook Page"
            val savedCategory = prefs?.getString("page_category", null)
                ?: legacyPrefs?.getString("page_category", null)
                ?: tokenStore.getToken("meta_connected_page_category")
                ?: "Facebook Page"

            val savedIgId = prefs?.getString("ig_id", null)?.takeIf { it.isNotBlank() }
                ?: legacyPrefs?.getString("ig_id", null)?.takeIf { it.isNotBlank() }
                ?: tokenStore.getToken("meta_connected_ig_id")?.takeIf { it.isNotBlank() }
            val savedIgUsername = prefs?.getString("ig_username", null)?.takeIf { it.isNotBlank() }
                ?: legacyPrefs?.getString("ig_username", null)?.takeIf { it.isNotBlank() }
                ?: tokenStore.getToken("meta_connected_ig_username")?.takeIf { it.isNotBlank() }
            val ig = if (!savedIgId.isNullOrBlank()) {
                InstagramAccountInfo(
                    instagramAccountId = savedIgId,
                    username = savedIgUsername ?: "instagram_business",
                    accountType = InstagramAccountType.PROFESSIONAL_BUSINESS,
                    isConnected = true
                )
            } else {
                null
            }

            if (!savedPageToken.isNullOrBlank()) {
                oauthClient.storePageTokenSafely(pageId, savedPageToken)
                tokenStore.saveToken("meta_connected_page_token", savedPageToken)
                tokenStore.saveToken("meta_page_access_token", savedPageToken)
                tokenStore.saveToken("meta_page_access_token_$pageId", savedPageToken)
            }

            _connectionState.value = MetaConnectionState(
                status = MetaConnectionStatus.CONNECTED,
                facebookPage = FacebookPageInfo(
                    pageId = pageId,
                    pageName = pageName,
                    category = savedCategory,
                    isConnected = true,
                    hasAccessTokenRef = !savedPageToken.isNullOrBlank() || isDemoSandbox
                ),
                instagramAccount = ig,
                errorMessage = null,
                lastConnectedTimestamp = prefs?.getLong("last_connected", System.currentTimeMillis()) ?: System.currentTimeMillis(),
                isDemoSandbox = isDemoSandbox
            )
        }
    }

    /**
     * Connects Facebook Page and optional Instagram Professional account.
     * Tokens and metadata are stored persistently and securely so they survive app restarts and background kills.
     */
    fun saveConnection(
        facebookPage: FacebookPageInfo,
        instagramAccount: InstagramAccountInfo?,
        pageToken: String? = null,
        isDemoSandbox: Boolean = false
    ) {
        val existingToken = tokenStore.getToken("meta_page_access_token_${facebookPage.pageId}")
            ?: tokenStore.getToken("meta_page_access_token")
            ?: tokenStore.getToken("meta_connected_page_token")
            ?: prefs?.getString("page_token", null)?.takeIf { it.isNotBlank() }
            ?: legacyPrefs?.getString("page_token", null)?.takeIf { it.isNotBlank() }

        val effectiveToken = if (!pageToken.isNullOrBlank()) pageToken else existingToken

        if (!effectiveToken.isNullOrBlank()) {
            oauthClient.storePageTokenSafely(facebookPage.pageId, effectiveToken)
            tokenStore.saveToken("meta_connected_page_token", effectiveToken)
            tokenStore.saveToken("meta_page_access_token", effectiveToken)
            tokenStore.saveToken("meta_page_access_token_${facebookPage.pageId}", effectiveToken)
        }

        // Persist to dedicated preferences with synchronous commit()
        val prefsEditor = prefs?.edit()
            ?.putBoolean("is_connected", true)
            ?.putBoolean("is_demo_sandbox", isDemoSandbox)
            ?.putString("page_id", facebookPage.pageId)
            ?.putString("page_name", facebookPage.pageName)
            ?.putString("page_category", facebookPage.category)
            ?.putString("ig_id", instagramAccount?.instagramAccountId ?: "")
            ?.putString("ig_username", instagramAccount?.username ?: "")
            ?.putLong("last_connected", System.currentTimeMillis())

        if (!effectiveToken.isNullOrBlank()) {
            prefsEditor?.putString("page_token", effectiveToken)
        }
        prefsEditor?.commit()

        val legEditor = legacyPrefs?.edit()
            ?.putBoolean("is_connected", true)
            ?.putBoolean("is_demo_sandbox", isDemoSandbox)
            ?.putString("page_id", facebookPage.pageId)
            ?.putString("page_name", facebookPage.pageName)
            ?.putString("page_category", facebookPage.category)
            ?.putLong("last_connected", System.currentTimeMillis())

        if (!effectiveToken.isNullOrBlank()) {
            legEditor?.putString("page_token", effectiveToken)
        }
        legEditor?.commit()

        // Also persist connection metadata in tokenStore
        tokenStore.saveToken("meta_is_connected", "true")
        tokenStore.saveToken("meta_is_demo_sandbox", isDemoSandbox.toString())
        tokenStore.saveToken("meta_connected_page_id", facebookPage.pageId)
        tokenStore.saveToken("meta_connected_page_name", facebookPage.pageName)
        tokenStore.saveToken("meta_connected_page_category", facebookPage.category)
        if (!effectiveToken.isNullOrBlank()) {
            tokenStore.saveToken("meta_connected_page_token", effectiveToken)
            tokenStore.saveToken("meta_page_access_token", effectiveToken)
            tokenStore.saveToken("meta_page_access_token_${facebookPage.pageId}", effectiveToken)
        }
        if (instagramAccount != null) {
            tokenStore.saveToken("meta_connected_ig_id", instagramAccount.instagramAccountId)
            tokenStore.saveToken("meta_connected_ig_username", instagramAccount.username)
        } else {
            tokenStore.deleteToken("meta_connected_ig_id")
            tokenStore.deleteToken("meta_connected_ig_username")
        }

        // Validate Instagram eligibility if present: do NOT disconnect Facebook if Instagram is personal
        val verifiedIg = if (instagramAccount != null && instagramAccount.accountType == InstagramAccountType.PERSONAL) {
            null
        } else {
            instagramAccount
        }

        val hasToken = !effectiveToken.isNullOrBlank() || oauthClient.hasPageToken(facebookPage.pageId) || isDemoSandbox

        _connectionState.value = MetaConnectionState(
            status = MetaConnectionStatus.CONNECTED,
            facebookPage = facebookPage.copy(hasAccessTokenRef = hasToken),
            instagramAccount = verifiedIg,
            errorMessage = if (instagramAccount != null && instagramAccount.accountType == InstagramAccountType.PERSONAL) {
                "Facebook Page connected! Instagram @${instagramAccount.username} is a Personal account (switch to Professional for IG publishing)."
            } else null,
            lastConnectedTimestamp = System.currentTimeMillis(),
            isDemoSandbox = isDemoSandbox
        )
    }

    fun disconnectFacebook() {
        prefs?.edit()?.clear()?.commit()
        legacyPrefs?.edit()?.clear()?.commit()
        tokenStore.deleteToken("meta_is_connected")
        tokenStore.deleteToken("meta_is_demo_sandbox")
        tokenStore.deleteToken("meta_connected_page_id")
        tokenStore.deleteToken("meta_connected_page_name")
        tokenStore.deleteToken("meta_connected_page_category")
        tokenStore.deleteToken("meta_connected_ig_id")
        tokenStore.deleteToken("meta_connected_ig_username")
        tokenStore.deleteToken("meta_connected_page_token")
        oauthClient.clearTokens()
        _connectionState.value = MetaConnectionState(
            status = MetaConnectionStatus.DISCONNECTED,
            facebookPage = null,
            instagramAccount = null,
            errorMessage = null,
            lastConnectedTimestamp = null,
            isDemoSandbox = false
        )
    }

    fun disconnectInstagram() {
        prefs?.edit()
            ?.remove("ig_id")
            ?.remove("ig_username")
            ?.commit()
        legacyPrefs?.edit()
            ?.remove("ig_id")
            ?.remove("ig_username")
            ?.commit()
        tokenStore.deleteToken("meta_connected_ig_id")
        tokenStore.deleteToken("meta_connected_ig_username")
        _connectionState.value = _connectionState.value.copy(
            instagramAccount = null
        )
    }

    fun setAvailablePages(pages: List<FacebookPageInfo>) {
        _connectionState.value = _connectionState.value.copy(
            availablePages = pages
        )
    }

    fun setAuthenticating(authenticating: Boolean) {
        _connectionState.value = _connectionState.value.copy(
            isAuthenticating = authenticating
        )
    }

    fun setOpeningMeta() {
        _connectionState.value = _connectionState.value.copy(
            status = MetaConnectionStatus.OPENING_META,
            isAuthenticating = true,
            errorMessage = null
        )
    }

    fun setWaitingForAuthorization() {
        _connectionState.value = _connectionState.value.copy(
            status = MetaConnectionStatus.WAITING_FOR_AUTHORIZATION,
            isAuthenticating = true,
            errorMessage = null
        )
    }

    fun setConnecting() {
        _connectionState.value = _connectionState.value.copy(
            status = MetaConnectionStatus.CONNECTING,
            isAuthenticating = true,
            errorMessage = null
        )
    }

    fun setPermissionDenied() {
        _connectionState.value = _connectionState.value.copy(
            status = MetaConnectionStatus.PERMISSION_REQUIRED,
            errorMessage = "Permission was not granted during Meta authorization. Required permissions: pages_manage_posts, instagram_content_publish."
        )
    }

    fun setExpired() {
        _connectionState.value = _connectionState.value.copy(
            status = MetaConnectionStatus.EXPIRED,
            errorMessage = "Meta session or token has expired. Please re-authorize the connection."
        )
    }

    fun setError(message: String) {
        val hasPage = _connectionState.value.facebookPage != null || hasSavedConnection()
        _connectionState.value = _connectionState.value.copy(
            status = if (hasPage) MetaConnectionStatus.CONNECTED else MetaConnectionStatus.ERROR,
            errorMessage = message
        )
    }

    fun disconnect() {
        tokenStore.deleteToken("meta_connected_page_id")
        tokenStore.deleteToken("meta_connected_page_name")
        tokenStore.deleteToken("meta_connected_page_category")
        tokenStore.deleteToken("meta_connected_ig_id")
        tokenStore.deleteToken("meta_connected_ig_username")
        oauthClient.clearTokens()
        _connectionState.value = MetaConnectionState(
            status = MetaConnectionStatus.DISCONNECTED,
            facebookPage = null,
            instagramAccount = null,
            errorMessage = null,
            lastConnectedTimestamp = null
        )
    }

    fun getConnectedFacebookPage(): FacebookPageInfo? = _connectionState.value.facebookPage

    fun getConnectedInstagramAccount(): InstagramAccountInfo? = _connectionState.value.instagramAccount

    fun isFullyConnected(): Boolean = _connectionState.value.isFullyConnected
}
