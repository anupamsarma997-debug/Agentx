package com.example.domain.model

import com.example.data.local.entity.OpportunityEntity
import com.example.data.model.content.ContentPlatform
import com.example.data.model.content.ContentType

data class SourceFact(
    val title: String,
    val organization: String?,
    val description: String,
    val category: String,
    val region: String,
    val eligibility: String?,
    val deadline: String?,
    val publishedAt: String?,
    val sourceName: String,
    val sourceUrl: String,
    val verificationStatus: String
) {
    companion object {
        fun fromEntity(entity: OpportunityEntity): SourceFact {
            return SourceFact(
                title = entity.title,
                organization = entity.organization,
                description = entity.description,
                category = entity.category,
                region = entity.region,
                eligibility = entity.eligibility,
                deadline = entity.deadline,
                publishedAt = entity.publishedAt,
                sourceName = entity.sourceName,
                sourceUrl = entity.sourceUrl,
                verificationStatus = entity.verificationStatus
            )
        }
    }
}

data class GeneratedContentResult(
    val title: String,
    val body: String,
    val caption: String,
    val hashtags: List<String>,
    val sourceUrl: String,
    val sourceName: String,
    val contentType: ContentType,
    val platform: ContentPlatform,
    val confidence: String = "HIGH",
    val needsReview: Boolean = false,
    val rawJson: String? = null
)
