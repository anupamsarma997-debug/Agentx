package com.example.domain.engine

import com.example.data.local.dao.MemeDao
import com.example.data.model.meme.MemeDraft
import com.example.data.model.meme.MemeFormat
import com.example.data.model.meme.MemeGenerationStatus
import com.example.data.model.meme.MemeSafetyStatus
import com.example.data.model.meme.MemeTopic
import com.example.data.model.opportunity.VerificationStatus
import com.example.data.remote.ai.AIResult
import com.example.data.remote.ai.GeminiClient
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

sealed interface MemeGenerationOutcome {
    data class Success(val draft: MemeDraft) : MemeGenerationOutcome
    data class BlockedBySafety(val reasons: List<String>) : MemeGenerationOutcome
    data class DuplicateDetected(val existingHash: String, val message: String) : MemeGenerationOutcome
    data class SourceIneligible(val reason: String) : MemeGenerationOutcome
    data class ConfigurationRequired(val message: String) : MemeGenerationOutcome
    data class Error(val message: String) : MemeGenerationOutcome
}

class MemeEngine(
    private val geminiClient: GeminiClient = GeminiClient(),
    private val safetyEngine: MemeSafetyEngine = MemeSafetyEngine(),
    private val memeDao: MemeDao? = null
) {

    companion object {
        const val MAX_SETUP_CHARS = 120
        const val MAX_PUNCHLINE_CHARS = 160
        const val MAX_CAPTION_CHARS = 500
        const val MAX_HASHTAGS = 8
    }

    /**
     * Checks whether the underlying source topic is eligible for meme generation.
     */
    fun checkSourceEligibility(statusStr: String): Result<Unit> {
        val status = VerificationStatus.fromString(statusStr)
        return when (status) {
            VerificationStatus.EXPIRED -> Result.failure(
                IllegalArgumentException("Cannot generate meme for an expired source opportunity.")
            )
            VerificationStatus.REJECTED -> Result.failure(
                IllegalArgumentException("Cannot generate meme for a rejected source opportunity.")
            )
            VerificationStatus.VERIFIED,
            VerificationStatus.NEEDS_REVIEW -> Result.success(Unit)
        }
    }

    /**
     * Generates a meme concept from a structured MemeTopic.
     */
    suspend fun generateMeme(
        topic: MemeTopic,
        format: MemeFormat = MemeFormat.TEXT_MEME
    ): MemeGenerationOutcome {
        // 1. Source verification gate
        val eligibility = checkSourceEligibility(topic.verificationStatus)
        if (eligibility.isFailure) {
            return MemeGenerationOutcome.SourceIneligible(
                eligibility.exceptionOrNull()?.message ?: "Source is ineligible"
            )
        }

        // 2. Build system and user prompts
        val systemPrompt = MemePromptBuilder.buildSystemPrompt(format)
        val userPrompt = MemePromptBuilder.buildUserPrompt(topic, format)

        // 3. Call AI Client
        val aiResult = geminiClient.generateContent(systemPrompt, userPrompt)

        return when (aiResult) {
            is AIResult.ConfigurationRequired -> {
                MemeGenerationOutcome.ConfigurationRequired(aiResult.message)
            }
            is AIResult.Error -> {
                MemeGenerationOutcome.Error(aiResult.message)
            }
            is AIResult.Success -> {
                parseAndValidateMeme(
                    rawJson = aiResult.jsonText,
                    topic = topic,
                    expectedFormat = format
                )
            }
        }
    }

    /**
     * Parses AI response, validates text limits, enforces source URL fidelity,
     * checks duplicate hash, and evaluates content safety.
     */
    suspend fun parseAndValidateMeme(
        rawJson: String,
        topic: MemeTopic,
        expectedFormat: MemeFormat
    ): MemeGenerationOutcome {
        val parsed = parseRawJson(rawJson, expectedFormat)
        if (parsed.isFailure) {
            return MemeGenerationOutcome.Error(
                "Meme parsing failed: ${parsed.exceptionOrNull()?.message ?: "Invalid JSON"}"
            )
        }

        val rawMeme = parsed.getOrThrow()

        // 1. Validate text length limits
        val setupText = if (rawMeme.setupText.length > MAX_SETUP_CHARS) {
            rawMeme.setupText.take(MAX_SETUP_CHARS).trim()
        } else {
            rawMeme.setupText
        }

        val punchlineText = if (rawMeme.punchlineText.length > MAX_PUNCHLINE_CHARS) {
            rawMeme.punchlineText.take(MAX_PUNCHLINE_CHARS).trim()
        } else {
            rawMeme.punchlineText
        }

        val caption = if (rawMeme.caption.length > MAX_CAPTION_CHARS) {
            rawMeme.caption.take(MAX_CAPTION_CHARS).trim()
        } else {
            rawMeme.caption
        }

        val hashtags = rawMeme.hashtags.take(MAX_HASHTAGS)

        // 2. Original stored source URL and source name MUST be preserved
        val sourceUrl = topic.sourceUrl
        val sourceName = topic.sourceName

        // 3. Duplicate check via content hash
        val contentHash = MemeDuplicateDetector.computeHash(
            topic = topic.topic,
            setupText = setupText,
            punchlineText = punchlineText
        )

        if (memeDao != null) {
            val existing = memeDao.getMemeByContentHash(contentHash)
            if (existing != null) {
                return MemeGenerationOutcome.DuplicateDetected(
                    existingHash = contentHash,
                    message = "A meme with the exact same topic, setup, and punchline already exists."
                )
            }
        }

        // 4. Run MemeSafetyEngine
        val safetyEvaluation = safetyEngine.evaluate(
            topic = topic.topic,
            setupText = setupText,
            punchlineText = punchlineText,
            caption = caption
        )

        if (safetyEvaluation.isBlocked) {
            return MemeGenerationOutcome.BlockedBySafety(safetyEvaluation.reasons)
        }

        // 5. Determine generation status based on safety and source verification
        val initialGenerationStatus = when {
            safetyEvaluation.needsReview -> MemeGenerationStatus.REVIEW_REQUIRED
            topic.verificationStatus == VerificationStatus.NEEDS_REVIEW.name -> MemeGenerationStatus.REVIEW_REQUIRED
            else -> MemeGenerationStatus.DRAFT
        }

        val draft = MemeDraft(
            id = UUID.randomUUID().toString(),
            sourceOpportunityId = topic.sourceOpportunityId,
            topic = topic.topic,
            memeFormat = expectedFormat,
            setupText = setupText,
            punchlineText = punchlineText,
            caption = caption,
            hashtags = hashtags,
            sourceUrl = sourceUrl,
            sourceName = sourceName,
            verificationStatus = topic.verificationStatus,
            safetyStatus = safetyEvaluation.status,
            generationStatus = initialGenerationStatus,
            contentHash = contentHash,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            rawJson = rawJson
        )

        return MemeGenerationOutcome.Success(draft)
    }

    private data class ParsedMemeFields(
        val setupText: String,
        val punchlineText: String,
        val caption: String,
        val hashtags: List<String>
    )

    private fun parseRawJson(rawJson: String, expectedFormat: MemeFormat): Result<ParsedMemeFields> {
        return try {
            val cleaned = rawJson.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val json = JSONObject(cleaned)

            val setup = json.optString("setupText").trim()
            if (setup.isBlank()) {
                return Result.failure(IllegalArgumentException("Missing setupText in meme response"))
            }

            val punchline = json.optString("punchlineText").trim()
            if (punchline.isBlank()) {
                return Result.failure(IllegalArgumentException("Missing punchlineText in meme response"))
            }

            val caption = json.optString("caption").ifBlank { "$setup — $punchline" }.trim()

            val tagsArray = json.optJSONArray("hashtags") ?: JSONArray()
            val tagsList = mutableListOf<String>()
            for (i in 0 until tagsArray.length()) {
                val tag = tagsArray.optString(i).trim()
                if (tag.isNotBlank()) {
                    val formatted = if (tag.startsWith("#")) tag else "#$tag"
                    tagsList.add(formatted)
                }
            }

            Result.success(
                ParsedMemeFields(
                    setupText = setup,
                    punchlineText = punchline,
                    caption = caption,
                    hashtags = tagsList
                )
            )
        } catch (e: Exception) {
            Result.failure(IllegalArgumentException("Invalid JSON: ${e.localizedMessage}", e))
        }
    }
}
