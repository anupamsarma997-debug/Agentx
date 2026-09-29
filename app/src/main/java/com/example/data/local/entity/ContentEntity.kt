package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.content.ContentPlatform
import com.example.data.model.content.ContentType
import com.example.data.model.content.GenerationStatus
import com.example.data.model.opportunity.VerificationStatus

@Entity(
    tableName = "content_items",
    indices = [
        Index(value = ["sourceOpportunityId"]),
        Index(value = ["generationStatus"]),
        Index(value = ["contentType"]),
        Index(value = ["createdAt"])
    ]
)
data class ContentEntity(
    @PrimaryKey
    val id: String,
    val sourceOpportunityId: String,
    val contentType: String,
    val platform: String,
    val title: String,
    val body: String,
    val caption: String,
    val hashtags: String, // comma-separated or JSON list of hashtags
    val sourceUrl: String,
    val sourceName: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val generationStatus: String = GenerationStatus.DRAFT.name,
    val verificationStatus: String = VerificationStatus.NEEDS_REVIEW.name,
    val aiModel: String = "gemini-2.5-flash",
    val errorMessage: String? = null
) {
    val contentTypeEnum: ContentType
        get() = ContentType.fromString(contentType)

    val platformEnum: ContentPlatform
        get() = ContentPlatform.fromString(platform)

    val generationStatusEnum: GenerationStatus
        get() = GenerationStatus.fromString(generationStatus)

    val verificationStatusEnum: VerificationStatus
        get() = VerificationStatus.fromString(verificationStatus)

    fun getHashtagList(): List<String> {
        return hashtags.split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }
}
