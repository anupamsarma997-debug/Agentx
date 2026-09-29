package com.example

import com.example.data.model.opportunity.OpportunityCategory
import com.example.data.model.opportunity.OpportunityRegion
import com.example.data.model.opportunity.SourceTier
import com.example.data.model.opportunity.VerificationStatus
import com.example.domain.engine.OpportunityNormalizer
import com.example.domain.engine.ParsedDeadline
import com.example.domain.engine.VerificationEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class OpportunityScoutTest {

    private val normalizer = OpportunityNormalizer
    private val verificationEngine = VerificationEngine()

    // 1. Normalization
    @Test
    fun `test 1 - text and title normalization collapses excess whitespace and trims`() {
        val rawTitle = "   APSC   Combined   Competitive   Exam  2026   "
        val normalized = normalizer.normalizeTitle(rawTitle)
        assertEquals("APSC Combined Competitive Exam 2026", normalized)
    }

    // 2. URL Normalization
    @Test
    fun `test 2 - url normalization strips tracking parameters and lowercases scheme host`() {
        val rawUrl = "HTTPS://Apsc.Nic.In/cce.html?utm_source=facebook&utm_medium=cpc&id=101&fbclid=IwAR123#overview"
        val normalized = normalizer.normalizeUrl(rawUrl)
        assertTrue(normalized.startsWith("https://apsc.nic.in/cce.html?id=101"))
        assertFalse(normalized.contains("utm_source"))
        assertFalse(normalized.contains("fbclid"))
        assertFalse(normalized.contains("#overview"))
    }

    // 3. Duplicate Detection via Content Hash
    @Test
    fun `test 3 - duplicate detection produces identical hash for identical semantic inputs`() {
        val hash1 = normalizer.computeContentHash(
            title = "Assam Startup Cohort",
            domain = "startup.assam.gov.in",
            deadline = "2026-12-15",
            organization = "Dept of Industries"
        )
        val hash2 = normalizer.computeContentHash(
            title = "  Assam   Startup  Cohort  ",
            domain = "startup.assam.gov.in",
            deadline = "2026-12-15",
            organization = "Dept of Industries "
        )
        assertEquals(hash1, hash2)
    }

    // 4. Content Hash Consistency
    @Test
    fun `test 4 - content hash differs when key fields differ`() {
        val hash1 = normalizer.computeContentHash(
            title = "Smart India Hackathon",
            domain = "sih.gov.in",
            deadline = "2026-10-31",
            organization = "AICTE"
        )
        val hash2 = normalizer.computeContentHash(
            title = "Smart India Hackathon Junior",
            domain = "sih.gov.in",
            deadline = "2026-10-31",
            organization = "AICTE"
        )
        assertNotEquals(hash1, hash2)
    }

    // 5. Deadline Parsing
    @Test
    fun `test 5 - deadline parsing handles standard date formats`() {
        val parsedIso = normalizer.parseDeadline("2026-11-30")
        assertEquals("2026-11-30", parsedIso.normalizedString)
        assertNotNull(parsedIso.epochMillis)

        val parsedSlash = normalizer.parseDeadline("30/11/2026")
        assertEquals("2026-11-30", parsedSlash.normalizedString)
        assertNotNull(parsedSlash.epochMillis)
    }

    // 6. Expired Deadline Detection
    @Test
    fun `test 6 - expired deadline is flagged when date is in the past`() {
        val pastDate = LocalDate.now().minusDays(10).toString()
        val parsed = normalizer.parseDeadline(pastDate)
        assertTrue(parsed.isExpired)

        val verification = verificationEngine.verify(
            title = "Past Event 2024",
            description = "Completed event",
            sourceUrl = "https://apsc.nic.in/archive",
            sourceTier = SourceTier.TIER_1_OFFICIAL,
            category = OpportunityCategory.EVENT,
            parsedDeadline = parsed
        )
        assertEquals(VerificationStatus.EXPIRED, verification.status)
        assertTrue(verification.isExpired)
    }

    // 7. Missing Deadline
    @Test
    fun `test 7 - missing deadline is preserved without incorrectly marking expired`() {
        val parsed = normalizer.parseDeadline(null)
        assertNull(parsed.normalizedString)
        assertNull(parsed.epochMillis)
        assertFalse(parsed.isExpired)
    }

    // 8. Source Tier Assignment
    @Test
    fun `test 8 - source tier assigns Tier 1 to official government and university domains`() {
        assertEquals(SourceTier.TIER_1_OFFICIAL, normalizer.determineSourceTier("https://apsc.nic.in/page"))
        assertEquals(SourceTier.TIER_1_OFFICIAL, normalizer.determineSourceTier("https://assam.gov.in/notices"))
        assertEquals(SourceTier.TIER_1_OFFICIAL, normalizer.determineSourceTier("https://iitg.ac.in/admissions"))
        assertEquals(SourceTier.TIER_1_OFFICIAL, normalizer.determineSourceTier("https://sih.gov.in"))

        assertEquals(SourceTier.TIER_2_REPUTABLE, normalizer.determineSourceTier("https://assamtribune.com/news"))
        assertEquals(SourceTier.TIER_3_COMMUNITY, normalizer.determineSourceTier("https://random-tech-blog.info/post"))
    }

    // 9. Default Verification Status
    @Test
    fun `test 9 - reputable non-tier-1 sources default safely to NEEDS_REVIEW`() {
        val futureDeadline = ParsedDeadline(
            normalizedString = "2026-12-31",
            epochMillis = System.currentTimeMillis() + 10000000,
            isExpired = false
        )
        val result = verificationEngine.verify(
            title = "Assam Youth Entrepreneurship Meet",
            description = "Community tech meet for young startup founders.",
            sourceUrl = "https://assamtribune.com/events/102",
            sourceTier = SourceTier.TIER_2_REPUTABLE,
            category = OpportunityCategory.EVENT,
            parsedDeadline = futureDeadline
        )
        assertEquals(VerificationStatus.NEEDS_REVIEW, result.status)
        assertFalse(result.isExpired)
    }

    // 10. Verification Failure on Malformed URL or Short Title
    @Test
    fun `test 10 - verification failure rejects malformed URLs or stub titles`() {
        val futureDeadline = ParsedDeadline(normalizedString = null, epochMillis = null, isExpired = false)
        val resultMalformed = verificationEngine.verify(
            title = "Valid Title Here",
            description = "Description",
            sourceUrl = "not-a-valid-url",
            sourceTier = SourceTier.UNKNOWN,
            category = OpportunityCategory.JOB,
            parsedDeadline = futureDeadline
        )
        assertEquals(VerificationStatus.REJECTED, resultMalformed.status)

        val resultShortTitle = verificationEngine.verify(
            title = "Hi",
            description = "Description",
            sourceUrl = "https://apsc.nic.in",
            sourceTier = SourceTier.TIER_1_OFFICIAL,
            category = OpportunityCategory.JOB,
            parsedDeadline = futureDeadline
        )
        assertEquals(VerificationStatus.REJECTED, resultShortTitle.status)
    }

    // 11. Duplicate Suppression Logic
    @Test
    fun `test 11 - duplicate hash detection prevents duplicate processing`() {
        val hashSet = mutableSetOf<String>()
        val hash = normalizer.computeContentHash("SIH 2026", "sih.gov.in", "2026-10-31", "AICTE")

        assertTrue(hashSet.add(hash))
        assertFalse(hashSet.add(hash)) // Second attempt fails duplicate check
    }

    // 12. Category Filtering Verification
    @Test
    fun `test 12 - category parsing and matching functions properly`() {
        val catJob = OpportunityCategory.fromString("JOB")
        val catGov = OpportunityCategory.fromString("GOVERNMENT_JOB")
        val catUnknown = OpportunityCategory.fromString("NON_EXISTENT")

        assertEquals(OpportunityCategory.JOB, catJob)
        assertEquals(OpportunityCategory.GOVERNMENT_JOB, catGov)
        assertEquals(OpportunityCategory.OTHER, catUnknown)
    }

    // 13. Region Filtering Verification
    @Test
    fun `test 13 - region sorting and parsing maintains priority hierarchy`() {
        assertTrue(OpportunityRegion.ASSAM.sortOrder < OpportunityRegion.NORTHEAST_INDIA.sortOrder)
        assertTrue(OpportunityRegion.NORTHEAST_INDIA.sortOrder < OpportunityRegion.INDIA.sortOrder)
        assertTrue(OpportunityRegion.INDIA.sortOrder < OpportunityRegion.INTERNATIONAL.sortOrder)
    }

    // 14. Political Content Safety & Neutrality
    @Test
    fun `test 14 - partisan propaganda or viral unverified claims are strictly rejected`() {
        val deadline = ParsedDeadline(normalizedString = null, epochMillis = null, isExpired = false)
        val partisanResult = verificationEngine.verify(
            title = "Vote for our political party announcement only",
            description = "Partisan campaign message",
            sourceUrl = "https://political-blog.org/news",
            sourceTier = SourceTier.TIER_3_COMMUNITY,
            category = OpportunityCategory.NEWS,
            parsedDeadline = deadline
        )
        assertEquals(VerificationStatus.REJECTED, partisanResult.status)

        val viralRumorResult = verificationEngine.verify(
            title = "Viral secret trick to get job without exam",
            description = "Forwarded as received from anonymous WhatsApp forward",
            sourceUrl = "https://unverified-portal.xyz/secret",
            sourceTier = SourceTier.TIER_3_COMMUNITY,
            category = OpportunityCategory.JOB,
            parsedDeadline = deadline
        )
        assertEquals(VerificationStatus.REJECTED, viralRumorResult.status)
    }
}
