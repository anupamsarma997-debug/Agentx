package com.example

import com.example.data.local.entity.OpportunityEntity
import com.example.data.model.opportunity.OpportunityCategory
import com.example.data.model.opportunity.OpportunityRegion
import com.example.data.model.opportunity.SourceTier
import com.example.data.model.opportunity.VerificationStatus
import com.example.data.model.verification.FinalVerificationStatus
import com.example.data.model.verification.GeneratedContentFact
import com.example.data.model.verification.PublishReadiness
import com.example.data.model.verification.RejectionReason
import com.example.domain.engine.ApprovalDecision
import com.example.domain.engine.ApprovalEngine
import com.example.domain.engine.ContentSafetyEngine
import com.example.domain.engine.FinalVerificationEngine
import com.example.domain.engine.SafetyLevel
import com.example.domain.publishing.FoundationDisabledMetaPublisher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FinalVerificationTest {

    private lateinit var contentSafetyEngine: ContentSafetyEngine
    private lateinit var verificationEngine: FinalVerificationEngine
    private lateinit var approvalEngine: ApprovalEngine
    private lateinit var disabledPublisher: FoundationDisabledMetaPublisher

    @Before
    fun setUp() {
        contentSafetyEngine = ContentSafetyEngine()
        verificationEngine = FinalVerificationEngine(contentSafetyEngine)
        approvalEngine = ApprovalEngine()
        disabledPublisher = FoundationDisabledMetaPublisher()
    }

    private fun createSampleOpportunity(
        id: String = "opp_test_1",
        title: String = "Assam Police Sub-Inspector Recruitment 2026",
        description: String = "State Level Police Recruitment Board Assam invites applications for 500 Sub Inspector posts. Deadline is 2026-10-10. Minimum age 20 years, graduate degree required.",
        category: String = OpportunityCategory.GOVERNMENT_JOB.name,
        region: String = OpportunityRegion.ASSAM.name,
        sourceName: String = "SLPRB Assam",
        sourceUrl: String = "https://slprbassam.in/si-recruitment-2026",
        sourceDomain: String = "slprbassam.in",
        deadline: String? = "2026-10-10",
        eligibility: String? = "Graduate degree, Age 20-26",
        organization: String? = "State Level Police Recruitment Board Assam",
        verificationStatus: String = VerificationStatus.VERIFIED.name
    ): OpportunityEntity {
        return OpportunityEntity(
            id = id,
            title = title,
            description = description,
            category = category,
            region = region,
            sourceName = sourceName,
            sourceUrl = sourceUrl,
            sourceDomain = sourceDomain,
            deadline = deadline,
            eligibility = eligibility,
            organization = organization,
            sourceTier = SourceTier.TIER_1_OFFICIAL.name,
            verificationStatus = verificationStatus,
            contentHash = "hash_$id",
            isExpired = false
        )
    }

    @Test
    fun testValidContentPassesVerificationAndIsReadyForApproval() {
        val opp = createSampleOpportunity()
        val contentFact = GeneratedContentFact(
            title = "Assam Police Sub-Inspector Recruitment 2026",
            body = "State Level Police Recruitment Board Assam has announced 500 SI vacancies. Eligibility: Graduate degree. Apply before 2026-10-10 at official portal.",
            caption = "Official notice from SLPRB Assam: https://slprbassam.in/si-recruitment-2026 #AssamJobs",
            deadline = "2026-10-10",
            eligibility = "Graduate degree",
            organization = "State Level Police Recruitment Board Assam",
            sourceUrl = "https://slprbassam.in/si-recruitment-2026",
            hashtags = listOf("#AssamJobs", "#SLPRB")
        )

        val result = verificationEngine.verifyContent(
            contentId = "post_1",
            source = opp,
            contentFact = contentFact,
            platform = "INSTAGRAM"
        )

        assertTrue("Source verified must be true", result.sourceVerified)
        assertTrue("Source URL valid must be true", result.sourceUrlValid)
        assertTrue("Facts consistent must be true", result.factsConsistent)
        assertTrue("Deadline consistent must be true", result.deadlineConsistent)
        assertTrue("Safety passed must be true", result.safetyPassed)
        assertFalse("Duplicate free must be true", !result.duplicateFree)
        assertFalse("Human review not required for clean government opportunity", result.humanReviewRequired)
        assertEquals("Status must be PASSED", FinalVerificationStatus.PASSED, result.status)

        val decision = approvalEngine.evaluateApproval(result)
        assertTrue("Approval decision must be Allowed", decision is ApprovalDecision.Allowed)
        assertEquals(PublishReadiness.READY_FOR_PUBLISHER, (decision as ApprovalDecision.Allowed).readiness)
    }

    @Test
    fun testMismatchedDeadlineFailsVerification() {
        val opp = createSampleOpportunity(deadline = "2026-10-10")
        val contentFact = GeneratedContentFact(
            title = "Assam Police Recruitment",
            body = "Apply now before deadline 2026-10-15!", // Mismatched deadline
            caption = "Source: SLPRB Assam https://slprbassam.in/si-recruitment-2026",
            deadline = "2026-10-15",
            sourceUrl = "https://slprbassam.in/si-recruitment-2026"
        )

        val result = verificationEngine.verifyContent(
            contentId = "post_bad_deadline",
            source = opp,
            contentFact = contentFact,
            platform = "INSTAGRAM"
        )

        assertFalse("Deadline consistent must be false", result.deadlineConsistent)
        assertEquals("Status must be FAILED due to mismatched deadline", FinalVerificationStatus.FAILED, result.status)

        val decision = approvalEngine.evaluateApproval(result)
        assertTrue("Approval must be disallowed for mismatched deadline", decision is ApprovalDecision.Disallowed)
    }

    @Test
    fun testExpiredSourceIsMarkedExpiredAndBlockedFromApproval() {
        val opp = createSampleOpportunity(deadline = "2020-01-01") // Expired in past
        val contentFact = GeneratedContentFact(
            title = "Old Police Recruitment",
            body = "Apply before 2020-01-01.",
            caption = "Source: SLPRB Assam https://slprbassam.in/si-recruitment-2026",
            sourceUrl = "https://slprbassam.in/si-recruitment-2026"
        )

        val result = verificationEngine.verifyContent(
            contentId = "post_expired",
            source = opp,
            contentFact = contentFact,
            platform = "INSTAGRAM"
        )

        assertEquals("Status must be EXPIRED", FinalVerificationStatus.EXPIRED, result.status)
        val decision = approvalEngine.evaluateApproval(result)
        assertTrue("Approval must be disallowed for expired source", decision is ApprovalDecision.Disallowed)
        assertFalse("Cannot manually approve expired content", approvalEngine.canManuallyApprove(result))
    }

    @Test
    fun testFabricatedUrlFailsVerification() {
        val opp = createSampleOpportunity(sourceUrl = "https://slprbassam.in/si-recruitment-2026")
        val contentFact = GeneratedContentFact(
            title = "Assam Police Jobs",
            body = "Click this link to apply: https://fake-phishing-jobs.com/apply", // Fabricated foreign URL
            caption = "Source: SLPRB Assam",
            sourceUrl = "https://fake-phishing-jobs.com/apply"
        )

        val result = verificationEngine.verifyContent(
            contentId = "post_fake_url",
            source = opp,
            contentFact = contentFact,
            platform = "INSTAGRAM"
        )

        assertFalse("Source URL valid must be false", result.sourceUrlValid)
        assertFalse("Facts consistent must be false due to fabricated URL", result.factsConsistent)
        val decision = approvalEngine.evaluateApproval(result)
        assertTrue("Approval must be disallowed for fabricated URL", decision is ApprovalDecision.Disallowed)
    }

    @Test
    fun testContentSafetyBlocksHateSpeechAndScams() {
        val opp = createSampleOpportunity()
        val hateContent = GeneratedContentFact(
            title = "Hate Speech Alert",
            body = "We should attack and destroy all muslims and cause genocide.",
            caption = "Hate campaign"
        )

        val result = verificationEngine.verifyContent(
            contentId = "post_hate",
            source = opp,
            contentFact = hateContent,
            platform = "INSTAGRAM"
        )

        assertEquals("Status must be BLOCKED for hate speech", FinalVerificationStatus.BLOCKED, result.status)
        assertFalse("Safety passed must be false", result.safetyPassed)
        val decision = approvalEngine.evaluateApproval(result)
        assertTrue("Approval must be strictly disallowed for blocked content", decision is ApprovalDecision.Disallowed)
        assertFalse("Manual approval strictly prohibited for BLOCKED content", approvalEngine.canManuallyApprove(result))
    }

    @Test
    fun testPoliticalAffairsRequiresHumanReview() {
        val opp = createSampleOpportunity()
        val politicalNews = GeneratedContentFact(
            title = "Assam Assembly Election Schedule Announced",
            body = "The Election Commission announced the schedule for Assam Vidhan Sabha elections. All major parties including AGP and Congress will participate.",
            caption = "SLPRB Assam https://slprbassam.in/si-recruitment-2026 #Election2026",
            sourceUrl = "https://slprbassam.in/si-recruitment-2026"
        )

        val result = verificationEngine.verifyContent(
            contentId = "post_political",
            source = opp,
            contentFact = politicalNews,
            platform = "INSTAGRAM"
        )

        assertTrue("Political review must be required", result.politicalReviewRequired)
        assertTrue("Human review must be required", result.humanReviewRequired)
        assertEquals("Status must be NEEDS_REVIEW", FinalVerificationStatus.NEEDS_REVIEW, result.status)

        val decision = approvalEngine.evaluateApproval(result)
        assertTrue("Approval engine must flag RequiresHumanReview", decision is ApprovalDecision.RequiresHumanReview)
    }

    @Test
    fun testPoliticalPersuasionAndVoterTargetingIsBlocked() {
        val opp = createSampleOpportunity()
        val persuasionContent = GeneratedContentFact(
            title = "Election Campaign 2026",
            body = "Cast your vote for BJP candidate in upcoming election rally. Elect our leader!",
            caption = "SLPRB Assam https://slprbassam.in/si-recruitment-2026 #Election2026",
            sourceUrl = "https://slprbassam.in/si-recruitment-2026"
        )

        val result = verificationEngine.verifyContent(
            contentId = "post_persuasion",
            source = opp,
            contentFact = persuasionContent,
            platform = "INSTAGRAM"
        )

        assertEquals("Status must be BLOCKED for voter persuasion", FinalVerificationStatus.BLOCKED, result.status)
        assertTrue("Political review required is true", result.politicalReviewRequired)
        assertTrue("Human review required is true", result.humanReviewRequired)
        val decision = approvalEngine.evaluateApproval(result)
        assertTrue("Approval must be disallowed for voter targeting", decision is ApprovalDecision.Disallowed)
    }

    @Test
    fun testManualApprovalAndRejectionRecords() {
        val approvalRecord = approvalEngine.buildManualApproval("Editor verified official gazette.")
        assertEquals("HUMAN", approvalRecord.approvalType)
        assertEquals("Editor verified official gazette.", approvalRecord.approvalReason)
        assertTrue("approvedAt must be recent", approvalRecord.approvedAt > 0)

        val rejectionRecord = approvalEngine.buildRejection(RejectionReason.WRONG_DEADLINE, "Deadline in caption was incorrect.")
        assertEquals(RejectionReason.WRONG_DEADLINE, rejectionRecord.reason)
        assertEquals("Deadline in caption was incorrect.", rejectionRecord.note)
        assertTrue("rejectedAt must be recent", rejectionRecord.rejectedAt > 0)
    }

    @Test
    fun testMetaPublisherRemainsStrictlyDisabled() = kotlinx.coroutines.runBlocking {
        val publishResult = disabledPublisher.publishInstagramPhoto("ig_1", "https://example.com/img.png", "Caption")
        assertTrue("Publishing must return Disabled status", publishResult is com.example.domain.publishing.PublishResult.Disabled)
        val disabledMessage = (publishResult as com.example.domain.publishing.PublishResult.Disabled).message
        assertTrue("Failure reason must mention disabled", disabledMessage.contains("disabled", ignoreCase = true))

        val reelResult = disabledPublisher.publishInstagramReel("ig_1", "https://example.com/video.mp4", "Caption")
        assertTrue("Reel publishing must return Disabled status", reelResult is com.example.domain.publishing.PublishResult.Disabled)
    }
}
