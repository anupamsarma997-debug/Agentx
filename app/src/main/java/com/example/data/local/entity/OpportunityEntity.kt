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
        Index(value = ["discoveredAt"])
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
    val isExpired: Boolean = false
) {
    val categoryEnum: OpportunityCategory
        get() = OpportunityCategory.fromString(category)

    val regionEnum: OpportunityRegion
        get() = OpportunityRegion.fromString(region)

    val verificationStatusEnum: VerificationStatus
        get() = VerificationStatus.fromString(verificationStatus)

    val sourceTierEnum: SourceTier
        get() = SourceTier.fromString(sourceTier)
}
