package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.opportunity.OpportunityCategory
import com.example.data.model.opportunity.OpportunityRegion
import com.example.data.model.opportunity.SourceTier
import com.example.data.model.opportunity.VerificationStatus

@Entity(
    tableName = "opportunities",
    indices = [
        Index(value = ["contentHash"], unique = true),
        Index(value = ["category"]),
        Index(value = ["region"]),
        Index(value = ["verificationStatus"]),
        Index(value = ["deadlineEpochMillis"]),
        Index(value = ["discoveredAt"]),
        Index(value = ["isPosted"])
    ]
)
data class OpportunityEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val region: String,
    val sourceName: String,
    val sourceUrl: String,
    val sourceDomain: String,
    val publishedAt: String? = null,
    val deadline: String? = null,
    val deadlineEpochMillis: Long? = null,
    val eligibility: String? = null,
    val organization: String? = null,
    val sourceTier: String = SourceTier.UNKNOWN.name,
    val verificationStatus: String = VerificationStatus.NEEDS_REVIEW.name,
    val discoveredAt: Long = System.currentTimeMillis(),
    val lastCheckedAt: Long = System.currentTimeMillis(),
    val contentHash: String,
    val isExpired: Boolean = false,
    val isPosted: Boolean = false,
    val postedAt: Long? = null
) {
    val categoryEnum: OpportunityCategory
        get() = OpportunityCategory.fromString(category)

    val regionEnum: OpportunityRegion
        get() = OpportunityRegion.fromString(region)

    val verificationStatusEnum: VerificationStatus
        get() = VerificationStatus.fromString(verificationStatus)

    val sourceTierEnum: SourceTier
        get() = SourceTier.fromString(sourceTier)

    /**
     * Checks if this opportunity was discovered/published within the last 48 hours.
     */
    fun isFresh48Hours(): Boolean {
        val fortyEightHoursAgo = System.currentTimeMillis() - (48L * 60 * 60 * 1000)
        return discoveredAt >= fortyEightHoursAgo
    }

    /**
     * Checks if this is a Government (Sarkari) scheme, recruitment, or portal update.
     */
    fun isSarkariUpdate(): Boolean {
        val cat = category.uppercase()
        if (cat.contains("GOVERNMENT") || cat.contains("SCHEME") || cat.contains("JOB") ||
            cat.contains("MSME") || cat.contains("SCHOLARSHIP") || cat.contains("ASSAM")) {
            return true
        }
        val url = sourceUrl.lowercase()
        val domain = sourceDomain.lowercase()
        if (url.contains(".gov.in") || url.contains(".nic.in") || domain.contains("gov.in") || domain.contains("nic.in")) {
            return true
        }
        val org = (organization ?: "").lowercase()
        val src = sourceName.lowercase()
        val t = title.lowercase()
        return org.contains("govt") || org.contains("government") || org.contains("চৰকাৰ") ||
            src.contains("govt") || src.contains("government") || src.contains("চৰকাৰ") ||
            t.contains("আঁচনি") || t.contains("নিযুক্তি") || t.contains("চৰকাৰী") || t.contains("sarkari")
    }
}
