package com.example.domain.engine

import com.example.data.model.opportunity.OpportunityCategory
import com.example.data.model.opportunity.SourceTier
import com.example.data.model.opportunity.VerificationStatus
import java.net.URI

data class VerificationResult(
    val status: VerificationStatus,
    val isExpired: Boolean,
    val notes: String
)

class VerificationEngine {

    companion object {
        private val UNVERIFIABLE_PATTERNS = listOf(
            "forwarded as received",
            "anonymous source claims",
            "whatsapp forward",
            "rumor has it",
            "shocking truth leaked",
            "viral secret trick to get job without exam"
        )

        private val PARTISAN_PROPAGANDA_FLAGS = listOf(
            "vote for",
            "destroy the opposition",
            "political party announcement only",
            "secret conspiracy exposed"
        )
    }

    fun verify(
        title: String,
        description: String,
        sourceUrl: String,
        sourceTier: SourceTier,
        category: OpportunityCategory,
        parsedDeadline: ParsedDeadline
    ): VerificationResult {
        // 1. Basic URL Syntax Check
        if (sourceUrl.isBlank() || (!sourceUrl.startsWith("http://") && !sourceUrl.startsWith("https://"))) {
            return VerificationResult(
                status = VerificationStatus.REJECTED,
                isExpired = false,
                notes = "Invalid or missing source URL."
            )
        }

        try {
            val uri = URI(sourceUrl)
            if (uri.host.isNullOrBlank()) {
                return VerificationResult(
                    status = VerificationStatus.REJECTED,
                    isExpired = false,
                    notes = "Malformed URL host."
                )
            }
        } catch (_: Exception) {
            return VerificationResult(
                status = VerificationStatus.REJECTED,
                isExpired = false,
                notes = "Malformed URL syntax."
            )
        }

        // 2. Title & Content Coherence
        val normTitle = OpportunityNormalizer.normalizeTitle(title)
        if (normTitle.length < 5) {
            return VerificationResult(
                status = VerificationStatus.REJECTED,
                isExpired = false,
                notes = "Title too short or incoherent."
            )
        }

        val combinedContent = "$normTitle ${OpportunityNormalizer.normalizeText(description)}".lowercase()

        // 3. Sensationalist / Anonymous Claim Check
        if (UNVERIFIABLE_PATTERNS.any { combinedContent.contains(it) }) {
            return VerificationResult(
                status = VerificationStatus.REJECTED,
                isExpired = false,
                notes = "Rejected: Contains anonymous or viral rumor markers."
            )
        }

        // 4. Partisan propaganda filter
        if (PARTISAN_PROPAGANDA_FLAGS.any { combinedContent.contains(it) }) {
            return VerificationResult(
                status = VerificationStatus.REJECTED,
                isExpired = false,
                notes = "Rejected: Partisan political propaganda violates factual neutrality rule."
            )
        }

        // 5. Deadline Check
        if (parsedDeadline.isExpired) {
            return VerificationResult(
                status = VerificationStatus.EXPIRED,
                isExpired = true,
                notes = "Application deadline has already passed (${parsedDeadline.normalizedString})."
            )
        }

        // 6. Conservative Verification Decision
        // Tier 1 official sources with valid source URL and intact metadata qualify for VERIFIED
        if (sourceTier == SourceTier.TIER_1_OFFICIAL && normTitle.isNotBlank()) {
            return VerificationResult(
                status = VerificationStatus.VERIFIED,
                isExpired = false,
                notes = "Verified against Tier 1 official entity / government portal."
            )
        }

        // Tier 2 or Tier 3 items: Conservative default to NEEDS_REVIEW
        return VerificationResult(
            status = VerificationStatus.NEEDS_REVIEW,
            isExpired = false,
            notes = "Source is reputable/public; manual review recommended before broadcast."
        )
    }
}
