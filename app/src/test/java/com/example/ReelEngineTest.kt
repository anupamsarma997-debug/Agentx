package com.example

import com.example.data.local.settings.AppSettings
import com.example.data.model.opportunity.OpportunityCategory
import com.example.data.model.opportunity.OpportunityRegion
import com.example.data.model.opportunity.VerificationStatus
import com.example.data.model.reel.ReelGenerationOutcome
import com.example.data.model.reel.ReelGenerationStatus
import com.example.data.model.reel.ReelLanguage
import com.example.data.model.reel.ReelSafetyStatus
import com.example.data.model.reel.ReelScene
import com.example.data.model.reel.ReelTopic
import com.example.data.model.reel.ReelType
import com.example.data.remote.ai.AIResult
import com.example.data.remote.ai.GeminiClient
import com.example.domain.publishing.FoundationDisabledMetaPublisher
import com.example.domain.automation.FreeTierGuard
import com.example.domain.engine.ReelDuplicateDetector
import com.example.domain.engine.ReelEngine
import com.example.domain.engine.ReelSafetyEngine
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
class ReelEngineTest {

    private lateinit var safetyEngine: ReelSafetyEngine
    private lateinit var freeTierGuard: FreeTierGuard
    private lateinit var testTopic: ReelTopic

    @Before
    fun setup() {
        safetyEngine = ReelSafetyEngine()
        freeTierGuard = FreeTierGuard()
        testTopic = ReelTopic(
            topic = "Assam Startup Seed Fund 2026",
            context = "Government of Assam announced ₹25 Lakhs seed grant for early-stage tech startups. Apply online at https://startup.assam.gov.in before 30th April 2026. Minimum eligibility: registered DPIIT startup in Assam.",
            category = OpportunityCategory.STARTUP.name,
            region = OpportunityRegion.ASSAM.name,
            sourceName = "Assam Startup Mission",
            sourceUrl = "https://startup.assam.gov.in",
            verificationStatus = VerificationStatus.VERIFIED.name,
            sourceOpportunityId = "opp_assam_startup_001",
            deadline = "30th April 2026",
            eligibility = "DPIIT registered startup in Assam"
        )
    }

    private fun createValidJson(
        durationSeconds: Int = 30,
        hook: String = "Assam founders, ₹25 Lakhs grant alert!",
        scenes: List<Pair<Int, String>> = listOf(
            Pair(5, "Scene 1 Hook"),
            Pair(10, "Scene 2 Details"),
            Pair(10, "Scene 3 Eligibility"),
            Pair(5, "Scene 4 Source")
        ),
        sourceUrl: String = "https://startup.assam.gov.in",
        hashtags: List<String> = listOf("Assam", "Startups", "Grants"),
        customCaption: String? = null
    ): String {
        val totalSec = scenes.sumOf { it.first }
        val scenesJson = scenes.mapIndexed { idx, pair ->
            """
            {
              "sceneNumber": ${idx + 1},
              "durationSeconds": ${pair.first},
              "visualDescription": "Visual for scene ${idx + 1}",
              "onScreenText": "${pair.second}",
              "voiceoverText": "Voiceover explaining ${pair.second}",
              "transition": "Cut"
            }
            """.trimIndent()
        }.joinToString(",")

        val tagsJson = hashtags.joinToString(",") { "\"$it\"" }
        val caption = customCaption ?: "Big grant for Assam founders! Apply before 30th April 2026. Source: Assam Startup Mission - $sourceUrl"

        return """
        {
          "title": "Assam Startup Seed Fund 2026",
          "reelType": "STARTUP_REEL",
          "durationSeconds": $totalSec,
          "hook": "$hook",
          "scenes": [$scenesJson],
          "voiceover": "Full voiceover script covering the seed fund.",
          "caption": "$caption",
          "hashtags": [$tagsJson],
          "sourceUrl": "$sourceUrl",
          "needsReview": false
        }
        """.trimIndent()
    }

    // 1. 15-second Reel validation
    @Test
    fun test1_15SecondReelValidation() = runBlocking {
        val json15 = createValidJson(
            durationSeconds = 15,
            scenes = listOf(
                Pair(3, "Quick hook"),
                Pair(7, "Main grant info"),
                Pair(5, "Source: Assam Startup Mission")
            )
        )
        val engine = ReelEngine(safetyEngine = safetyEngine, freeTierGuard = freeTierGuard)
        val outcome = engine.parseAndValidateReel(
            rawText = json15,
            topic = testTopic,
            targetReelType = ReelType.STARTUP_REEL,
            expectedDuration = 15,
            language = ReelLanguage.ENGLISH
        )
        assertTrue("15s Reel should be valid", outcome is ReelGenerationOutcome.Success)
        val draft = (outcome as ReelGenerationOutcome.Success).draft
        assertEquals(15, draft.durationSeconds)
        assertEquals(15, draft.totalSceneDuration)
        assertTrue(draft.isDurationAligned)
    }

    // 2. 30-second Reel validation
    @Test
    fun test2_30SecondReelValidation() = runBlocking {
        val json30 = createValidJson(
            durationSeconds = 30,
            scenes = listOf(
                Pair(4, "Hook 0-4s"),
                Pair(10, "Details 4-14s"),
                Pair(10, "Eligibility 14-24s"),
                Pair(6, "Source: Assam Startup Mission")
            )
        )
        val engine = ReelEngine(safetyEngine = safetyEngine, freeTierGuard = freeTierGuard)
        val outcome = engine.parseAndValidateReel(
            rawText = json30,
            topic = testTopic,
            targetReelType = ReelType.STARTUP_REEL,
            expectedDuration = 30,
            language = ReelLanguage.ENGLISH
        )
        assertTrue("30s Reel should be valid", outcome is ReelGenerationOutcome.Success)
        val draft = (outcome as ReelGenerationOutcome.Success).draft
        assertEquals(30, draft.durationSeconds)
        assertEquals(30, draft.totalSceneDuration)
    }

    // 3. 60-second Reel validation
    @Test
    fun test3_60SecondReelValidation() = runBlocking {
        val json60 = createValidJson(
            durationSeconds = 60,
            scenes = listOf(
                Pair(5, "Hook"),
                Pair(15, "Overview"),
                Pair(15, "Deep Dive"),
                Pair(15, "Application Steps"),
                Pair(10, "Source: Assam Startup Mission")
            )
        )
        val engine = ReelEngine(safetyEngine = safetyEngine, freeTierGuard = freeTierGuard)
        val outcome = engine.parseAndValidateReel(
            rawText = json60,
            topic = testTopic,
            targetReelType = ReelType.STARTUP_REEL,
            expectedDuration = 60,
            language = ReelLanguage.ENGLISH
        )
        assertTrue("60s Reel should be valid", outcome is ReelGenerationOutcome.Success)
        val draft = (outcome as ReelGenerationOutcome.Success).draft
        assertEquals(60, draft.durationSeconds)
        assertEquals(60, draft.totalSceneDuration)
    }

    // 4. scene durations equal total duration
    @Test
    fun test4_sceneDurationsMismatchRejected() = runBlocking {
        val mismatchedJson = """
        {
          "title": "Mismatched Reel",
          "reelType": "STARTUP_REEL",
          "durationSeconds": 30,
          "hook": "Valid hook here",
          "scenes": [
            { "sceneNumber": 1, "durationSeconds": 5, "visualDescription": "v", "onScreenText": "t", "voiceoverText": "v" },
            { "sceneNumber": 2, "durationSeconds": 10, "visualDescription": "v", "onScreenText": "t", "voiceoverText": "v" }
          ],
          "voiceover": "Full voiceover",
          "caption": "Source: Assam Startup Mission https://startup.assam.gov.in",
          "hashtags": ["test"],
          "sourceUrl": "https://startup.assam.gov.in"
        }
        """.trimIndent() // Total scene duration is 15, expected is 30
        val engine = ReelEngine(safetyEngine = safetyEngine, freeTierGuard = freeTierGuard)
        val outcome = engine.parseAndValidateReel(
            rawText = mismatchedJson,
            topic = testTopic,
            targetReelType = ReelType.STARTUP_REEL,
            expectedDuration = 30,
            language = ReelLanguage.ENGLISH
        )
        assertTrue("Scene duration sum mismatch must be rejected", outcome is ReelGenerationOutcome.ValidationFailed)
        val err = (outcome as ReelGenerationOutcome.ValidationFailed).error
        assertTrue(err.contains("does not equal total Reel duration"))
    }

    // 5. duration >60 rejected
    @Test
    fun test5_durationExceeding60Rejected() = runBlocking {
        val json75 = createValidJson(
            durationSeconds = 75,
            scenes = listOf(Pair(75, "Single long scene"))
        )
        val engine = ReelEngine(safetyEngine = safetyEngine, freeTierGuard = freeTierGuard)
        val outcome = engine.parseAndValidateReel(
            rawText = json75,
            topic = testTopic,
            targetReelType = ReelType.STARTUP_REEL,
            expectedDuration = 75,
            language = ReelLanguage.ENGLISH
        )
        assertTrue("Duration > 60 must be rejected", outcome is ReelGenerationOutcome.ValidationFailed)
        val err = (outcome as ReelGenerationOutcome.ValidationFailed).error
        assertTrue(err.contains("exceeds maximum limit of 60 seconds"))
    }

    // 6. invalid JSON rejected
    @Test
    fun test6_invalidJsonRejected() = runBlocking {
        val malformed = "This is not a JSON object at all."
        val engine = ReelEngine(safetyEngine = safetyEngine, freeTierGuard = freeTierGuard)
        val outcome = engine.parseAndValidateReel(
            rawText = malformed,
            topic = testTopic,
            targetReelType = ReelType.STARTUP_REEL,
            expectedDuration = 30,
            language = ReelLanguage.ENGLISH
        )
        assertTrue("Malformed JSON must be rejected", outcome is ReelGenerationOutcome.ValidationFailed)
        val err = (outcome as ReelGenerationOutcome.ValidationFailed).error
        assertTrue(err.contains("Invalid JSON"))
    }

    // 7. missing hook rejected
    @Test
    fun test7_missingHookRejected() = runBlocking {
        val noHookJson = """
        {
          "title": "Reel without hook",
          "reelType": "STARTUP_REEL",
          "durationSeconds": 15,
          "hook": "",
          "scenes": [
            { "sceneNumber": 1, "durationSeconds": 15, "visualDescription": "v", "onScreenText": "t", "voiceoverText": "v" }
          ],
          "voiceover": "v",
          "caption": "Source: Assam Startup Mission https://startup.assam.gov.in",
          "hashtags": ["tag"],
          "sourceUrl": "https://startup.assam.gov.in"
        }
        """.trimIndent()
        val engine = ReelEngine(safetyEngine = safetyEngine, freeTierGuard = freeTierGuard)
        val outcome = engine.parseAndValidateReel(
            rawText = noHookJson,
            topic = testTopic,
            targetReelType = ReelType.STARTUP_REEL,
            expectedDuration = 15,
            language = ReelLanguage.ENGLISH
        )
        assertTrue("Empty hook must be rejected", outcome is ReelGenerationOutcome.ValidationFailed)
        val err = (outcome as ReelGenerationOutcome.ValidationFailed).error
        assertTrue(err.contains("Missing hook"))
    }

    // 8. source URL protection (AI cannot replace or modify stored source URL)
    @Test
    fun test8_sourceUrlProtectionRejectsAlteredUrl() = runBlocking {
        val alteredUrlJson = createValidJson(
            durationSeconds = 15,
            scenes = listOf(Pair(15, "Scene 1")),
            sourceUrl = "https://fake-phishing-url.com/grant"
        )
        val engine = ReelEngine(safetyEngine = safetyEngine, freeTierGuard = freeTierGuard)
        val outcome = engine.parseAndValidateReel(
            rawText = alteredUrlJson,
            topic = testTopic, // has https://startup.assam.gov.in
            targetReelType = ReelType.STARTUP_REEL,
            expectedDuration = 15,
            language = ReelLanguage.ENGLISH
        )
        assertTrue("Altered URL must be rejected", outcome is ReelGenerationOutcome.ValidationFailed)
        val err = (outcome as ReelGenerationOutcome.ValidationFailed).error
        assertTrue(err.contains("differs from stored source URL"))
    }

    // 9. fabricated deadline rejection
    @Test
    fun test9_fabricatedDeadlineRejection() = runBlocking {
        // Topic has deadline "30th April 2026", but AI claims "15th December 2028"
        val fakeDeadlineJson = createValidJson(
            durationSeconds = 15,
            scenes = listOf(Pair(15, "Apply before 15th December 2028!")),
            customCaption = "Apply before 15th December 2028. Source: Assam Startup Mission https://startup.assam.gov.in"
        )
        val engine = ReelEngine(safetyEngine = safetyEngine, freeTierGuard = freeTierGuard)
        val outcome = engine.parseAndValidateReel(
            rawText = fakeDeadlineJson,
            topic = testTopic,
            targetReelType = ReelType.STARTUP_REEL,
            expectedDuration = 15,
            language = ReelLanguage.ENGLISH
        )
        assertTrue("Fabricated deadline must be rejected", outcome is ReelGenerationOutcome.ValidationFailed)
        val err = (outcome as ReelGenerationOutcome.ValidationFailed).error
        assertTrue(err.contains("Fabricated deadline date detected"))
    }

    // 10. hashtag limit (>8 rejected)
    @Test
    fun test10_hashtagLimitRejected() = runBlocking {
        val nineHashtags = listOf("tag1", "tag2", "tag3", "tag4", "tag5", "tag6", "tag7", "tag8", "tag9")
        val json9Tags = createValidJson(
            durationSeconds = 15,
            scenes = listOf(Pair(15, "Scene 1")),
            hashtags = nineHashtags
        )
        val engine = ReelEngine(safetyEngine = safetyEngine, freeTierGuard = freeTierGuard)
        val outcome = engine.parseAndValidateReel(
            rawText = json9Tags,
            topic = testTopic,
            targetReelType = ReelType.STARTUP_REEL,
            expectedDuration = 15,
            language = ReelLanguage.ENGLISH
        )
        assertTrue("Hashtag count > 8 must be rejected", outcome is ReelGenerationOutcome.ValidationFailed)
        val err = (outcome as ReelGenerationOutcome.ValidationFailed).error
        assertTrue(err.contains("Too many hashtags"))
    }

    // 11. political content requires review
    @Test
    fun test11_politicalContentRequiresReview() = runBlocking {
        val politicalEvaluation = safetyEngine.evaluate(
            topic = "Assam Assembly Election Updates 2026",
            hook = "Important updates on upcoming Vidhan Sabha election.",
            scenes = listOf(
                ReelScene(1, 15, "Assembly building", "Vidhan Sabha election schedule", "Election commission notification", "Cut")
            ),
            voiceover = "Election commission notification for Vidhan Sabha.",
            caption = "Source: Assam Election Commission https://ceoassam.nic.in"
        )
        assertEquals("Political/election content must require review", ReelSafetyStatus.NEEDS_REVIEW, politicalEvaluation.status)
        assertTrue(politicalEvaluation.isPolitical)
    }

    // 12. unsafe content blocked
    @Test
    fun test12_unsafeContentBlocked() = runBlocking {
        val unsafeEvaluation = safetyEngine.evaluate(
            topic = "Hostile Takeover",
            hook = "Guaranteed 1000% return crypto hack schemes",
            scenes = listOf(
                ReelScene(1, 15, "Hack screens", "Guaranteed 1000% return crypto hack", "Bank phishing script", "Cut")
            ),
            voiceover = "How to do bank phishing",
            caption = "Scam script"
        )
        assertEquals("Scam and malicious content must be BLOCKED", ReelSafetyStatus.BLOCKED, unsafeEvaluation.status)
        assertTrue(unsafeEvaluation.reasons.isNotEmpty())
    }

    // 13. duplicate Reel detection
    @Test
    fun test13_duplicateReelDetection() {
        val hash1 = ReelDuplicateDetector.computeHash(
            sourceId = "opp_123",
            reelType = "STARTUP_REEL",
            hook = "Assam founders, ₹25 Lakhs grant alert!",
            topic = "Assam Startup Seed Fund 2026"
        )
        val hash2 = ReelDuplicateDetector.computeHash(
            sourceId = "OPP_123",
            reelType = "startup_reel",
            hook = "assam founders, ₹25 lakhs grant alert!",
            topic = "assam startup seed fund 2026"
        )
        assertEquals("Normalized identical fields must yield identical hash", hash1, hash2)

        val hashDifferent = ReelDuplicateDetector.computeHash(
            sourceId = "opp_999",
            reelType = "STARTUP_REEL",
            hook = "A completely different hook!",
            topic = "Assam Startup Seed Fund 2026"
        )
        assertNotEquals("Different source or hook must produce distinct hash", hash1, hashDifferent)
    }

    // 14. Reel quota respected
    @Test
    fun test14_reelQuotaRespected() {
        val freshSettings = AppSettings(freeMode = true, todayReelCount = 0, dailyReelTarget = 2)
        val decision = freeTierGuard.canGenerateReel(freshSettings)
        assertTrue("Fresh settings should allow Reel generation", decision is com.example.domain.automation.GenerationDecision.Allowed)

        val exhaustedSettings = AppSettings(freeMode = true, todayReelCount = 2, dailyReelTarget = 2)
        val exhaustedDecision = freeTierGuard.canGenerateReel(exhaustedSettings)
        assertTrue("Exhausted settings must block Reel generation", exhaustedDecision is com.example.domain.automation.GenerationDecision.QuotaExhausted)
    }

    // 15. Gemini not called after quota reached
    @Test
    fun test15_geminiNotCalledAfterQuotaReached() = runBlocking {
        var geminiCallCount = 0
        val mockGemini = object : GeminiClient() {
            override suspend fun generateContent(systemPrompt: String, userPrompt: String): AIResult {
                geminiCallCount++
                return AIResult.Success("{}")
            }
        }
        val engine = ReelEngine(
            geminiClient = mockGemini,
            safetyEngine = safetyEngine,
            freeTierGuard = freeTierGuard
        )
        val exhaustedSettings = AppSettings(freeMode = true, todayReelCount = 2, dailyReelTarget = 2)
        val outcome = engine.generateReel(
            topic = testTopic,
            reelType = ReelType.STARTUP_REEL,
            durationSeconds = 30,
            language = ReelLanguage.ENGLISH,
            settings = exhaustedSettings
        )
        assertTrue("Should return QuotaExhausted", outcome is ReelGenerationOutcome.QuotaExhausted)
        assertEquals("Gemini API must NOT be called when quota is reached", 0, geminiCallCount)
    }

    // 16. rejected source blocked
    @Test
    fun test16_rejectedSourceBlocked() = runBlocking {
        val rejectedTopic = testTopic.copy(verificationStatus = VerificationStatus.REJECTED.name)
        val engine = ReelEngine(safetyEngine = safetyEngine, freeTierGuard = freeTierGuard)
        val outcome = engine.generateReel(
            topic = rejectedTopic,
            reelType = ReelType.STARTUP_REEL,
            durationSeconds = 30
        )
        assertTrue("Rejected source must be blocked from Reel generation", outcome is ReelGenerationOutcome.SourceIneligible)
    }

    // 17. expired source blocked
    @Test
    fun test17_expiredSourceBlocked() = runBlocking {
        val expiredTopic = testTopic.copy(verificationStatus = VerificationStatus.EXPIRED.name)
        val engine = ReelEngine(safetyEngine = safetyEngine, freeTierGuard = freeTierGuard)
        val outcome = engine.generateReel(
            topic = expiredTopic,
            reelType = ReelType.STARTUP_REEL,
            durationSeconds = 30
        )
        assertTrue("Expired source must be blocked from Reel generation", outcome is ReelGenerationOutcome.SourceIneligible)
    }

    // 18. generated Reel remains unpublished (no Meta publishing in Phase 7)
    @Test
    fun test18_generatedReelRemainsUnpublished() = runBlocking {
        val json = createValidJson(
            durationSeconds = 30,
            scenes = listOf(
                Pair(5, "Hook"),
                Pair(10, "Details"),
                Pair(10, "Eligibility"),
                Pair(5, "Source: Assam Startup Mission")
            )
        )
        val engine = ReelEngine(safetyEngine = safetyEngine, freeTierGuard = freeTierGuard)
        val outcome = engine.parseAndValidateReel(
            rawText = json,
            topic = testTopic,
            targetReelType = ReelType.STARTUP_REEL,
            expectedDuration = 30,
            language = ReelLanguage.ENGLISH
        )
        assertTrue(outcome is ReelGenerationOutcome.Success)
        val draft = (outcome as ReelGenerationOutcome.Success).draft

        // Assert state is DRAFT or REVIEW_REQUIRED, NEVER PUBLISHED
        assertNotEquals("Reel must NEVER be PUBLISHED in Phase 7", ReelGenerationStatus.PUBLISHED, draft.generationStatus)
        assertTrue("Initial status must be DRAFT or REVIEW_REQUIRED",
            draft.generationStatus == ReelGenerationStatus.DRAFT || draft.generationStatus == ReelGenerationStatus.REVIEW_REQUIRED)

        // MetaPublisher check
        val publisher = FoundationDisabledMetaPublisher()
        val publishResult = publisher.publishInstagramReel("ig_test", "https://example.com/video.mp4", "caption")
        assertTrue("Meta Reel publishing must be Disabled in Phase 7", publishResult is com.example.domain.publishing.PublishResult.Disabled)
    }
}
