package com.example.data.model.meta

enum class MetaConnectionStatus {
    DISCONNECTED,
    OPENING_META,
    WAITING_FOR_AUTHORIZATION,
    CONNECTING,
    CONNECTED,
    PERMISSION_REQUIRED,
    CONFIGURATION_REQUIRED,
    EXPIRED,
    ERROR
}

enum class InstagramAccountType(val displayName: String) {
    PROFESSIONAL_BUSINESS("Professional (Business)"),
    PROFESSIONAL_CREATOR("Professional (Creator)"),
    PERSONAL("Personal (Unsupported for Publishing)"),
    UNKNOWN("Unknown")
}

data class FacebookPageInfo(
    val pageId: String,
    val pageName: String,
    val category: String = "Facebook Page",
    val isConnected: Boolean = true,
    val hasAccessTokenRef: Boolean = false
) {
    /**
     * Safely masks the page ID to prevent full exposure in logs or UI
     */
    val maskedPageId: String
        get() {
            return if (pageId.length > 4) {
                "***" + pageId.takeLast(4)
            } else {
                "***"
            }
        }
}

data class InstagramAccountInfo(
    val instagramAccountId: String,
    val username: String,
    val accountType: InstagramAccountType = InstagramAccountType.PROFESSIONAL_BUSINESS,
    val isConnected: Boolean = true
) {
    val isEligibleForPublishing: Boolean
        get() = accountType == InstagramAccountType.PROFESSIONAL_BUSINESS ||
                accountType == InstagramAccountType.PROFESSIONAL_CREATOR

    val maskedAccountId: String
        get() = if (instagramAccountId.length > 4) "***" + instagramAccountId.takeLast(4) else "***"
}

data class MetaConnectionState(
    val status: MetaConnectionStatus = MetaConnectionStatus.DISCONNECTED,
    val facebookPage: FacebookPageInfo? = null,
    val instagramAccount: InstagramAccountInfo? = null,
    val errorMessage: String? = null,
    val lastConnectedTimestamp: Long? = null,
    val isDemoSandbox: Boolean = false,
    val availablePages: List<FacebookPageInfo> = emptyList(),
    val isAuthenticating: Boolean = false
) {
    val isFacebookConnected: Boolean
        get() = (status == MetaConnectionStatus.CONNECTED || (facebookPage != null && status != MetaConnectionStatus.DISCONNECTED && status != MetaConnectionStatus.EXPIRED)) &&
                facebookPage?.isConnected == true

    val isInstagramConnected: Boolean
        get() = (status == MetaConnectionStatus.CONNECTED || (instagramAccount != null && status != MetaConnectionStatus.DISCONNECTED && status != MetaConnectionStatus.EXPIRED)) &&
                instagramAccount?.isConnected == true &&
                instagramAccount.isEligibleForPublishing

    val isFullyConnected: Boolean
        get() = isFacebookConnected && isInstagramConnected
}
