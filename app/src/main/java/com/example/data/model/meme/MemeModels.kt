package com.example.data.model.meme

import com.example.data.local.entity.OpportunityEntity
import com.example.data.model.opportunity.OpportunityCategory
import com.example.data.model.opportunity.OpportunityRegion
import com.example.data.model.opportunity.VerificationStatus

enum class MemeFormat(val displayName: String, val description: String) {
    TEXT_MEME("Text Meme", "Relatable text-only observation or quote"),
    TOP_BOTTOM("Top / Bottom", "Classic setup at the top, punchline at the bottom"),
    EXPECTATION_REALITY("Expectation vs Reality", "Contrasting optimistic expectations with real outcomes"),
    BEFORE_AFTER("Before & After", "Humorous transformation or contrast"),
    TWO_PANEL("Two Panel", "Situation setup followed by reaction"),
    CHAT_STYLE("Chat Style", "Simulated messaging / dialogue observation"),
    LIST_STYLE("List Style", "Bullet-point satirical or relatable breakdown"),
    REACTION_STYLE("Reaction Style", "Describing a scenario and an unexpected reaction"),
    ASSAM_RELATABLE("Assam Relatable", "Wholesome, hyper-relatable everyday Assam & Northeast life"),
    JOB_RELATABLE("Job Relatable", "Job hunting, resumes, interview struggles, and work life"),
    STUDENT_RELATABLE("Student Relatable", "Exams, college hostels, viva, assignments, and results"),
    STARTUP_RELATABLE("Startup Relatable", "Founders, pitch decks, coffee, and bootstrapping humor"),
    NEWSBOY_CREATOR_STYLE("NewsBoy & Neon Man Style", "Fast creator updates, YouTube community buzz, and witty creator commentary"),
    SARKARI_SCHEME_RELATABLE("Sarkari Scheme Relatable", "Middle class & student reactions to Bharat Sarkar & MSME opportunities");

    companion object {
        fun fromString(value: String): MemeFormat {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: TEXT_MEME
        }
    }
}

enum class MemeSafetyStatus(val displayName: String) {
    SAFE("Safe"),
    NEEDS_REVIEW("Needs Review"),
    BLOCKED("Blocked");

    companion object {
        fun fromString(value: String): MemeSafetyStatus {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: NEEDS_REVIEW
        }
    }
}

enum class MemeGenerationStatus(val displayName: String) {
    DRAFT("Draft"),
    REVIEW_REQUIRED("Review Required"),
    APPROVED("Approved"),
    PUBLISHED("Published"),
    REJECTED("Rejected"),
    FAILED("Failed");

    companion object {
        fun fromString(value: String): MemeGenerationStatus {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: DRAFT
        }
    }
}

data class MemeTopic(
    val topic: String,
    val context: String,
    val category: String,
    val region: String,
    val sourceName: String,
    val sourceUrl: String,
    val verificationStatus: String,
    val sourceOpportunityId: String? = null
) {
    companion object {
        fun fromOpportunity(opp: OpportunityEntity): MemeTopic {
            return MemeTopic(
                topic = opp.title,
                context = opp.description,
                category = opp.category,
                region = opp.region,
                sourceName = opp.sourceName,
                sourceUrl = opp.sourceUrl,
                verificationStatus = opp.verificationStatus,
                sourceOpportunityId = opp.id
            )
        }

        fun createAssamTheme(themeTitle: String, description: String): MemeTopic {
            return MemeTopic(
                topic = themeTitle,
                context = description,
                category = OpportunityCategory.EDUCATION.name,
                region = OpportunityRegion.ASSAM.name,
                sourceName = "Assam Cultural & Civic Observations",
                sourceUrl = "https://assam.gov.in",
                verificationStatus = VerificationStatus.VERIFIED.name,
                sourceOpportunityId = null
            )
        }

        fun createNortheastTheme(themeTitle: String, description: String): MemeTopic {
            return MemeTopic(
                topic = themeTitle,
                context = description,
                category = OpportunityCategory.EDUCATION.name,
                region = OpportunityRegion.NORTHEAST_INDIA.name,
                sourceName = "Northeast Civic & Youth Observations",
                sourceUrl = "https://necouncil.gov.in",
                verificationStatus = VerificationStatus.VERIFIED.name,
                sourceOpportunityId = null
            )
        }

        fun createGeneralTheme(themeTitle: String, description: String): MemeTopic {
            return MemeTopic(
                topic = themeTitle,
                context = description,
                category = OpportunityCategory.TECHNOLOGY.name,
                region = OpportunityRegion.INDIA.name,
                sourceName = "Public General Observations",
                sourceUrl = "https://india.gov.in",
                verificationStatus = VerificationStatus.VERIFIED.name,
                sourceOpportunityId = null
            )
        }

        fun createCreatorNewsTheme(themeTitle: String, description: String): MemeTopic {
            return MemeTopic(
                topic = themeTitle,
                context = description,
                category = OpportunityCategory.NEWS.name,
                region = OpportunityRegion.INDIA.name,
                sourceName = "NewsBoy & Neon Man Creator Updates",
                sourceUrl = "https://youtube.com",
                verificationStatus = VerificationStatus.VERIFIED.name,
                sourceOpportunityId = null
            )
        }

        fun createSarkariSchemeTheme(themeTitle: String, description: String): MemeTopic {
            return MemeTopic(
                topic = themeTitle,
                context = description,
                category = OpportunityCategory.BUSINESS.name,
                region = OpportunityRegion.INDIA.name,
                sourceName = "Ministry of MSME & Bharat Sarkar",
                sourceUrl = "https://udyamregistration.gov.in",
                verificationStatus = VerificationStatus.VERIFIED.name,
                sourceOpportunityId = null
            )
        }
    }
}

data class MemeDraft(
    val id: String,
    val sourceOpportunityId: String?,
    val topic: String,
    val memeFormat: MemeFormat,
    val setupText: String,
    val punchlineText: String,
    val caption: String,
    val hashtags: List<String>,
    val sourceUrl: String,
    val sourceName: String,
    val verificationStatus: String,
    val safetyStatus: MemeSafetyStatus,
    val generationStatus: MemeGenerationStatus,
    val contentHash: String,
    val createdAt: Long,
    val updatedAt: Long,
    val rawJson: String? = null
)
