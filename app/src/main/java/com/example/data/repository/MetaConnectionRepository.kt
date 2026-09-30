package com.example.data.repository

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
    private val tokenStore: SecureTokenStore
) {
    private val _connectionState = MutableStateFlow(MetaConnectionState())
    val connectionState: StateFlow<MetaConnectionState> = _connectionState.asStateFlow()

    init {
        // Initial check: verify if Meta Developer credentials are provided
        val configCheck = oauthClient.checkConfigurationStatus()
        if (configCheck != null) {
            _connectionState.value = MetaConnectionState(
                status = MetaConnectionStatus.CONFIGURATION_REQUIRED,
                errorMessage = "Meta Developer configuration required (App ID / Redirect URI)."
            )
        }
    }

    /**
     * Connects Facebook Page and optional Instagram Professional account.
     * Tokens are securely stored via SecureTokenStore and never retained in UI state.
     */
    fun saveConnection(
        facebookPage: FacebookPageInfo,
        instagramAccount: InstagramAccountInfo?,
        pageToken: String? = null,
        isDemoSandbox: Boolean = false
    ) {
        if (pageToken != null && pageToken.isNotBlank()) {
            oauthClient.storePageTokenSafely(facebookPage.pageId, pageToken)
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
