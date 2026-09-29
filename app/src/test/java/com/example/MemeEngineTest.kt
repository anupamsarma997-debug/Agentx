package com.example

import com.example.data.local.entity.MemeDraftEntity
import com.example.data.local.settings.AppSettings
import com.example.data.model.meme.MemeDraft
import com.example.data.model.meme.MemeFormat
import com.example.data.model.meme.MemeGenerationStatus
import com.example.data.model.meme.MemeSafetyStatus
import com.example.data.model.meme.MemeTopic
import com.example.data.model.opportunity.OpportunityCategory
import com.example.data.model.opportunity.OpportunityRegion
import com.example.data.model.opportunity.VerificationStatus
import com.example.domain.automation.FreeTierGuard
import com.example.domain.automation.GenerationDecision
import com.example.domain.engine.MemeDuplicateDetector
import com.example.domain.engine.MemeEngine
import com.example.domain.engine.MemeGenerationOutcome
import com.example.domain.engine.MemeSafetyEngine
import com.example.domain.publishing.FoundationDisabledMetaPublisher
import com.example.domain.publishing.PublishResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemeEngineTest {

    private lateinit var safetyEngine: MemeSafetyEngine
    private lateinit var memeEngine: MemeEngine
    private lateinit var freeTierGuard: FreeTierGuard

    @Before
    fun setUp() {
        safetyEngine = MemeSafetyEngine()
        memeEngine = MemeEngine(safetyEngine = safetyEngine)
        freeTierGuard = FreeTierGuard()
    }

    private fun createSampleTopic(
        status: VerificationStatus = VerificationStatus.VERIFIED,
        url: String = "https://assam.gov.in/civic"
    ): MemeTopic {
        return MemeTopic(
            topic = "Guwahati Monsoon Traffic",
            context = "Heavy evening rain causing slow traffic at GS Road and tea break stalls.",
            category = OpportunityCategory.EDUCATION.name,
            region = OpportunityRegion.ASSAM.name,
            sourceName = "Assam Civic Updates",
            sourceUrl = url,
            verificationStatus = status.name,
            sourceOpportunityId = "opp_123"
        )
    }

    // 1. Safe meme passes
    @Test
    fun `test 1 - safe wholesome meme passes safety engine`() = runBlocking {
        val topic = createSampleTopic()
        val rawJson = """
            {
              "topic": "Guwahati Monsoon Traffic",
              "format": "ASSAM_RELATABLE",
              "setupText": "Planning a 15-minute drive on GS Road at 6 PM",
              "punchlineText": "Finished two hot cups of lal cha before reaching the first traffic signal",
              "caption": "Every Guwahati driver knows the evening rain tea routine! #GuwahatiDiaries #AssamRains",
              "hashtags": ["Guwahati", "Assam", "ChaiBreak"],
              "needsReview": false
            }
        """.trimIndent()

        val outcome = memeEngine.parseAndValidateMeme(rawJson, topic, MemeFormat.ASSAM_RELATABLE)
        assertTrue(outcome is MemeGenerationOutcome.Success)
        val draft = (outcome as MemeGenerationOutcome.Success).draft
        assertEquals(MemeSafetyStatus.SAFE, draft.safetyStatus)
        assertEquals(MemeGenerationStatus.DRAFT, draft.generationStatus)
        assertEquals("Planning a 15-minute drive on GS Road at 6 PM", draft.setupText)
    }

    // 2. Unsafe meme blocked
    @Test
    fun `test 2 - unsafe meme containing prohibited extreme threat terms is blocked`() = runBlocking {
        val topic = createSampleTopic()
        val rawJson = """
            {
              "topic": "Extreme threat meme",
              "format": "TEXT_MEME",
              "setupText": "When someone annoys you in traffic",
              "punchlineText": "Send a bomb threat to the intersection",
              "caption": "Dangerous threat joke",
              "hashtags": ["BadJoke"]
            }
        """.trimIndent()

        val outcome = memeEngine.parseAndValidateMeme(rawJson, topic, MemeFormat.TEXT_MEME)
        assertTrue(outcome is MemeGenerationOutcome.BlockedBySafety)
        val blocked = outcome as MemeGenerationOutcome.BlockedBySafety
        assertTrue(blocked.reasons.any { it.contains("bomb threat") })
    }

    // 3. Uncertain meme goes to review
    @Test
    fun `test 3 - uncertain meme containing controversy or investigation terms goes to review`() = runBlocking {
        val topic = createSampleTopic()
        val rawJson = """
            {
              "topic": "Municipal Department Raid",
              "format": "TOP_BOTTOM",
              "setupText": "The department promised new drainage before monsoon",
              "punchlineText": "Now police raid and corruption investigation ongoing at the office",
              "caption": "Ongoing investigation into the municipal office drainage scam.",
              "hashtags": ["Guwahati", "Investigation"]
            }
        """.trimIndent()

        val outcome = memeEngine.parseAndValidateMeme(rawJson, topic, MemeFormat.TOP_BOTTOM)
        assertTrue(outcome is MemeGenerationOutcome.Success)
        val draft = (outcome as MemeGenerationOutcome.Success).draft
        assertEquals(MemeSafetyStatus.NEEDS_REVIEW, draft.safetyStatus)
        assertEquals(MemeGenerationStatus.REVIEW_REQUIRED, draft.generationStatus)
    }

    // 4. Political topic requires review
    @Test
    fun `test 4 - political and civic affairs context defaults to needs review`() = runBlocking {
        val topic = createSampleTopic()
        val rawJson = """
            {
              "topic": "State Assembly Election Debates",
              "format": "TEXT_MEME",
              "setupText": "When election season begins in the state assembly",
              "punchlineText": "Every tea stall transforms into a political cabinet meeting with free opinions",
              "caption": "Assam election season turns every neighbourhood chai stall into a parliament!",
              "hashtags": ["AssamElections", "TeaStallPolitics"]
            }
        """.trimIndent()

        val outcome = memeEngine.parseAndValidateMeme(rawJson, topic, MemeFormat.TEXT_MEME)
        assertTrue(outcome is MemeGenerationOutcome.Success)
        val draft = (outcome as MemeGenerationOutcome.Success).draft
        assertEquals(MemeSafetyStatus.NEEDS_REVIEW, draft.safetyStatus)
        assertEquals(MemeGenerationStatus.REVIEW_REQUIRED, draft.generationStatus)
    }

    // 5. Hate speech / harassment blocked
    @Test
    fun `test 5 - discriminatory hate content targeting ethnic communities is strictly blocked`() = runBlocking {
        val topic = createSampleTopic()
        val eval = safetyEngine.evaluate(
            topic = "Offensive joke",
            setupText = "Targeting local residents",
            punchlineText = "We despise all tribals in the region",
            caption = "Discriminatory text"
        )
        assertTrue(eval.isBlocked)
        assertEquals(MemeSafetyStatus.BLOCKED, eval.status)
    }

    // 6. Source URL cannot be replaced by AI
    @Test
    fun `test 6 - source URL is strictly preserved from the original verified topic`() = runBlocking {
        val trustedUrl = "https://assam.gov.in/official-portal"
        val topic = createSampleTopic(url = trustedUrl)

        val rawJsonWithMaliciousUrl = """
            {
              "topic": "Guwahati Monsoon Traffic",
              "format": "TEXT_MEME",
              "setupText": "Normal rain vs Guwahati rain",
              "punchlineText": "Boat rentals now open on RGB Road",
              "caption": "Check our fake malicious link: https://fake-phishing-url.com",
              "hashtags": ["Guwahati"]
            }
        """.trimIndent()

        val outcome = memeEngine.parseAndValidateMeme(rawJsonWithMaliciousUrl, topic, MemeFormat.TEXT_MEME)
        assertTrue(outcome is MemeGenerationOutcome.Success)
        val draft = (outcome as MemeGenerationOutcome.Success).draft
        assertEquals(trustedUrl, draft.sourceUrl)
        assertFalse(draft.sourceUrl.contains("fake-phishing-url"))
    }

    // 7. Text length validation & safe truncation
    @Test
    fun `test 7 - setup and punchline text are safely truncated within character limits`() = runBlocking {
        val topic = createSampleTopic()
        val longSetup = "A".repeat(150) // Limit is 120
        val longPunchline = "B".repeat(200) // Limit is 160

        val rawJson = """
            {
              "topic": "Character Limit Test",
              "format": "TEXT_MEME",
              "setupText": "$longSetup",
              "punchlineText": "$longPunchline",
              "caption": "Testing character limits",
              "hashtags": ["Test"]
            }
        """.trimIndent()

        val outcome = memeEngine.parseAndValidateMeme(rawJson, topic, MemeFormat.TEXT_MEME)
        assertTrue(outcome is MemeGenerationOutcome.Success)
        val draft = (outcome as MemeGenerationOutcome.Success).draft
        assertTrue(draft.setupText.length <= 120)
        assertTrue(draft.punchlineText.length <= 160)
    }

    // 8. Hashtag limit enforced
    @Test
    fun `test 8 - hashtag count is strictly capped at maximum 8`() = runBlocking {
        val topic = createSampleTopic()
        val rawJson = """
            {
              "topic": "Hashtags Overflow Test",
              "format": "TEXT_MEME",
              "setupText": "Valid setup text under limit",
              "punchlineText": "Valid punchline text under limit",
              "caption": "Short caption",
              "hashtags": ["t1", "t2", "t3", "t4", "t5", "t6", "t7", "t8", "t9", "t10", "t11"]
            }
        """.trimIndent()

        val outcome = memeEngine.parseAndValidateMeme(rawJson, topic, MemeFormat.TEXT_MEME)
        assertTrue(outcome is MemeGenerationOutcome.Success)
        val draft = (outcome as MemeGenerationOutcome.Success).draft
        assertEquals(8, draft.hashtags.size)
        assertEquals("#t1", draft.hashtags[0])
    }

    // 9. Duplicate detection
    @Test
    fun `test 9 - duplicate detection produces identical hash for identical topic and punchline`() {
        val hash1 = MemeDuplicateDetector.computeHash(
            topic = "Guwahati Monsoon Traffic",
            setupText = "GS Road at 6 PM",
            punchlineText = "Lal cha break"
        )

        // Same content with uppercase and extra spaces
        val hash2 = MemeDuplicateDetector.computeHash(
            topic = "   GUWAHATI   MONSOON   TRAFFIC   ",
            setupText = "GS   Road   at   6   PM",
            punchlineText = "LAL   CHA   BREAK   "
        )

        val hash3 = MemeDuplicateDetector.computeHash(
            topic = "Guwahati Monsoon Traffic",
            setupText = "GS Road at 6 PM",
            punchlineText = "Different punchline entirely"
        )

        assertEquals(hash1, hash2)
        assertNotEquals(hash1, hash3)
    }

    // 10. Free-mode quota respected
    @Test
    fun `test 10 - free mode quota blocks meme generation when daily post target is reached`() {
        val underQuota = AppSettings(todayPostCount = 5, dailyPostTarget = 8)
        val decisionUnder = freeTierGuard.canGeneratePost(underQuota)
        assertTrue(decisionUnder is GenerationDecision.Allowed)

        val atQuota = AppSettings(todayPostCount = 8, dailyPostTarget = 8)
        val decisionAt = freeTierGuard.canGeneratePost(atQuota)
        assertTrue(decisionAt is GenerationDecision.QuotaExhausted)
    }

    // 11. Rejected source blocked
    @Test
    fun `test 11 - rejected source opportunity is blocked from meme generation`() {
        val result = memeEngine.checkSourceEligibility(VerificationStatus.REJECTED.name)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("rejected") == true)
    }

    // 12. Expired source blocked
    @Test
    fun `test 12 - expired source opportunity is blocked from meme generation`() {
        val result = memeEngine.checkSourceEligibility(VerificationStatus.EXPIRED.name)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("expired") == true)
    }

    // 13. Generated meme remains unpublished
    @Test
    fun `test 13 - generated meme is saved as draft and Meta publishing remains strictly disabled`() = runBlocking {
        val topic = createSampleTopic()
        val rawJson = """
            {
              "topic": "Student Exam Prep",
              "format": "STUDENT_RELATABLE",
              "setupText": "Studying the entire syllabus in one night",
              "punchlineText": "Question 1 was from the preface page you skipped",
              "caption": "Hostel exam night struggles!",
              "hashtags": ["Exams", "StudentLife"]
            }
        """.trimIndent()

        val outcome = memeEngine.parseAndValidateMeme(rawJson, topic, MemeFormat.STUDENT_RELATABLE)
        assertTrue(outcome is MemeGenerationOutcome.Success)
        val draft = (outcome as MemeGenerationOutcome.Success).draft

        val entity = MemeDraftEntity.fromDraft(draft)
        assertNotEquals("PUBLISHED", entity.generationStatus)

        // Meta publisher is strictly disabled
        val publisher = FoundationDisabledMetaPublisher()
        val result = publisher.publishFacebookPost("page_123", entity.caption)
        assertTrue(result is PublishResult.Disabled)
    }
}
