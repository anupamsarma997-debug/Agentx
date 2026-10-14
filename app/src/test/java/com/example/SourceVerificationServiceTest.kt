package com.example

import com.example.data.model.content.ContentType
import com.example.data.model.opportunity.OpportunityCategory
import com.example.data.model.opportunity.OpportunityRegion
import com.example.data.model.opportunity.VerificationStatus
import com.example.domain.engine.ContentCreationEngine
import com.example.domain.engine.ContentCreationOutcome
import com.example.domain.model.SourceFact
import com.example.domain.verification.SourceRiskLevel
import com.example.domain.verification.SourceVerificationService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SourceVerificationServiceTest {

    private lateinit var service: SourceVerificationService
    private lateinit var creationEngine: ContentCreationEngine

    @Before
    fun setUp() {
        service = SourceVerificationService()
        creationEngine = ContentCreationEngine()
    }

    @Test
    fun `test direct official government domain verification for Gauhati High Court`() = runBlocking {
        val result = service.verifySource(
            url = "https://ghconline.gov.in/Recruitment.html",
            title = "Gauhati High Court LDA and Copyist Recruitment Notice",
            sourceName = "Gauhati High Court Official Portal",
            deadline = null
        )

        assertTrue("Direct ghconline.gov.in URL should be verified", result.isVerified)
        assertTrue("Domain should be classified as government domain", result.isGovernmentDomain)
        assertEquals("Primary domain should match ghconline.gov.in", "ghconline.gov.in", result.primaryDomain)
        assertEquals("Risk level should be SAFE_OFFICIAL", SourceRiskLevel.SAFE_OFFICIAL, result.riskLevel)
        assertEquals("Verification status should be VERIFIED", VerificationStatus.VERIFIED, result.verificationStatus)
    }

    @Test
    fun `test expired deadline detection flags suspicious or expired risk`() = runBlocking {
        // User pointed out "Last date: 10 February 2026 likhi hai, jo beet chuki hai"
        val expiredResult = service.verifySource(
            url = "https://example-news-blog.com/ghc-jobs-2026",
            title = "Gauhati High Court Recruitment 2026",
            sourceName = "Third Party Blog",
            description = "Last date to apply was 10 February 2026",
            deadline = "10/02/2026"
        )

        assertEquals(
            "Expired deadline must be flagged as SUSPICIOUS_OR_EXPIRED",
            SourceRiskLevel.SUSPICIOUS_OR_EXPIRED,
            expiredResult.riskLevel
        )
        assertTrue("Verification summary should mention deadline has passed", expiredResult.verificationSummary.contains("passed") || expiredResult.verificationSummary.contains("Expired"))
        assertTrue("isVerified should be false for expired notices", !expiredResult.isVerified)
    }

    @Test
    fun `test checkDeadlineExpired helper accurately identifies past and future dates`() {
        // Past date
        val past = service.checkDeadlineExpired(deadline = "10/02/2026", textSnippet = null)
        assertNotNull("10/02/2026 should be identified as expired", past)

        // Future date (year 2030)
        val future = service.checkDeadlineExpired(deadline = "10/12/2030", textSnippet = null)
        assertNull("Future date 10/12/2030 should not be expired", future)
    }

    @Test
    fun `test secondary news content matching cross-references to official canonical portal`() = runBlocking {
        val result = service.verifySource(
            url = "https://newslivetv.com/article/12345",
            title = "গুৱাহাটী উচ্চ ন্যায়ালয় নিযুক্তিৰ সবিশেষ ঘোষণা",
            sourceName = "News Live",
            description = "Gauhati High Court Recruitment news update"
        )

        assertTrue("Should be verified via cross-reference", result.isVerified)
        assertEquals("Canonical URL should be mapped to ghconline.gov.in", "https://ghconline.gov.in/Recruitment.html", result.canonicalOfficialUrl)
        assertEquals("Risk level should be CROSS_REFERENCED_NEWS", SourceRiskLevel.CROSS_REFERENCED_NEWS, result.riskLevel)
    }

    @Test
    fun `test ContentCreationEngine blocks publication when deadline is expired`() = runBlocking {
        val expiredFact = SourceFact(
            title = "Unverified recruitment notification",
            organization = "Unverified Organization",
            description = "Recruitment notice with past deadline: 10 February 2026",
            category = OpportunityCategory.GOVERNMENT_JOB.name,
            region = OpportunityRegion.ASSAM.name,
            eligibility = "Graduation with computer diploma",
            deadline = "10/02/2026",
            publishedAt = "2026-01-15",
            sourceName = "Unverified Secondary",
            sourceUrl = "https://unverified-site.com/notice",
            verificationStatus = VerificationStatus.NEEDS_REVIEW.name
        )

        val outcome = creationEngine.generateContent(
            fact = expiredFact,
            contentType = ContentType.JOB_ALERT
        )

        assertTrue(
            "Outcome should be Error when deadline is expired",
            outcome is ContentCreationOutcome.Error
        )
        val errorMsg = (outcome as ContentCreationOutcome.Error).message
        assertTrue(
            "Error message should explain that post generation/publication is prevented due to expired deadline",
            errorMsg.contains("Publication blocked") || errorMsg.contains("expired")
        )
    }
}
