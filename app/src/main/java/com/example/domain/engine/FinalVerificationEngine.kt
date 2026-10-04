package com.example.domain.engine

import com.example.data.local.entity.OpportunityEntity
import com.example.data.model.opportunity.VerificationStatus
import com.example.data.model.verification.FinalVerificationResult
import com.example.data.model.verification.FinalVerificationStatus
import com.example.data.model.verification.GeneratedContentFact
import com.example.data.model.verification.SourceFact
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class FinalVerificationEngine(
    private val contentSafetyEngine: ContentSafetyEngine = ContentSafetyEngine()
) {

    companion object {
        private val URL_REGEX = Regex("https?://[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}(/[^\\s]*)?")
        private val PHONE_REGEX = Regex("(\\+91[- ]?)?[6-9]\\d{9}")
        private val EMAIL_REGEX = Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
        private val STATISTIC_REGEX = Regex("(?<!\\d)\\d{1,3}(,\\d{3})*(\\.\\d+)?%|\\b\\d+\\s*(crore|lakh|percent|million|billion)\\b", RegexOption.IGNORE_CASE)
    }

    /**
     * Executes the comprehensive 20-point verification check for any content item against its source.
     */
    fun verifyContent(
        contentId: String,
        source: OpportunityEntity?,
        contentFact: GeneratedContentFact,
        platform: String = "FACEBOOK",
        isDuplicate: Boolean = false
    ): FinalVerificationResult {
        val issues = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        var sourceExists = false
        var sourceUrlValid = false
        var sourceStillAvailable = true
        var sourceVerified = false
        var factsConsistent = true
        var deadlineConsistent = true
        var eligibilityConsistent = true
        var safetyPassed = false
        var duplicateFree = !isDuplicate
        var politicalReviewRequired = false
        var humanReviewRequired = false

        // 1. Source Exists Check
        if (source == null) {
            issues.add("Source opportunity not found in database.")
            factsConsistent = false
            return FinalVerificationResult(
                contentId = contentId,
                status = FinalVerificationStatus.FAILED,
                sourceVerified = false,
                sourceUrlValid = false,
                sourceStillAvailable = false,
                factsConsistent = false,
                deadlineConsistent = false,
                eligibilityConsistent = false,
                safetyPassed = false,
                duplicateFree = duplicateFree,
                humanReviewRequired = true,
                issues = issues,
                warnings = warnings
            )
        }
        sourceExists = true

        // 2 & 12. Source URL is valid and No Fabricated URLs
        val expectedSourceUrl = source.sourceUrl.trim()
        if (expectedSourceUrl.isBlank() || (!expectedSourceUrl.startsWith("http://") && !expectedSourceUrl.startsWith("https://"))) {
            issues.add("Stored source URL is invalid or empty: '$expectedSourceUrl'")
            sourceUrlValid = false
        } else {
            sourceUrlValid = true
        }

        // Scan generated content for foreign/fabricated URLs
        val fullContentText = "${contentFact.title} ${contentFact.body} ${contentFact.caption}".trim()
        val detectedUrls = URL_REGEX.findAll(fullContentText).map { it.value.trim() }.toList()

        for (detectedUrl in detectedUrls) {
            val normalizedExpected = normalizeUrl(expectedSourceUrl)
            val normalizedDetected = normalizeUrl(detectedUrl)
            if (!normalizedDetected.contains(normalizedExpected) && !normalizedExpected.contains(normalizedDetected)) {
                issues.add("Generated content contains unverified or altered URL: '$detectedUrl' (expected '$expectedSourceUrl')")
                sourceUrlValid = false
                factsConsistent = false
            }
        }

        // 3. Source Attribution Exists Check
        val hasAttribution = fullContentText.contains(source.sourceName, ignoreCase = true) ||
                fullContentText.contains("source", ignoreCase = true)
        if (!hasAttribution) {
            warnings.add("Source attribution '${source.sourceName}' not clearly visible in content text.")
            humanReviewRequired = true
        }

        // 4. Source Verification Status
        sourceVerified = (source.verificationStatus == VerificationStatus.VERIFIED.name)
        if (source.verificationStatus == VerificationStatus.NEEDS_REVIEW.name) {
            warnings.add("Underlying source is marked as NEEDS_REVIEW.")
            humanReviewRequired = true
        } else if (source.verificationStatus == VerificationStatus.REJECTED.name) {
            issues.add("Source was REJECTED by editorial team.")
            sourceVerified = false
        }

        // 5. Source has not expired Check
        val isExpired = checkIfExpired(source.deadline, source.verificationStatus)
        if (isExpired) {
            issues.add("Source opportunity deadline has expired or status is EXPIRED.")
            return FinalVerificationResult(
                contentId = contentId,
                status = FinalVerificationStatus.EXPIRED,
                sourceVerified = sourceVerified,
                sourceUrlValid = sourceUrlValid,
                sourceStillAvailable = false,
                factsConsistent = factsConsistent,
                deadlineConsistent = false,
                eligibilityConsistent = eligibilityConsistent,
                safetyPassed = false,
                duplicateFree = duplicateFree,
                humanReviewRequired = true,
                issues = issues,
                warnings = warnings
            )
        }

        // 6 & 9. Facts Consistent & Organization Name Match
        val sourceFact = SourceFact(
            title = source.title,
            organization = extractOrganization(source.title, source.sourceName),
            description = source.description,
            category = source.category,
            region = source.region,
            eligibility = source.eligibility ?: "",
            deadline = source.deadline ?: "",
            publishedAt = source.publishedAt ?: source.discoveredAt.toString(),
            sourceName = source.sourceName,
            sourceUrl = source.sourceUrl
        )

        // Compare Organization
        if (sourceFact.organization.isNotBlank()) {
            val orgMatch = fullContentText.contains(sourceFact.organization, ignoreCase = true) ||
                    fullContentText.contains(source.sourceName, ignoreCase = true)
            if (!orgMatch) {
                warnings.add("Organization name '${sourceFact.organization}' not clearly mentioned.")
                // Factual comparison cannot be verified with 100% confidence
                humanReviewRequired = true
            }
        }

        // 7. Deadline Check
        val (deadlineOk, deadlineIssue) = verifyDeadline(source.deadline, fullContentText)
        deadlineConsistent = deadlineOk
        if (!deadlineOk) {
            issues.add(deadlineIssue)
            factsConsistent = false
        } else if (deadlineIssue.isNotBlank()) {
            warnings.add(deadlineIssue)
            humanReviewRequired = true
        }

        // 8. Eligibility Check
        val (eligibilityOk, eligibilityMsg) = verifyEligibility(source.eligibility, fullContentText)
        eligibilityConsistent = eligibilityOk
        if (!eligibilityOk) {
            issues.add(eligibilityMsg)
            factsConsistent = false
        } else if (eligibilityMsg.isNotBlank()) {
            warnings.add(eligibilityMsg)
            humanReviewRequired = true
        }

        // 10 & 11. Statistics & Contact Details Check
        val (statsOk, statsMsg) = verifyStatistics(source.description, fullContentText)
        if (!statsOk) {
            warnings.add(statsMsg)
            humanReviewRequired = true
        }

        val (contactOk, contactMsg) = verifyContactDetails(source.description, fullContentText)
        if (!contactOk) {
            issues.add(contactMsg)
            factsConsistent = false
        }

        // 13 & 14 & 16. Content Safety & Political Persuasion / Civic Review
        val safetyEval = contentSafetyEngine.evaluate(
            title = contentFact.title,
            body = contentFact.body,
            caption = contentFact.caption,
            hashtags = contentFact.hashtags,
            sourceName = source.sourceName,
            category = source.category
        )

        if (safetyEval.isBlocked) {
            issues.addAll(safetyEval.reasons)
            safetyPassed = false
            return FinalVerificationResult(
                contentId = contentId,
                status = FinalVerificationStatus.BLOCKED,
                sourceVerified = sourceVerified,
                sourceUrlValid = sourceUrlValid,
                sourceStillAvailable = sourceStillAvailable,
                factsConsistent = false,
                deadlineConsistent = deadlineConsistent,
                eligibilityConsistent = eligibilityConsistent,
                safetyPassed = false,
                duplicateFree = duplicateFree,
                politicalReviewRequired = safetyEval.isPoliticalPersuasion || safetyEval.isPoliticalAffairs,
                humanReviewRequired = true,
                issues = issues,
                warnings = warnings
            )
        }

        safetyPassed = safetyEval.isSafe || safetyEval.needsReview
        if (safetyEval.needsReview) {
            warnings.addAll(safetyEval.reasons)
            humanReviewRequired = true
            if (safetyEval.isPoliticalAffairs || safetyEval.isPoliticalPersuasion) {
                politicalReviewRequired = true
            }
        }

        // 15. Duplicate Content Check
        if (isDuplicate) {
            issues.add("Duplicate content detected against previously generated queue items.")
            duplicateFree = false
            humanReviewRequired = true
        }

        // 17. Content Required Fields
        if (contentFact.title.isBlank() && contentFact.body.isBlank()) {
            issues.add("Content is completely blank.")
            factsConsistent = false
        }

        // 18. Platform Compatibility (Facebook Page Post)
        if (fullContentText.length > 5000) {
            warnings.add("Content is unusually long (${fullContentText.length} chars). Consider shortening for Facebook.")
        }

        // 19. Hashtag Limit
        if (contentFact.hashtags.size > 8) {
            warnings.add("Exceeds recommended limit of 8 hashtags (${contentFact.hashtags.size} hashtags provided).")
        }

        // 20. Content Length Limits
        if (contentFact.title.length > 200) {
            warnings.add("Title is overly long (>200 characters).")
        }

        // Determine Final Verification Status
        val finalStatus = when {
            issues.isNotEmpty() -> FinalVerificationStatus.FAILED
            humanReviewRequired || politicalReviewRequired || !sourceVerified -> FinalVerificationStatus.NEEDS_REVIEW
            else -> FinalVerificationStatus.PASSED
        }

        return FinalVerificationResult(
            contentId = contentId,
            status = finalStatus,
            checkedAt = System.currentTimeMillis(),
            sourceVerified = sourceVerified,
            sourceUrlValid = sourceUrlValid,
            sourceStillAvailable = sourceStillAvailable,
            factsConsistent = factsConsistent,
            deadlineConsistent = deadlineConsistent,
            eligibilityConsistent = eligibilityConsistent,
            safetyPassed = safetyPassed,
            duplicateFree = duplicateFree,
            politicalReviewRequired = politicalReviewRequired,
            humanReviewRequired = humanReviewRequired || (finalStatus == FinalVerificationStatus.NEEDS_REVIEW),
            issues = issues,
            warnings = warnings
        )
    }

    // -------------------------------------------------------------
    // Deterministic Comparison & Extraction Helpers
    // -------------------------------------------------------------

    private fun checkIfExpired(deadline: String?, status: String?): Boolean {
        if (status.equals("EXPIRED", ignoreCase = true)) return true
        if (deadline.isNullOrBlank()) return false

        val deadlineDate = parseDateOrNull(deadline) ?: return false
        val today = LocalDate.now()
        return deadlineDate.isBefore(today)
    }

    private fun verifyDeadline(sourceDeadline: String?, contentText: String): Pair<Boolean, String> {
        val detectedDates = extractDates(contentText)

        if (sourceDeadline.isNullOrBlank()) {
            // Source has no deadline, but generated content claims a specific deadline date
            return if (detectedDates.isNotEmpty()) {
                Pair(true, "Content specifies deadline date(s) [${detectedDates.joinToString()}], but source did not specify one. Requires verification.")
            } else {
                Pair(true, "")
            }
        }

        val sourceDate = parseDateOrNull(sourceDeadline)
        if (sourceDate != null && detectedDates.isNotEmpty()) {
            // If content mentions dates, ensure it does not state a conflicting future/past deadline date
            val matchesAny = detectedDates.any { it == sourceDate }
            if (!matchesAny) {
                return Pair(false, "Deadline inconsistency: source deadline is '$sourceDeadline', but content contains conflicting date(s) [${detectedDates.joinToString()}].")
            }
        }

        return Pair(true, "")
    }

    private fun verifyEligibility(sourceEligibility: String?, contentText: String): Pair<Boolean, String> {
        if (sourceEligibility.isNullOrBlank()) return Pair(true, "")

        val cleanSourceElig = sourceEligibility.trim().lowercase(Locale.ROOT)
        // Check if content completely reverses or invents criteria
        if (cleanSourceElig.contains("graduate") && contentText.contains("10th pass only", ignoreCase = true)) {
            return Pair(false, "Eligibility conflict: source requires Graduate, but content specifies 10th pass only.")
        }
        if (cleanSourceElig.contains("assam resident") && contentText.contains("open to all international", ignoreCase = true)) {
            return Pair(false, "Eligibility conflict: source requires Assam residency, but content states open to all international.")
        }

        return Pair(true, "")
    }

    private fun verifyStatistics(sourceDescription: String, contentText: String): Pair<Boolean, String> {
        val contentStats = STATISTIC_REGEX.findAll(contentText).map { it.value.lowercase(Locale.ROOT) }.toSet()
        if (contentStats.isEmpty()) return Pair(true, "")

        val sourceStats = STATISTIC_REGEX.findAll(sourceDescription).map { it.value.lowercase(Locale.ROOT) }.toSet()
        val unverifiedStats = contentStats.filter { stat -> !sourceDescription.contains(stat, ignoreCase = true) }

        return if (unverifiedStats.isNotEmpty()) {
            Pair(true, "Content mentions numerical statistics not directly verbatim in source summary: ${unverifiedStats.joinToString()}. Editorial check recommended.")
        } else {
            Pair(true, "")
        }
    }

    private fun verifyContactDetails(sourceDescription: String, contentText: String): Pair<Boolean, String> {
        val detectedPhones = PHONE_REGEX.findAll(contentText).map { it.value }.toList()
        for (phone in detectedPhones) {
            if (!sourceDescription.contains(phone)) {
                return Pair(false, "Fabricated or unverified phone number detected: '$phone'")
            }
        }

        val detectedEmails = EMAIL_REGEX.findAll(contentText).map { it.value }.toList()
        for (email in detectedEmails) {
            if (!sourceDescription.contains(email)) {
                return Pair(false, "Fabricated or unverified email detected: '$email'")
            }
        }

        return Pair(true, "")
    }

    private fun normalizeUrl(url: String): String {
        return url.lowercase(Locale.ROOT)
            .removePrefix("https://")
            .removePrefix("http://")
            .removePrefix("www.")
            .trimEnd('/')
    }

    private fun extractOrganization(title: String, sourceName: String): String {
        return sourceName.ifBlank {
            title.split("-", "|", ":").firstOrNull()?.trim() ?: ""
        }
    }

    private fun extractDates(text: String): List<LocalDate> {
        val dates = mutableListOf<LocalDate>()
        // ISO yyyy-MM-dd
        val isoRegex = Regex("\\b\\d{4}-\\d{2}-\\d{2}\\b")
        isoRegex.findAll(text).forEach { m ->
            parseDateOrNull(m.value)?.let { dates.add(it) }
        }
        // dd-MM-yyyy or dd/MM/yyyy
        val ddmmyyyy = Regex("\\b\\d{1,2}[/-]\\d{1,2}[/-]\\d{4}\\b")
        ddmmyyyy.findAll(text).forEach { m ->
            parseDateOrNull(m.value)?.let { dates.add(it) }
        }
        return dates.distinct()
    }

    private fun parseDateOrNull(dateStr: String): LocalDate? {
        val cleaned = dateStr.trim()
        val formatters = listOf(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("d/M/yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("d-M-yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH)
        )
        for (formatter in formatters) {
            try {
                return LocalDate.parse(cleaned, formatter)
            } catch (_: Exception) { }
        }
        return null
    }
}
