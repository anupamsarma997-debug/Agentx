package com.example.data.model.reel

import com.example.data.local.entity.ContentEntity
import com.example.data.local.entity.MemeDraftEntity
import com.example.data.local.entity.OpportunityEntity
import com.example.data.model.opportunity.OpportunityCategory
import com.example.data.model.opportunity.OpportunityRegion
import com.example.data.model.opportunity.VerificationStatus

enum class ReelType(val displayName: String, val shortDescription: String) {
    OPPORTUNITY_REEL("Opportunity Alert", "Verified opportunity announcement"),
    NEWS_REEL("Verified News", "Factual news update with source"),
    JOB_ALERT_REEL("Job Alert", "Recruitment and hiring alert"),
    SCHOLARSHIP_REEL("Scholarship Alert", "Student grants and fellowships"),
    GRANT_REEL("Grant Alert", "Government & institutional funding"),
    HACKATHON_REEL("Hackathon Alert", "Developer and innovation competitions"),
    STARTUP_REEL("Startup Alert", "Incubation and entrepreneurial schemes"),
    ASSAM_FACT_REEL("Assam Fact", "Cultural and civic insight from Assam"),
    TECH_REEL("Tech Update", "Technology and digital tools"),
    EDUCATION_REEL("Education Reel", "Academic resources and admissions"),
    MEME_REEL("Meme Reel", "Relatable visual humor and reaction scene"),
    GENERAL_INFORMATION_REEL("General Information", "Useful civic or informational brief");

    companion object {
        fun fromString(value: String): ReelType {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: OPPORTUNITY_REEL
        }
    }
}

enum class ReelLanguage(val displayName: String) {
    ENGLISH("English"),
    HINGLISH("Hinglish"),
    ASSAMESE("Assamese");

    companion object {
        fun fromString(value: String): ReelLanguage {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: ENGLISH
        }
    }
}

enum class ReelSafetyStatus {
    SAFE,
    NEEDS_REVIEW,
    BLOCKED;

    companion object {
        fun fromString(value: String): ReelSafetyStatus {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: SAFE
        }
    }
}

enum class ReelGenerationStatus {
    DRAFT,
    GENERATING,
    GENERATED,
    REVIEW_REQUIRED,
    APPROVED,
    REJECTED,
    FAILED,
    PUBLISHED;

    companion object {
        fun fromString(value: String): ReelGenerationStatus {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: DRAFT
        }
    }
}

data class ReelScene(
    val sceneNumber: Int,
    val durationSeconds: Int,
    val visualDescription: String,
    val onScreenText: String,
    val voiceoverText: String,
    val transition: String = "Cut",
    val backgroundSuggestion: String? = null
)

data class ReelDraft(
    val id: String,
    val sourceOpportunityId: String? = null,
    val sourceContentId: String? = null,
    val reelType: ReelType,
    val title: String,
    val hook: String,
    val durationSeconds: Int = 30,
    val scenes: List<ReelScene>,
    val voiceover: String,
    val caption: String,
    val hashtags: List<String>,
    val sourceName: String,
    val sourceUrl: String,
    val verificationStatus: String,
    val safetyStatus: ReelSafetyStatus = ReelSafetyStatus.SAFE,
    val generationStatus: ReelGenerationStatus = ReelGenerationStatus.DRAFT,
    val language: ReelLanguage = ReelLanguage.ENGLISH,
    val contentHash: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val errorMessage: String? = null
) {
    init {
        // Enforce maximum duration 60 seconds
        require(durationSeconds in 15..60) { "Reel duration must be between 15 and 60 seconds." }
    }

    val totalSceneDuration: Int
        get() = scenes.sumOf { it.durationSeconds }

    val isDurationAligned: Boolean
        get() = totalSceneDuration == durationSeconds
}

data class ReelTopic(
    val topic: String,
    val context: String,
    val category: String = OpportunityCategory.EDUCATION.name,
    val region: String = OpportunityRegion.ASSAM.name,
    val sourceName: String = "Civic Updates",
    val sourceUrl: String = "",
    val verificationStatus: String = VerificationStatus.VERIFIED.name,
    val sourceOpportunityId: String? = null,
    val sourceContentId: String? = null,
    val deadline: String? = null,
    val eligibility: String? = null,
    val isPolitical: Boolean = false
) {
    companion object {
        fun fromOpportunity(opp: OpportunityEntity): ReelTopic {
            return ReelTopic(
                topic = opp.title,
                context = opp.description,
                category = opp.category,
                region = opp.region,
                sourceName = opp.sourceName,
                sourceUrl = opp.sourceUrl,
                verificationStatus = opp.verificationStatus,
                sourceOpportunityId = opp.id,
                deadline = opp.deadline,
                eligibility = opp.eligibility
            )
        }

        fun fromContent(content: ContentEntity): ReelTopic {
            return ReelTopic(
                topic = content.title,
                context = content.body,
                category = OpportunityCategory.OTHER.name,
                region = OpportunityRegion.ASSAM.name,
                sourceName = content.sourceName,
                sourceUrl = content.sourceUrl,
                verificationStatus = content.verificationStatus,
                sourceContentId = content.id
            )
        }

        fun fromMemeDraft(meme: MemeDraftEntity): ReelTopic {
            val memeContext = "Setup: ${meme.setupText} | Punchline: ${meme.punchlineText} | Caption: ${meme.caption}"
            return ReelTopic(
                topic = meme.topic,
                context = memeContext,
                category = OpportunityCategory.OTHER.name,
                region = OpportunityRegion.ASSAM.name,
                sourceName = meme.sourceName,
                sourceUrl = meme.sourceUrl,
                verificationStatus = meme.verificationStatus,
                sourceOpportunityId = meme.sourceOpportunityId
            )
        }

        fun createGeneralTheme(
            topic: String,
            context: String,
            sourceName: String = "SocialAgent Digest",
            sourceUrl: String = "https://socialagent.local/info"
        ): ReelTopic {
            return ReelTopic(
                topic = topic,
                context = context,
                category = OpportunityCategory.ASSAM.name,
                region = OpportunityRegion.ASSAM.name,
                sourceName = sourceName,
                sourceUrl = sourceUrl,
                verificationStatus = VerificationStatus.VERIFIED.name
            )
        }
    }
}

sealed interface ReelGenerationOutcome {
    data class Success(val draft: ReelDraft) : ReelGenerationOutcome
    data class BlockedBySafety(val reasons: List<String>) : ReelGenerationOutcome
    data class QuotaExhausted(val reason: String) : ReelGenerationOutcome
    data class SourceIneligible(val reason: String) : ReelGenerationOutcome
    data class DuplicateDetected(val hash: String) : ReelGenerationOutcome
    data class ConfigurationRequired(val message: String) : ReelGenerationOutcome
    data class ValidationFailed(val error: String) : ReelGenerationOutcome
    data class Error(val message: String) : ReelGenerationOutcome
}
