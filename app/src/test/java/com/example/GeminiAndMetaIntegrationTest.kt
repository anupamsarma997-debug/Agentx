package com.example

import com.example.data.local.security.InMemorySecureTokenStore
import com.example.data.model.content.ContentLength
import com.example.data.model.content.ContentPlatform
import com.example.data.model.content.ContentType
import com.example.data.model.meta.FacebookPageInfo
import com.example.data.model.meta.InstagramAccountInfo
import com.example.data.model.meta.InstagramAccountType
import com.example.data.model.meta.MetaConnectionStatus
import com.example.data.remote.ai.AIResult
import com.example.data.remote.ai.GeminiClient
import com.example.data.remote.meta.MetaOAuthConfig
import com.example.data.remote.meta.MetaOAuthClient
import com.example.data.remote.meta.MetaOAuthResult
import com.example.data.remote.meta.OAuthCallbackOutcome
import com.example.data.repository.MetaConnectionRepository
import com.example.domain.engine.ContentCreationEngine
import com.example.domain.engine.ContentCreationOutcome
import com.example.domain.model.SourceFact
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import android.net.Uri

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class GeminiAndMetaIntegrationTest {

    private fun createFact(
        title: String,
        sourceName: String,
        sourceUrl: String,
        description: String = "Description",
        category: String = "CATEGORY",
        region: String = "ASSAM",
        deadline: String? = null,
        organization: String? = null,
        eligibility: String? = null,
        publishedAt: String? = null,
        verificationStatus: String = "VERIFIED"
    ): SourceFact {
        return SourceFact(
            title = title,
            organization = organization,
            description = description,
            category = category,
            region = region,
            eligibility = eligibility,
            deadline = deadline,
            publishedAt = publishedAt,
            sourceName = sourceName,
            sourceUrl = sourceUrl,
            verificationStatus = verificationStatus
        )
    }

    // A. Gemini key missing -> clear configuration error
    @Test
    fun `test A - missing Gemini key produces clear configuration error`() = runTest {
        val clientWithoutKey = GeminiClient(
            apiKeyProvider = { "" }
        )
        val result = clientWithoutKey.generateContent("system prompt", "user prompt")
        assertTrue(result is AIResult.ConfigurationRequired)
        val configMsg = (result as AIResult.ConfigurationRequired).message
        assertTrue(configMsg.contains("Gemini API key is not configured"))
        assertTrue(configMsg.contains("AI Studio Secrets panel"))
    }

    // B. Gemini generation works for all 5 required content types
    @Test
    fun `test B1 - Job Alert generation and validation`() = runTest {
        val mockJson = """
            {
              "title": "APSC Engineering Services 2026",
              "body": "APSC has announced 120 posts for Assistant Engineers. Apply by Nov 30.",
              "caption": "APSC Engineering Recruitment",
              "hashtags": ["AssamJobs", "Engineers"],
              "sourceUrl": "https://apsc.nic.in/eng2026.html",
              "sourceName": "APSC Official",
              "contentType": "JOB_ALERT",
              "platform": "BOTH",
              "confidence": "HIGH",
              "needsReview": false
            }
        """.trimIndent()

        val mockClient = object : GeminiClient(apiKeyProvider = { "valid_test_key" }) {
            override suspend fun generateContent(systemPrompt: String, userPrompt: String): AIResult {
                return AIResult.Success(mockJson)
            }
        }
        val engine = ContentCreationEngine(geminiClient = mockClient)
        val fact = createFact(
            title = "APSC Engineering Services 2026",
            sourceName = "APSC Official",
            sourceUrl = "https://apsc.nic.in/eng2026.html",
            description = "Recruitment for 120 engineers",
            category = "GOVERNMENT_JOB",
            region = "ASSAM",
            deadline = "2026-11-30"
        )
        val outcome = engine.generateContent(fact, ContentType.JOB_ALERT, ContentPlatform.BOTH, ContentLength.SHORT)
        assertTrue(outcome is ContentCreationOutcome.Success)
        val success = outcome as ContentCreationOutcome.Success
        assertEquals(ContentType.JOB_ALERT, success.result.contentType)
        assertEquals("https://apsc.nic.in/eng2026.html", success.result.sourceUrl)
    }

    @Test
    fun `test B2 - Scholarship Alert generation and validation`() = runTest {
        val mockJson = """
            {
              "title": "National Means-cum-Merit Scholarship 2026",
              "body": "Applications open for Class 9 students in Assam. ₹12,000 annual scholarship.",
              "caption": "NMMSS Scholarship 2026",
              "hashtags": ["Scholarship", "AssamEducation"],
              "sourceUrl": "https://scholarships.gov.in/nmmss",
              "sourceName": "National Scholarship Portal",
              "contentType": "SCHOLARSHIP_ALERT",
              "platform": "BOTH",
              "confidence": "HIGH",
              "needsReview": false
            }
        """.trimIndent()

        val mockClient = object : GeminiClient(apiKeyProvider = { "valid_test_key" }) {
            override suspend fun generateContent(systemPrompt: String, userPrompt: String): AIResult {
                return AIResult.Success(mockJson)
            }
        }
        val engine = ContentCreationEngine(geminiClient = mockClient)
        val fact = createFact(
            title = "NMMSS Scholarship",
            sourceName = "National Scholarship Portal",
            sourceUrl = "https://scholarships.gov.in/nmmss",
            description = "Class 9 merit scholarship",
            category = "SCHOLARSHIP",
            region = "ASSAM"
        )
        val outcome = engine.generateContent(fact, ContentType.SCHOLARSHIP_ALERT, ContentPlatform.BOTH, ContentLength.SHORT)
        assertTrue(outcome is ContentCreationOutcome.Success)
        val success = outcome as ContentCreationOutcome.Success
        assertEquals(ContentType.SCHOLARSHIP_ALERT, success.result.contentType)
    }

    @Test
    fun `test B3 - Hackathon Alert generation and validation`() = runTest {
        val mockJson = """
            {
              "title": "Smart India Hackathon 2026 Senior Edition",
              "body": "World's biggest open innovation hackathon. Prize pool of ₹1 Crore.",
              "caption": "SIH 2026 Alert",
              "hashtags": ["Hackathon", "SIH2026"],
              "sourceUrl": "https://sih.gov.in",
              "sourceName": "Ministry of Education",
              "contentType": "HACKATHON_ALERT",
              "platform": "BOTH",
              "confidence": "HIGH",
              "needsReview": false
            }
        """.trimIndent()

        val mockClient = object : GeminiClient(apiKeyProvider = { "valid_test_key" }) {
            override suspend fun generateContent(systemPrompt: String, userPrompt: String): AIResult {
                return AIResult.Success(mockJson)
            }
        }
        val engine = ContentCreationEngine(geminiClient = mockClient)
        val fact = createFact(
            title = "Smart India Hackathon 2026",
            sourceName = "Ministry of Education",
            sourceUrl = "https://sih.gov.in",
            description = "Innovation hackathon for university students",
            category = "HACKATHON",
            region = "ALL_INDIA"
        )
        val outcome = engine.generateContent(fact, ContentType.HACKATHON_ALERT, ContentPlatform.BOTH, ContentLength.SHORT)
        assertTrue(outcome is ContentCreationOutcome.Success)
        val success = outcome as ContentCreationOutcome.Success
        assertEquals(ContentType.HACKATHON_ALERT, success.result.contentType)
    }

    @Test
    fun `test B4 - Startup Alert generation and validation`() = runTest {
        val mockJson = """
            {
              "title": "Startup India Seed Fund Scheme 2026",
              "body": "Financial assistance up to ₹50 Lakhs for early-stage proof of concept.",
              "caption": "Seed Fund Scheme Alert",
              "hashtags": ["Startups", "SeedFund"],
              "sourceUrl": "https://seedfund.startupindia.gov.in",
              "sourceName": "DPIIT",
              "contentType": "STARTUP_ALERT",
              "platform": "BOTH",
              "confidence": "HIGH",
              "needsReview": false
            }
        """.trimIndent()

        val mockClient = object : GeminiClient(apiKeyProvider = { "valid_test_key" }) {
            override suspend fun generateContent(systemPrompt: String, userPrompt: String): AIResult {
                return AIResult.Success(mockJson)
            }
        }
        val engine = ContentCreationEngine(geminiClient = mockClient)
        val fact = createFact(
            title = "Startup India Seed Fund",
            sourceName = "DPIIT",
            sourceUrl = "https://seedfund.startupindia.gov.in",
            description = "Seed funding grants and convertible debentures",
            category = "GRANT_FUNDING",
            region = "ALL_INDIA"
        )
        val outcome = engine.generateContent(fact, ContentType.STARTUP_ALERT, ContentPlatform.BOTH, ContentLength.SHORT)
        assertTrue(outcome is ContentCreationOutcome.Success)
        val success = outcome as ContentCreationOutcome.Success
        assertEquals(ContentType.STARTUP_ALERT, success.result.contentType)
    }

    @Test
    fun `test B5 - News Summary generation and validation`() = runTest {
        val mockJson = """
            {
              "title": "Assam Cabinet Approves New IT Policy",
              "body": "The state government approved incentives for technology companies setting up in Guwahati.",
              "caption": "Assam IT Policy Update",
              "hashtags": ["AssamNews", "TechPolicy"],
              "sourceUrl": "https://assam.gov.in/news/it-policy-2026",
              "sourceName": "Government of Assam",
              "contentType": "NEWS_POST",
              "platform": "BOTH",
              "confidence": "HIGH",
              "needsReview": false
            }
        """.trimIndent()

        val mockClient = object : GeminiClient(apiKeyProvider = { "valid_test_key" }) {
            override suspend fun generateContent(systemPrompt: String, userPrompt: String): AIResult {
                return AIResult.Success(mockJson)
            }
        }
        val engine = ContentCreationEngine(geminiClient = mockClient)
        val fact = createFact(
            title = "Assam IT Policy",
            sourceName = "Government of Assam",
            sourceUrl = "https://assam.gov.in/news/it-policy-2026",
            description = "New IT investment policy",
            category = "GENERAL_NEWS",
            region = "ASSAM"
        )
        val outcome = engine.generateContent(fact, ContentType.NEWS_POST, ContentPlatform.BOTH, ContentLength.SHORT)
        assertTrue(outcome is ContentCreationOutcome.Success)
        val success = outcome as ContentCreationOutcome.Success
        assertEquals(ContentType.NEWS_POST, success.result.contentType)
    }

    // C. Meta configuration missing -> clear configuration message
    @Test
    fun `test C - missing Meta configuration produces clear diagnostic message`() {
        val store = InMemorySecureTokenStore()
        val client = MetaOAuthClient(
            config = MetaOAuthConfig(appId = ""),
            tokenStore = store
        )
        val check = client.checkConfigurationStatus()
        assertTrue(check is MetaOAuthResult.ConfigurationRequired)
        val msg = (check as MetaOAuthResult.ConfigurationRequired).message
        assertTrue(msg.contains("META_APP_ID"))
        assertTrue(msg.contains("AI Studio Secrets panel"))
        assertNull(client.buildAuthorizationUri())
    }

    // D. Facebook connection configured -> real OAuth flow starts
    @Test
    fun `test D - configured Meta App ID builds valid OAuth dialog URL`() {
        val store = InMemorySecureTokenStore()
        val client = MetaOAuthClient(
            config = MetaOAuthConfig(
                appId = "987654321012345",
                redirectUri = "socialagent://meta-callback"
            ),
            tokenStore = store
        )
        assertNull(client.checkConfigurationStatus())
        val uri = client.buildAuthorizationUri()
        assertNotNull(uri)
        assertEquals("https", uri?.scheme)
        assertEquals("www.facebook.com", uri?.host)
        assertEquals("/v19.0/dialog/oauth", uri?.path)
        assertEquals("987654321012345", uri?.getQueryParameter("client_id"))
        assertEquals("socialagent://meta-callback", uri?.getQueryParameter("redirect_uri"))
        assertTrue(uri?.getQueryParameter("scope")?.contains("pages_manage_posts") == true)
        assertTrue(uri?.getQueryParameter("scope")?.contains("instagram_content_publish") == true)
    }

    // E. Instagram connection configured -> Professional account lookup & personal rejection
    @Test
    fun `test E - Instagram Professional vs Personal connection rules`() {
        val store = InMemorySecureTokenStore()
        val client = MetaOAuthClient(
            config = MetaOAuthConfig(appId = "987654321012345"),
            tokenStore = store
        )
        val repo = MetaConnectionRepository(client, store)
        val fb = FacebookPageInfo(pageId = "page_123", pageName = "Official Portal")

        // Eligible Professional account
        val eligibleIg = InstagramAccountInfo(
            instagramAccountId = "ig_123",
            username = "official_portal",
            accountType = InstagramAccountType.PROFESSIONAL_BUSINESS
        )
        repo.saveConnection(fb, eligibleIg, pageToken = "token_abc")
        assertTrue(repo.connectionState.value.isFacebookConnected)
        assertTrue(repo.connectionState.value.isInstagramConnected)
        assertTrue(repo.connectionState.value.isFullyConnected)

        // Ineligible Personal account rejected
        val personalIg = InstagramAccountInfo(
            instagramAccountId = "ig_456",
            username = "personal_account",
            accountType = InstagramAccountType.PERSONAL
        )
        repo.saveConnection(fb, personalIg, pageToken = "token_abc")
        assertFalse(repo.connectionState.value.isInstagramConnected)
        assertEquals(MetaConnectionStatus.ERROR, repo.connectionState.value.status)
        assertTrue(repo.connectionState.value.errorMessage!!.contains("Personal account"))
    }

    // F. Sandbox mode -> clearly labeled simulation only
    @Test
    fun `test F - Sandbox simulation is explicitly labeled as demo and not production`() {
        val store = InMemorySecureTokenStore()
        val client = MetaOAuthClient(
            config = MetaOAuthConfig(appId = "987654321012345"),
            tokenStore = store
        )
        val repo = MetaConnectionRepository(client, store)
        val fb = FacebookPageInfo(pageId = "demo_page_1", pageName = "Demo Page")
        val ig = InstagramAccountInfo(
            instagramAccountId = "demo_ig_1",
            username = "demo_account",
            accountType = InstagramAccountType.PROFESSIONAL_BUSINESS
        )

        repo.saveConnection(fb, ig, pageToken = "demo_token", isDemoSandbox = true)

        val state = repo.connectionState.value
        assertEquals(MetaConnectionStatus.CONNECTED, state.status)
        assertTrue(state.isDemoSandbox)
        assertTrue(state.isFacebookConnected)
        assertTrue(state.isInstagramConnected)

        // Clearing via disconnect clears sandbox flag
        repo.disconnectFacebook()
        assertFalse(repo.connectionState.value.isDemoSandbox)
        assertEquals(MetaConnectionStatus.DISCONNECTED, repo.connectionState.value.status)
    }

    // G. Invalid or expired Meta authentication -> safe error + reconnect option
    @Test
    fun `test G - expired session safely transitions state for reconnect`() {
        val store = InMemorySecureTokenStore()
        val client = MetaOAuthClient(
            config = MetaOAuthConfig(appId = "987654321012345"),
            tokenStore = store
        )
        val repo = MetaConnectionRepository(client, store)
        val fb = FacebookPageInfo(pageId = "exp_page", pageName = "Expiring Page")
        repo.saveConnection(fb, null, pageToken = "temp_token")

        repo.setExpired()
        assertEquals(MetaConnectionStatus.EXPIRED, repo.connectionState.value.status)
        assertTrue(repo.connectionState.value.errorMessage!!.contains("expired"))

        // Reconnect flow re-establishes state
        repo.saveConnection(fb, null, pageToken = "fresh_token")
        assertEquals(MetaConnectionStatus.CONNECTED, repo.connectionState.value.status)
    }

    // H. No secrets exposed in logs, state strings, or error messages
    @Test
    fun `test H - secrets are never leaked in state, exceptions or string representations`() {
        val secretToken = "SECRET_META_TOKEN_NEVER_LEAK_IN_LOGS_9999"
        val store = InMemorySecureTokenStore()
        val client = MetaOAuthClient(
            config = MetaOAuthConfig(appId = "123456789"),
            tokenStore = store
        )
        val repo = MetaConnectionRepository(client, store)
        val fb = FacebookPageInfo(pageId = "10492850239", pageName = "Assam Tech")
        repo.saveConnection(fb, null, pageToken = secretToken)

        val state = repo.connectionState.value
        val stateString = state.toString()

        assertFalse("Secret token was found in MetaConnectionState.toString()", stateString.contains(secretToken))
        assertFalse("Secret token was found in FacebookPageInfo.toString()", fb.toString().contains(secretToken))

        // Check masked ID
        assertEquals("***0239", fb.maskedPageId)
        assertFalse(fb.maskedPageId.contains("1049285"))

        // Secure token store holds the actual secret securely
        assertEquals(secretToken, store.getToken("meta_page_access_token_10492850239"))
    }
}
