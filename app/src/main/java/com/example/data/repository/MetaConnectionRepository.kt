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
        if (prefs != null) {
            val isConnected = prefs.getBoolean("is_connected", false)
            val pageId = prefs.getString("page_id", null)
            if (isConnected && !pageId.isNullOrBlank()) return true
        }
        if (legacyPrefs != null) {
            val isConnected = legacyPrefs.getBoolean("is_connected", false)
            val pageId = legacyPrefs.getString("page_id", null)
            if (isConnected && !pageId.isNullOrBlank()) return true
        }
        val tokenPageId = tokenStore.getToken("meta_connected_page_id")
        val tokenIsConnected = tokenStore.getToken("meta_is_connected") == "true"
        return tokenIsConnected && !tokenPageId.isNullOrBlank()
    }

    fun restoreSavedConnection() {
        val savedPageId = prefs?.getString("page_id", null)
            ?: legacyPrefs?.getString("page_id", null)
            ?: tokenStore.getToken("meta_connected_page_id")
        val savedPageName = prefs?.getString("page_name", null)
            ?: legacyPrefs?.getString("page_name", null)
            ?: tokenStore.getToken("meta_connected_page_name")
        val isConnected = prefs?.getBoolean("is_connected", false)
            ?: legacyPrefs?.getBoolean("is_connected", false)
            ?: (tokenStore.getToken("meta_is_connected") == "true")
        val isDemoSandbox = prefs?.getBoolean("is_demo_sandbox", false)
            ?: legacyPrefs?.getBoolean("is_demo_sandbox", false)
            ?: (tokenStore.getToken("meta_is_demo_sandbox") == "true")
        val savedPageToken = prefs?.getString("page_token", null)
            ?: legacyPrefs?.getString("page_token", null)
            ?: tokenStore.getToken("meta_connected_page_token")
            ?: tokenStore.getToken("meta_page_access_token")

        if (isConnected || (!savedPageId.isNullOrBlank() && !savedPageName.isNullOrBlank())) {
            val pageId = savedPageId ?: "connected_page"
            val pageName = savedPageName ?: "Connected Facebook Page"
            val savedCategory = prefs?.getString("page_category", null)
                ?: legacyPrefs?.getString("page_category", null)
                ?: tokenStore.getToken("meta_connected_page_category")
                ?: "Facebook Page"

            val savedIgId = prefs?.getString("ig_id", null)
                ?: legacyPrefs?.getString("ig_id", null)
                ?: tokenStore.getToken("meta_connected_ig_id")
            val savedIgUsername = prefs?.getString("ig_username", null)
                ?: legacyPrefs?.getString("ig_username", null)
                ?: tokenStore.getToken("meta_connected_ig_username")
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
        if (pageToken != null && pageToken.isNotBlank()) {
            oauthClient.storePageTokenSafely(facebookPage.pageId, pageToken)
            tokenStore.saveToken("meta_connected_page_token", pageToken)
            tokenStore.saveToken("meta_page_access_token", pageToken)
            tokenStore.saveToken("meta_page_access_token_${facebookPage.pageId}", pageToken)
        }

        // Persist to dedicated preferences with synchronous commit()
        prefs?.edit()
            ?.putBoolean("is_connected", true)
            ?.putBoolean("is_demo_sandbox", isDemoSandbox)
            ?.putString("page_id", facebookPage.pageId)
            ?.putString("page_name", facebookPage.pageName)
            ?.putString("page_category", facebookPage.category)
            ?.putString("page_token", pageToken ?: "")
            ?.putString("ig_id", instagramAccount?.instagramAccountId ?: "")
            ?.putString("ig_username", instagramAccount?.username ?: "")
            ?.putLong("last_connected", System.currentTimeMillis())
            ?.commit()

        legacyPrefs?.edit()
            ?.putBoolean("is_connected", true)
            ?.putBoolean("is_demo_sandbox", isDemoSandbox)
            ?.putString("page_id", facebookPage.pageId)
            ?.putString("page_name", facebookPage.pageName)
            ?.putString("page_category", facebookPage.category)
            ?.putString("page_token", pageToken ?: "")
            ?.putLong("last_connected", System.currentTimeMillis())
            ?.commit()

        // Also persist connection metadata in tokenStore
        tokenStore.saveToken("meta_is_connected", "true")
        tokenStore.saveToken("meta_is_demo_sandbox", isDemoSandbox.toString())
        tokenStore.saveToken("meta_connected_page_id", facebookPage.pageId)
        tokenStore.saveToken("meta_connected_page_name", facebookPage.pageName)
        tokenStore.saveToken("meta_connected_page_category", facebookPage.category)
        if (pageToken != null && pageToken.isNotBlank()) {
            tokenStore.saveToken("meta_connected_page_token", pageToken)
            tokenStore.saveToken("meta_page_access_token", pageToken)
            tokenStore.saveToken("meta_page_access_token_${facebookPage.pageId}", pageToken)
        }
        if (instagramAccount != null) {
            tokenStore.saveToken("meta_connected_ig_id", instagramAccount.instagramAccountId)
            tokenStore.saveToken("meta_connected_ig_username", instagramAccount.username)
        } else {
            tokenStore.deleteToken("meta_connected_ig_id")
            tokenStore.deleteToken("meta_connected_ig_username")
        }

        // Validate Instagram eligibility if present
        if (instagramAccount != null && instagramAccount.accountType == InstagramAccountType.PERSONAL) {
            _connectionState.value = MetaConnectionState(
                status = MetaConnectionStatus.ERROR,
                facebookPage = facebookPage.copy(hasAccessTokenRef = pageToken != null),
                instagramAccount = instagramAccount,
                errorMessage = "Instagram Personal account (@${instagramAccount.username}) is not eligible for publishing. Please switch to a Professional (Business or Creator) account in Instagram settings.",
                lastConnectedTimestamp = System.currentTimeMillis(),
                isDemoSandbox = isDemoSandbox
            )
            return
        }

        val hasToken = oauthClient.hasPageToken(facebookPage.pageId) || (pageToken != null)

        _connectionState.value = MetaConnectionState(
            status = MetaConnectionStatus.CONNECTED,
            facebookPage = facebookPage.copy(hasAccessTokenRef = hasToken),
            instagramAccount = instagramAccount,
            errorMessage = null,
            lastConnectedTimestamp = System.currentTimeMillis(),
            isDemoSandbox = isDemoSandbox
        )
    }

    fun disconnectFacebook() {
        prefs?.edit()?.clear()?.commit()
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
        val currentFb = _connectionState.value.facebookPage
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
        _connectionState.value = _connectionState.value.copy(
            status = MetaConnectionStatus.ERROR,
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
