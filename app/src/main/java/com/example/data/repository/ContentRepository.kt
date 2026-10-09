package com.example.data.repository

import com.example.data.local.dao.ContentDao
import com.example.data.local.entity.ContentEntity
import com.example.data.local.entity.OpportunityEntity
import com.example.data.model.content.ContentLength
import com.example.data.model.content.ContentPlatform
import com.example.data.model.content.ContentType
import com.example.data.model.content.GenerationStatus
import com.example.domain.engine.ContentCreationEngine
import com.example.domain.engine.ContentCreationOutcome
import com.example.domain.generator.PostImageGenerator
import com.example.domain.generator.PostImageSize
import com.example.domain.model.SourceFact
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class ContentRepository(
    private val contentDao: ContentDao,
    private val creationEngine: ContentCreationEngine,
    private val postImageGenerator: PostImageGenerator? = null,
    private val opportunityDao: com.example.data.local.dao.OpportunityDao? = null
) {

    fun observeContentQueue(): Flow<List<ContentEntity>> = contentDao.getAllContent()

    fun getDrafts(): Flow<List<ContentEntity>> = contentDao.getContentByStatus(GenerationStatus.DRAFT.name)

    fun getReviewRequired(): Flow<List<ContentEntity>> = contentDao.getContentByStatus(GenerationStatus.REVIEW_REQUIRED.name)

    fun getGeneratedContent(): Flow<List<ContentEntity>> = contentDao.getContentByStatus(GenerationStatus.GENERATED.name)

    fun getApprovedContent(): Flow<List<ContentEntity>> = contentDao.getContentByStatus(GenerationStatus.APPROVED.name)

    fun getContentById(id: String): Flow<ContentEntity?> = contentDao.getContentById(id)

    suspend fun getContentByIdSync(id: String): ContentEntity? = contentDao.getContentByIdSync(id)

    fun countTotal(): Flow<Int> = contentDao.countTotal()

    fun countByStatus(status: GenerationStatus): Flow<Int> = contentDao.countByStatus(status.name)

    suspend fun updateContentStatus(id: String, status: GenerationStatus) {
        contentDao.updateStatus(id, status.name)
    }

    suspend fun updateContentDraft(
        id: String,
        title: String,
        body: String,
        caption: String,
        hashtags: String
    ) {
        contentDao.updateContentDraft(id, title, body, caption, hashtags)
    }

    suspend fun deleteDraft(id: String) {
        contentDao.deleteContentById(id)
    }

    suspend fun saveDraft(content: ContentEntity): Long {
        return contentDao.insertContent(content)
    }

    /**
     * Executes AI generation from an OpportunityEntity and persists the resulting draft/review item.
     * Enforces strict anti-duplication so no opportunity is posted twice.
     */
    suspend fun generateContent(
        opportunity: OpportunityEntity,
        contentType: ContentType = ContentType.OPPORTUNITY_POST,
        platform: ContentPlatform = ContentPlatform.BOTH,
        length: ContentLength = ContentLength.SHORT,
        imageSize: PostImageSize = PostImageSize.SQUARE,
        language: String = "ASSAMESE",
        forceRegenerate: Boolean = false
    ): ContentCreationOutcome {
        // Strict anti-duplication: "Joh joh opportunity ekbar update huye hai wo dusri bar nhi hona jiye"
        val alreadyHasContent = contentDao.hasContentForOpportunity(opportunity.id)
        if (!forceRegenerate && (opportunity.isPosted || alreadyHasContent)) {
            return ContentCreationOutcome.Error(
                "এই সুযোগৰ বাবে ইতিমধ্যে পোষ্ট প্ৰস্তুত কৰা হৈছে! একেটা পোষ্ট বাৰে বাৰে বনোৱা নহয়। (Post already exists for this opportunity! No duplicate allowed.)"
            )
        }

        val fact = SourceFact.fromEntity(opportunity)
        val outcome = creationEngine.generateContent(
            fact = fact,
            contentType = contentType,
            platform = platform,
            length = length,
            language = language
        )

        if (outcome is ContentCreationOutcome.Success) {
            val contentId = UUID.randomUUID().toString()
            
            // Generate all 4 social media sizes (Square 1:1, Portrait 4:5, Landscape 16:9, Story 9:16)
            var targetImagePath: String? = null
            PostImageSize.entries.forEach { sz ->
                val generatedPath = postImageGenerator?.generatePostBanner(
                    contentId = contentId,
                    title = outcome.result.title,
                    category = opportunity.category,
                    organization = opportunity.organization ?: outcome.result.sourceName,
                    deadline = opportunity.deadline,
                    sourceUrl = outcome.result.sourceUrl,
                    region = opportunity.region,
                    size = sz
                )
                if (sz == imageSize || targetImagePath == null) {
                    targetImagePath = generatedPath
                }
            }

            val entity = ContentEntity(
                id = contentId,
                sourceOpportunityId = opportunity.id,
                contentType = contentType.name,
                platform = platform.name,
                title = outcome.result.title,
                body = outcome.result.body,
                caption = outcome.result.caption,
                hashtags = outcome.result.hashtags.joinToString(", "),
                sourceUrl = outcome.result.sourceUrl,
                sourceName = outcome.result.sourceName,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                generationStatus = outcome.initialStatus.name,
                verificationStatus = opportunity.verificationStatus,
                aiModel = "gemini-2.5-flash",
                errorMessage = null,
                imageUrl = targetImagePath
            )
            contentDao.insertContent(entity)
            try {
                opportunityDao?.markAsPosted(opportunity.id)
            } catch (_: Exception) {}
        }

        return outcome
    }
}
