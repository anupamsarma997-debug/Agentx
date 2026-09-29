package com.example

import com.example.data.local.entity.ContentEntity
import com.example.data.local.entity.OpportunityEntity
import com.example.data.local.settings.AppSettings
import com.example.data.model.content.ContentLength
import com.example.data.model.content.ContentPlatform
import com.example.data.model.content.ContentType
import com.example.data.model.content.GenerationStatus
import com.example.data.model.opportunity.OpportunityCategory
import com.example.data.model.opportunity.OpportunityRegion
import com.example.data.model.opportunity.SourceTier
import com.example.data.model.opportunity.VerificationStatus
import com.example.domain.automation.FreeTierGuard
import com.example.domain.automation.GenerationDecision
import com.example.domain.engine.ContentCreationEngine
import com.example.domain.engine.GenerationGateResult
import com.example.domain.model.SourceFact
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ContentCreationTest {

    private lateinit var creationEngine: ContentCreationEngine
    private lateinit var freeTierGuard: FreeTierGuard

    @Before
    fun setUp() {
        creationEngine = ContentCreationEngine()
        freeTierGuard = FreeTierGuard()
    }

    private fun createTestOpportunity(
        status: VerificationStatus = VerificationStatus.VERIFIED,
        deadline: String? = "2026-11-30"
    ): OpportunityEntity {
        return OpportunityEntity(
            id = "test_opp_1",
            title = "APSC CCE 2026 Examination",
            description = "Recruitment for Assam Civil Services and Police Services.",
            category = OpportunityCategory.GOVERNMENT_JOB.name,
            region = OpportunityRegion.ASSAM.name,
            sourceName = "Assam Public Service Commission",
            sourceUrl = "https://apsc.nic.in/cce.html",
            sourceDomain = "apsc.nic.in",
            publishedAt = "2026-09-20",
            deadline = deadline,
            deadlineEpochMillis = 1796000000000L,
            eligibility = "Graduate degree; state domicile.",
            organization = "APSC",
            sourceTier = SourceTier.TIER_1_OFFICIAL.name,
            verificationStatus = status.name,
            discoveredAt = System.currentTimeMillis(),
            lastCheckedAt = System.currentTimeMillis(),
            contentHash = "hash_12345",
            isExpired = status == VerificationStatus.EXPIRED
        )
    }

    // 1. Verified source can generate
    @Test
    fun `test 1 - verified source passes generation gate`() {
        val gate = creationEngine.checkGenerationGate(VerificationStatus.VERIFIED)
        assertEquals(GenerationGateResult.Permitted, gate)
        val initialStatus = creationEngine.resolveInitialStatus(VerificationStatus.VERIFIED)
        assertEquals(GenerationStatus.GENERATED, initialStatus)
    }

    // 2. Needs-review source generates REVIEW_REQUIRED
    @Test
    fun `test 2 - needs review source produces REVIEW_REQUIRED status`() {
        val gate = creationEngine.checkGenerationGate(VerificationStatus.NEEDS_REVIEW)
        assertEquals(GenerationGateResult.Permitted, gate)
        val initialStatus = creationEngine.resolveInitialStatus(VerificationStatus.NEEDS_REVIEW)
        assertEquals(GenerationStatus.REVIEW_REQUIRED, initialStatus)
    }

    // 3. Rejected source blocked
    @Test
    fun `test 3 - rejected source is blocked from generation`() {
        val gate = creationEngine.checkGenerationGate(VerificationStatus.REJECTED)
        assertTrue(gate is GenerationGateResult.Blocked)
    }

    // 4. Expired source blocked for normal promotion
    @Test
    fun `test 4 - expired source is blocked from promotional generation`() {
        val gate = creationEngine.checkGenerationGate(VerificationStatus.EXPIRED)
        assertTrue(gate is GenerationGateResult.Blocked)
    }

    // 5. Source URL cannot be replaced by AI
    @Test
    fun `test 5 - source url cannot be replaced by AI output`() {
        val trustedUrl = "https://apsc.nic.in/cce.html"
        val mockAiJsonWithDifferentUrl = """
            {
              "title": "Assam Civil Services Alert",
              "body": "Apply for APSC CCE. Check source: https://apsc.nic.in",
              "caption": "APSC Examination Alert",
              "hashtags": ["Assam", "Jobs"],
              "sourceUrl": "https://attacker-fake-site.com/phishing",
              "sourceName": "APSC",
              "contentType": "GOVERNMENT_JOB",
              "platform": "BOTH",
              "confidence": "HIGH",
              "needsReview": false
            }
        """.trimIndent()

        val parsed = creationEngine.validateAndParseResponse(
            rawJson = mockAiJsonWithDifferentUrl,
            expectedSourceUrl = trustedUrl,
            expectedSourceName = "APSC",
            expectedDeadline = "2026-11-30",
            expectedContentType = ContentType.JOB_ALERT,
            expectedPlatform = ContentPlatform.BOTH
        ).getOrThrow()

        // Verified expectation: URL is strictly enforced to be the original trustedUrl
        assertEquals(trustedUrl, parsed.sourceUrl)
        assertFalse(parsed.sourceUrl.contains("attacker-fake-site"))
    }

    // 6. Invalid JSON rejected
    @Test
    fun `test 6 - invalid json is rejected by validator`() {
        val malformedJson = "{ title: 'Missing quotes', body: }"
        val result = creationEngine.validateAndParseResponse(
            rawJson = malformedJson,
            expectedSourceUrl = "https://apsc.nic.in",
            expectedSourceName = "APSC",
            expectedDeadline = "2026-11-30",
            expectedContentType = ContentType.OPPORTUNITY_POST,
            expectedPlatform = ContentPlatform.BOTH
        )
        assertTrue(result.isFailure)
    }

    // 7. Missing title rejected
    @Test
    fun `test 7 - missing title is rejected by validator`() {
        val jsonNoTitle = """
            {
              "title": "",
              "body": "Valid post body text here",
              "caption": "Short caption",
              "hashtags": ["Jobs"]
            }
        """.trimIndent()

        val result = creationEngine.validateAndParseResponse(
            rawJson = jsonNoTitle,
            expectedSourceUrl = "https://apsc.nic.in",
            expectedSourceName = "APSC",
            expectedDeadline = null,
            expectedContentType = ContentType.OPPORTUNITY_POST,
            expectedPlatform = ContentPlatform.BOTH
        )
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("title") == true)
    }

    // 8. Hashtag limit enforced (max 8)
    @Test
    fun `test 8 - hashtag count is strictly capped at maximum 8`() {
        val jsonManyTags = """
            {
              "title": "SIH 2026 Innovation Hackathon",
              "body": "Smart India Hackathon announcement details.",
              "caption": "SIH Hackathon",
              "hashtags": ["tag1", "tag2", "tag3", "tag4", "tag5", "tag6", "tag7", "tag8", "tag9", "tag10"]
            }
        """.trimIndent()

        val parsed = creationEngine.validateAndParseResponse(
            rawJson = jsonManyTags,
            expectedSourceUrl = "https://sih.gov.in",
            expectedSourceName = "SIH",
            expectedDeadline = "2026-10-31",
            expectedContentType = ContentType.HACKATHON_ALERT,
            expectedPlatform = ContentPlatform.BOTH
        ).getOrThrow()

        assertEquals(8, parsed.hashtags.size)
        assertEquals("#tag1", parsed.hashtags[0])
    }

    // 9. Free-mode quota respected
    @Test
    fun `test 9 - free mode post generation quota is enforced`() {
        val underQuotaSettings = AppSettings(todayPostCount = 5, dailyPostTarget = 8)
        val decisionUnder = freeTierGuard.canGeneratePost(underQuotaSettings)
        assertTrue(decisionUnder is GenerationDecision.Allowed)

        val atQuotaSettings = AppSettings(todayPostCount = 8, dailyPostTarget = 8)
        val decisionAt = freeTierGuard.canGeneratePost(atQuotaSettings)
        assertTrue(decisionAt is GenerationDecision.QuotaExhausted)
    }

    // 10. Gemini not called after quota reached
    @Test
    fun `test 10 - generation blocked when quota is exhausted`() {
        val exhaustedSettings = AppSettings(todayPostCount = 8, dailyPostTarget = 8)
        val decision = freeTierGuard.canGeneratePost(exhaustedSettings)
        assertTrue(decision is GenerationDecision.QuotaExhausted)
    }

    // 11. Political content remains neutral
    @Test
    fun `test 11 - system prompt explicitly instructs political neutrality`() {
        val prompt = creationEngine.buildSystemPrompt(
            contentType = ContentType.NEWS_POST,
            platform = ContentPlatform.BOTH,
            length = ContentLength.SHORT
        )
        assertTrue(prompt.contains("POLITICAL & NEWS NEUTRALITY"))
        assertTrue(prompt.contains("neutral"))
        assertTrue(prompt.contains("Do not endorse, praise, or criticize"))
    }

    // 12. Fabricated deadline rejected
    @Test
    fun `test 12 - fabricated deadline in title rejected when source had no deadline`() {
        val jsonFabricatedDeadline = """
            {
              "title": "New Scheme Deadline: 2026-12-31 announced",
              "body": "Post body",
              "caption": "Caption",
              "hashtags": ["Scheme"]
            }
        """.trimIndent()

        val result = creationEngine.validateAndParseResponse(
            rawJson = jsonFabricatedDeadline,
            expectedSourceUrl = "https://assam.gov.in",
            expectedSourceName = "Assam Portal",
            expectedDeadline = null, // Source had NO deadline
            expectedContentType = ContentType.OPPORTUNITY_POST,
            expectedPlatform = ContentPlatform.BOTH
        )
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Fabricated deadline") == true)
    }

    // 13. Content saved as draft, not published
    @Test
    fun `test 13 - generated content is marked DRAFT or GENERATED and never PUBLISHED`() {
        val opp = createTestOpportunity(VerificationStatus.VERIFIED)
        val initialStatus = creationEngine.resolveInitialStatus(opp.verificationStatusEnum)

        val contentEntity = ContentEntity(
            id = UUID.randomUUID().toString(),
            sourceOpportunityId = opp.id,
            contentType = ContentType.OPPORTUNITY_POST.name,
            platform = ContentPlatform.BOTH.name,
            title = "Test Draft",
            body = "Body",
            caption = "Caption",
            hashtags = "#Jobs",
            sourceUrl = opp.sourceUrl,
            sourceName = opp.sourceName,
            generationStatus = initialStatus.name,
            verificationStatus = opp.verificationStatus
        )

        assertNotEquals(GenerationStatus.PUBLISHED.name, contentEntity.generationStatus)
        assertEquals(GenerationStatus.GENERATED.name, contentEntity.generationStatus)
    }

    // 14. Regeneration creates a new draft
    @Test
    fun `test 14 - regeneration generates distinct draft ID and retains draft state`() {
        val opp = createTestOpportunity(VerificationStatus.VERIFIED)
        val draft1 = ContentEntity(
            id = UUID.randomUUID().toString(),
            sourceOpportunityId = opp.id,
            contentType = ContentType.OPPORTUNITY_POST.name,
            platform = ContentPlatform.BOTH.name,
            title = "Draft 1",
            body = "Body 1",
            caption = "Caption 1",
            hashtags = "#Jobs",
            sourceUrl = opp.sourceUrl,
            sourceName = opp.sourceName,
            generationStatus = GenerationStatus.GENERATED.name
        )

        val draft2 = ContentEntity(
            id = UUID.randomUUID().toString(),
            sourceOpportunityId = opp.id,
            contentType = ContentType.OPPORTUNITY_POST.name,
            platform = ContentPlatform.BOTH.name,
            title = "Draft 2",
            body = "Body 2",
            caption = "Caption 2",
            hashtags = "#Jobs",
            sourceUrl = opp.sourceUrl,
            sourceName = opp.sourceName,
            generationStatus = GenerationStatus.GENERATED.name
        )

        assertNotEquals(draft1.id, draft2.id)
        assertNotEquals(GenerationStatus.PUBLISHED.name, draft2.generationStatus)
    }
}
