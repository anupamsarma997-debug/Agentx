package com.example

import com.example.data.local.security.InMemorySecureTokenStore
import com.example.data.model.meta.FacebookPageInfo
import com.example.data.model.meta.InstagramAccountInfo
import com.example.data.model.meta.InstagramAccountType
import com.example.data.model.meta.MetaConnectionStatus
import com.example.data.remote.meta.MetaOAuthConfig
import com.example.data.remote.meta.MetaOAuthClient
import com.example.data.remote.meta.MetaOAuthResult
import com.example.data.repository.MetaConnectionRepository
import com.example.domain.publishing.FoundationDisabledMetaPublisher
import com.example.domain.publishing.PublishResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MetaConnectionTest {

    private lateinit var tokenStore: InMemorySecureTokenStore
    private lateinit var oauthClient: MetaOAuthClient
    private lateinit var repository: MetaConnectionRepository

    @Before
    fun setUp() {
        tokenStore = InMemorySecureTokenStore()
        oauthClient = MetaOAuthClient(
            config = MetaOAuthConfig(appId = "1234567890", redirectUri = "socialagent://meta-callback"),
            tokenStore = tokenStore
        )
        repository = MetaConnectionRepository(oauthClient, tokenStore)
    }

    // 1. Default disconnected state
    @Test
    fun `test 1 - default disconnected state`() {
        val repo = MetaConnectionRepository(
            oauthClient = MetaOAuthClient(
                config = MetaOAuthConfig(appId = "configured_id"),
                tokenStore = tokenStore
            ),
            tokenStore = tokenStore
        )
        val state = repo.connectionState.value
        assertEquals(MetaConnectionStatus.DISCONNECTED, state.status)
        assertNull(state.facebookPage)
        assertNull(state.instagramAccount)
        assertFalse(state.isFacebookConnected)
        assertFalse(state.isInstagramConnected)
        assertFalse(state.isFullyConnected)
    }

    // 2. Facebook connected state
    @Test
    fun `test 2 - facebook connected state`() {
        val fb = FacebookPageInfo(pageId = "page_9876", pageName = "My Tech Community")
        repository.saveConnection(facebookPage = fb, instagramAccount = null, pageToken = "secret_page_token")

        val state = repository.connectionState.value
        assertEquals(MetaConnectionStatus.CONNECTED, state.status)
        assertTrue(state.isFacebookConnected)
        assertFalse(state.isInstagramConnected)
        assertEquals("My Tech Community", state.facebookPage?.pageName)
        assertEquals("***9876", state.facebookPage?.maskedPageId)
        assertTrue(state.facebookPage?.hasAccessTokenRef == true)
    }

    // 3. Instagram connected state
    @Test
    fun `test 3 - instagram connected state`() {
        val fb = FacebookPageInfo(pageId = "page_1111", pageName = "Social Portal")
        val ig = InstagramAccountInfo(
            instagramAccountId = "ig_2222",
            username = "socialportal_official",
            accountType = InstagramAccountType.PROFESSIONAL_BUSINESS
        )
        repository.saveConnection(facebookPage = fb, instagramAccount = ig, pageToken = "tok_333")

        val state = repository.connectionState.value
        assertEquals(MetaConnectionStatus.CONNECTED, state.status)
        assertTrue(state.isFacebookConnected)
        assertTrue(state.isInstagramConnected)
        assertTrue(state.isFullyConnected)
        assertEquals("socialportal_official", state.instagramAccount?.username)
        assertTrue(state.instagramAccount?.isEligibleForPublishing == true)
    }

    // 4. Instagram Personal account rejection/unsupported state
    @Test
    fun `test 4 - instagram personal account rejection`() {
        val fb = FacebookPageInfo(pageId = "page_1234", pageName = "Personal Brand")
        val igPersonal = InstagramAccountInfo(
            instagramAccountId = "ig_5678",
            username = "john_doe_personal",
            accountType = InstagramAccountType.PERSONAL
        )
        repository.saveConnection(facebookPage = fb, instagramAccount = igPersonal, pageToken = "token_abc")

        val state = repository.connectionState.value
        assertEquals(MetaConnectionStatus.ERROR, state.status)
        assertFalse(state.isInstagramConnected)
        assertFalse(state.isFullyConnected)
        assertNotNull(state.errorMessage)
        assertTrue(state.errorMessage!!.contains("Personal account"))
        assertFalse(igPersonal.isEligibleForPublishing)
    }

    // 5. Permission denied state
    @Test
    fun `test 5 - permission denied state`() {
        repository.setPermissionDenied()

        val state = repository.connectionState.value
        assertEquals(MetaConnectionStatus.PERMISSION_REQUIRED, state.status)
        assertNotNull(state.errorMessage)
        assertTrue(state.errorMessage!!.contains("Permission was not granted"))
    }

    // 6. Expired connection
    @Test
    fun `test 6 - expired connection`() {
        repository.setExpired()

        val state = repository.connectionState.value
        assertEquals(MetaConnectionStatus.EXPIRED, state.status)
        assertNotNull(state.errorMessage)
        assertTrue(state.errorMessage!!.contains("expired"))
    }

    // 7. Disconnect clears connection metadata
    @Test
    fun `test 7 - disconnect clears connection metadata`() {
        val fb = FacebookPageInfo(pageId = "page_9999", pageName = "To Disconnect")
        repository.saveConnection(facebookPage = fb, instagramAccount = null, pageToken = "tok_page")

        assertTrue(repository.isFullyConnected() || repository.connectionState.value.isFacebookConnected)
        assertTrue(tokenStore.hasToken("meta_page_access_token_page_9999"))

        repository.disconnect()

        val state = repository.connectionState.value
        assertEquals(MetaConnectionStatus.DISCONNECTED, state.status)
        assertNull(state.facebookPage)
        assertNull(state.instagramAccount)
        assertFalse(tokenStore.hasToken("meta_page_access_token_page_9999"))
    }

    // 8. Token store abstraction never exposes tokens through UI state
    @Test
    fun `test 8 - token store abstraction never exposes tokens through UI state`() {
        val rawToken = "EAABwzL_REAL_SECRET_TOKEN_DO_NOT_EXPOSE_12345"
        val fb = FacebookPageInfo(pageId = "page_sec_1", pageName = "Secure Page")
        repository.saveConnection(facebookPage = fb, instagramAccount = null, pageToken = rawToken)

        val state = repository.connectionState.value
        // Token is held in private tokenStore, NOT in FacebookPageInfo
        assertEquals("Secure Page", state.facebookPage?.pageName)
        assertEquals("***ec_1", state.facebookPage?.maskedPageId)
        assertTrue(state.facebookPage?.hasAccessTokenRef == true)

        // Verify token does not appear in state toString representation
        assertFalse(state.toString().contains(rawToken))
        assertFalse(state.facebookPage.toString().contains(rawToken))
        // Verify token store actually holds it securely
        assertEquals(rawToken, tokenStore.getToken("meta_page_access_token_page_sec_1"))
    }

    // 9. Missing Meta configuration produces safe configuration-required state
    @Test
    fun `test 9 - missing meta configuration produces safe configuration-required state`() {
        val unconfiguredClient = MetaOAuthClient(
            config = MetaOAuthConfig(appId = ""), // empty App ID
            tokenStore = tokenStore
        )
        val configStatus = unconfiguredClient.checkConfigurationStatus()
        assertTrue(configStatus is MetaOAuthResult.ConfigurationRequired)
        assertNull(unconfiguredClient.buildAuthorizationUri())

        val repo = MetaConnectionRepository(unconfiguredClient, tokenStore)
        assertEquals(MetaConnectionStatus.CONFIGURATION_REQUIRED, repo.connectionState.value.status)
    }

    // Foundation publisher safety check: publishing strictly returns Disabled result
    @Test
    fun `publishing remains strictly disabled in phase 3`() = runTest {
        val publisher = FoundationDisabledMetaPublisher()
        val fbResult = publisher.publishFacebookPost("123", "Test Post")
        val igResult = publisher.publishInstagramPhoto("456", "https://example.com/img.jpg", "Caption")
        val reelResult = publisher.publishInstagramReel("789", "https://example.com/vid.mp4", "Reel")

        assertTrue(fbResult is PublishResult.Disabled)
        assertTrue(igResult is PublishResult.Disabled)
        assertTrue(reelResult is PublishResult.Disabled)
    }
}
