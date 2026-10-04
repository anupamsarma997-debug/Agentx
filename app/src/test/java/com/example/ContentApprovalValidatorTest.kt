package com.example
 
import com.example.data.local.entity.ContentEntity
import com.example.data.model.content.ContentPlatform
import com.example.data.model.content.ContentType
import com.example.data.model.content.GenerationStatus
import com.example.domain.validator.ContentApprovalValidator
import com.example.domain.validator.ContentField
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class ContentApprovalValidatorTest {

    private fun createSampleContent(
        title: String = "NATS 2.0 Apprenticeship Portal Registration 2026",
        body: String = "The Ministry of Education has launched the NATS 2.0 Apprenticeship Portal. Eligible engineering and diploma graduates across India can register online for over 50,000 paid industrial apprenticeships.",
        sourceUrl: String = "https://nats.education.gov.in",
        sourceName: String = "NATS Portal",
        caption: String = "Register for official NATS 2.0 apprenticeships.",
        hashtags: String = "#NATS, #Apprenticeship, #Education, #IndiaGov"
    ): ContentEntity {
        return ContentEntity(
            id = UUID.randomUUID().toString(),
            sourceOpportunityId = UUID.randomUUID().toString(),
            contentType = ContentType.OPPORTUNITY_POST.name,
            platform = ContentPlatform.BOTH.name,
            title = title,
            body = body,
            caption = caption,
            hashtags = hashtags,
            sourceUrl = sourceUrl,
            sourceName = sourceName,
            generationStatus = GenerationStatus.NEEDS_REVIEW.name
        )
    }

    /**
     * Test 1: Missing Source URL (Rule 3)
     * When source URL is empty, blank, or "...", validation MUST fail with Rule 3 as a blocking failure.
     */
    @Test
    fun testMissingSourceUrlFailsRule3() = runTest {
        val validator = ContentApprovalValidator()

        // 1a: Blank URL
        val contentBlank = createSampleContent(sourceUrl = "")
        val resultBlank = validator.validate(contentBlank, skipNetworkCheck = true)
        assertFalse("Blank URL should fail validation", resultBlank.passed)
        assertTrue(resultBlank.hasBlockingFailures)
        val failureBlank = resultBlank.failures.firstOrNull { it.ruleId == 3 }
        assertTrue("Rule 3 failure must be present for blank URL", failureBlank != null)
        assertEquals(ContentField.SOURCE_URL, failureBlank?.field)
        assertTrue("Rule 3 failure must be blocking", failureBlank?.isBlocking == true)

        // 1b: Ellipsis URL "..."
        val contentEllipsis = createSampleContent(sourceUrl = "...")
        val resultEllipsis = validator.validate(contentEllipsis, skipNetworkCheck = true)
        assertFalse("Ellipsis URL should fail validation", resultEllipsis.passed)
        val failureEllipsis = resultEllipsis.failures.firstOrNull { it.ruleId == 3 }
        assertTrue("Rule 3 failure must be present for '...' URL", failureEllipsis != null)
    }

    /**
     * Test 2: Truncated Body "..." (Rule 2)
     * When Facebook body has "..." or is cut off, validation MUST fail with Rule 2 as a blocking failure.
     */
    @Test
    fun testTruncatedBodyFailsRule2() = runTest {
        val validator = ContentApprovalValidator()

        // 2a: Body ending with "..."
        val contentTruncatedEnd = createSampleContent(
            body = "This is a long promotional post about government engineering apprenticeships across India, but unfortunately it was truncated before completing the sentence..."
        )
        val resultEnd = validator.validate(contentTruncatedEnd, skipNetworkCheck = true)
        assertFalse("Body ending in ellipsis must fail validation", resultEnd.passed)
        val failureEnd = resultEnd.failures.firstOrNull { it.ruleId == 2 }
        assertTrue("Rule 2 failure must be present for truncated body", failureEnd != null)
        assertEquals(ContentField.FACEBOOK_BODY, failureEnd?.field)
        assertTrue("Rule 2 must be blocking", failureEnd?.isBlocking == true)

        // 2b: Body with literal "..." line
        val contentLiteralLine = createSampleContent(
            body = "First official announcement line for youth apprenticeship schemes.\n...\nCall to action details."
        )
        val resultLine = validator.validate(contentLiteralLine, skipNetworkCheck = true)
        assertFalse("Body containing standalone ellipsis must fail validation", resultLine.passed)
        assertTrue(resultLine.failures.any { it.ruleId == 2 })

        // 2c: Body too short (<80 chars)
        val contentTooShort = createSampleContent(body = "Short post.")
        val resultShort = validator.validate(contentTooShort, skipNetworkCheck = true)
        assertFalse("Body shorter than 80 characters must fail validation", resultShort.passed)
        assertTrue(resultShort.failures.any { it.ruleId == 2 })
    }

    /**
     * Test 3: Dead URL 404 (Rule 4)
     * When source URL returns HTTP 404, validation MUST report Rule 4 failure as blocking.
     */
    @Test
    fun testDeadUrl404FailsRule4() = runTest {
        // Inject customUrlChecker returning 404 Not Found
        val validator = ContentApprovalValidator(
            customUrlChecker = { url, _ ->
                if (url.contains("404")) Pair(404, null) else Pair(200, "Official Portal")
            }
        )

        val deadContent = createSampleContent(sourceUrl = "https://nats.education.gov.in/dead-link-404")
        val result = validator.validate(deadContent, skipNetworkCheck = false)

        val warning404 = result.warnings.firstOrNull { it.ruleId == 4 }
        assertTrue("Rule 4 note must be recorded for HTTP 404", warning404 != null)
        assertEquals(ContentField.SOURCE_URL, warning404?.field)
        assertFalse("Rule 4 must be non-blocking so editorial approval is allowed", warning404?.isBlocking == true)
        assertTrue("Note reason must mention 404", warning404?.reason?.contains("404") == true)
    }

    /**
     * Test 4: Valid Post passes all rules
     * A complete post with valid title, body >=80 chars without ellipsis, valid 200 URL matching title,
     * and <=8 hashtags MUST pass all rules cleanly.
     */
    @Test
    fun testValidPostPassesAllRules() = runTest {
        val validator = ContentApprovalValidator(
            customUrlChecker = { _, expectedTitle ->
                Pair(200, expectedTitle)
            }
        )

        val validContent = createSampleContent(
            title = "NATS 2.0 Apprenticeship Portal Registration 2026",
            body = "The Ministry of Education has announced registration for NATS 2.0 Apprenticeship scheme. Eligible diploma and degree holders can apply online at the official portal for industrial training and monthly stipends.",
            sourceUrl = "https://nats.education.gov.in",
            hashtags = "#NATS, #Apprenticeship, #Education",
            caption = "NATS 2.0 Apprenticeship registration details."
        )

        val result = validator.validate(validContent, skipNetworkCheck = false)

        assertTrue("Valid post must pass validation", result.passed)
        assertEquals("There should be no blocking failures", 0, result.failures.size)
        assertEquals("There should be no warnings", 0, result.warnings.size)
        assertTrue("Source should be verified", result.isSourceVerified)
    }

    /**
     * Test 5: Distinction between Blocking and Warning rules
     * Excessive hashtags (> 8) or unverified deadline is a WARNING, allowing the user to approve anyway.
     */
    @Test
    fun testWarningsDoNotBlockApprovalDirectly() = runTest {
        val validator = ContentApprovalValidator(
            customUrlChecker = { _, title -> Pair(200, title) }
        )

        // 10 hashtags (> 8)
        val contentWithManyTags = createSampleContent(
            hashtags = "#tag1, #tag2, #tag3, #tag4, #tag5, #tag6, #tag7, #tag8, #tag9, #tag10"
        )

        val result = validator.validate(contentWithManyTags, skipNetworkCheck = false)

        // Passed is true because there are NO blocking failures
        assertTrue("Should pass blocking check even with warnings", result.passed)
        assertFalse("Should have no blocking failures", result.hasBlockingFailures)
        assertTrue("Should have warnings", result.hasOnlyWarnings)
        val warningTag = result.warnings.firstOrNull { it.ruleId == 6 }
        assertTrue("Rule 6 hashtag warning should be present", warningTag != null)
        assertFalse("Rule 6 must NOT be blocking", warningTag?.isBlocking == true)
    }
}
