package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.meme.MemeDraft
import com.example.data.model.meme.MemeFormat
import com.example.data.model.meme.MemeGenerationStatus
import com.example.data.model.meme.MemeSafetyStatus

@Entity(
    tableName = "meme_drafts",
    indices = [
        Index(value = ["contentHash"], unique = false),
        Index(value = ["generationStatus"]),
        Index(value = ["safetyStatus"]),
        Index(value = ["createdAt"])
    ]
)
data class MemeDraftEntity(
    @PrimaryKey
    val id: String,
    val sourceOpportunityId: String?,
    val topic: String,
    val memeFormat: String,
    val setupText: String,
    val punchlineText: String,
    val caption: String,
    val hashtags: String,
    val sourceUrl: String,
    val sourceName: String,
    val verificationStatus: String,
    val safetyStatus: String,
    val generationStatus: String,
    val contentHash: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val errorMessage: String? = null
) {
    val memeFormatEnum: MemeFormat
        get() = MemeFormat.fromString(memeFormat)

    val safetyStatusEnum: MemeSafetyStatus
        get() = MemeSafetyStatus.fromString(safetyStatus)

    val generationStatusEnum: MemeGenerationStatus
        get() = MemeGenerationStatus.fromString(generationStatus)

    fun getHashtagList(): List<String> {
        return hashtags.split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }

    fun toDraft(): MemeDraft {
        return MemeDraft(
            id = id,
            sourceOpportunityId = sourceOpportunityId,
            topic = topic,
            memeFormat = memeFormatEnum,
            setupText = setupText,
            punchlineText = punchlineText,
            caption = caption,
            hashtags = getHashtagList(),
            sourceUrl = sourceUrl,
            sourceName = sourceName,
            verificationStatus = verificationStatus,
            safetyStatus = safetyStatusEnum,
            generationStatus = generationStatusEnum,
            contentHash = contentHash,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromDraft(draft: MemeDraft): MemeDraftEntity {
            return MemeDraftEntity(
                id = draft.id,
                sourceOpportunityId = draft.sourceOpportunityId,
                topic = draft.topic,
                memeFormat = draft.memeFormat.name,
                setupText = draft.setupText,
                punchlineText = draft.punchlineText,
                caption = draft.caption,
                hashtags = draft.hashtags.joinToString(", "),
                sourceUrl = draft.sourceUrl,
                sourceName = draft.sourceName,
                verificationStatus = draft.verificationStatus,
                safetyStatus = draft.safetyStatus.name,
                generationStatus = draft.generationStatus.name,
                contentHash = draft.contentHash,
                createdAt = draft.createdAt,
                updatedAt = draft.updatedAt
            )
        }
    }
}
