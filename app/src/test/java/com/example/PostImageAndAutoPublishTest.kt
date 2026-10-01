package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.security.InMemorySecureTokenStore
import com.example.data.model.meta.FacebookPageInfo
import com.example.data.model.meta.InstagramAccountInfo
import com.example.data.model.meta.InstagramAccountType
import com.example.data.model.meta.MetaConnectionStatus
import com.example.data.remote.meta.MetaOAuthConfig
import com.example.data.remote.meta.MetaOAuthClient
import com.example.data.repository.MetaConnectionRepository
import com.example.domain.generator.PostImageGenerator
import com.example.domain.publishing.FoundationDisabledMetaPublisher
import com.example.domain.publishing.PublishResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PostImageAndAutoPublishTest {

    private lateinit var context: Context
    private lateinit var tokenStore: InMemorySecureTokenStore
    private lateinit var oauthClient: MetaOAuthClient

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        tokenStore = InMemorySecureTokenStore()
        oauthClient = MetaOAuthClient(
            config = MetaOAuthConfig(appId = "1234567890", redirectUri = "socialagent://meta-callback"),
            tokenStore = tokenStore
        )
    }

    @Test
    fun `test 1 - connection persists across process recreation and restart`() {
        // Step 1: Initialize first repo instance and save connection
        val repo1 = MetaConnectionRepository(oauthClient, tokenStore, context)
        val fbPage = FacebookPageInfo(
            pageId = "99887766",
            pageName = "My Tech Page",
            category = "Education"
        )
        val igAccount = InstagramAccountInfo(
            instagramAccountId = "ig_9988",
            username = "mytech_page",
            accountType = InstagramAccountType.PROFESSIONAL_BUSINESS
        )
        repo1.saveConnection(
            facebookPage = fbPage,
            instagramAccount = igAccount,
            pageToken = "EAAB_test_page_token_12345",
            isDemoSandbox = false
        )

        assertEquals(MetaConnectionStatus.CONNECTED, repo1.connectionState.value.status)
        assertTrue(repo1.connectionState.value.isFacebookConnected)
        assertEquals("My Tech Page", repo1.connectionState.value.facebookPage?.pageName)

        // Step 2: Simulate app kill / background swipe: Create brand new repo instance with same context
        val repo2 = MetaConnectionRepository(oauthClient, tokenStore, context)

        // Verify connection is automatically restored without disconnecting
        assertEquals(MetaConnectionStatus.CONNECTED, repo2.connectionState.value.status)
        assertTrue(repo2.connectionState.value.isFacebookConnected)
        assertEquals("My Tech Page", repo2.connectionState.value.facebookPage?.pageName)
        assertEquals("99887766", repo2.connectionState.value.facebookPage?.pageId)
        assertEquals("mytech_page", repo2.connectionState.value.instagramAccount?.username)
        assertTrue(repo2.hasSavedConnection())
    }

    @Test
    fun `test 2 - post image generator produces valid file`() {
        val generator = PostImageGenerator(context)
        val imagePath = generator.generatePostBanner(
            contentId = "test_post_001",
            title = "Assam Youth Fellowship Program 2026",
            category = "Scholarship",
            organization = "Department of Science and Technology",
            deadline = "30 November 2026",
            sourceUrl = "https://fellowship.assam.gov.in",
            region = "Assam & North East"
        )

        assertNotNull(imagePath)
        val file = File(imagePath!!)
        assertTrue("Banner image file should exist", file.exists())
        assertTrue("Banner image file size should be > 1000 bytes", file.length() > 1000)
    }

    @Test
    fun `test 3 - publisher accepts imageUrl parameter`() = runTest {
        val publisher = FoundationDisabledMetaPublisher()
        val result = publisher.publishFacebookPost(
            pageId = "99887766",
            content = "Exciting Opportunity for Youth!",
            linkUrl = "https://fellowship.assam.gov.in",
            imageUrl = "/path/to/test_image.jpg"
        )

        assertTrue(result is PublishResult.Disabled)
    }
}
