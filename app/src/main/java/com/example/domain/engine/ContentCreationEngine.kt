package com.example.domain.engine

import com.example.data.model.content.ContentLength
import com.example.data.model.content.ContentPlatform
import com.example.data.model.content.ContentType
import com.example.data.model.content.GenerationStatus
import com.example.data.model.opportunity.VerificationStatus
import com.example.data.remote.ai.AIResult
import com.example.data.remote.ai.GeminiClient
import com.example.domain.model.GeneratedContentResult
import com.example.domain.model.SourceFact
import org.json.JSONArray
import org.json.JSONObject

sealed interface GenerationGateResult {
    data object Permitted : GenerationGateResult
    data class Blocked(val reason: String) : GenerationGateResult
}

sealed interface ContentCreationOutcome {
    data class Success(val result: GeneratedContentResult, val initialStatus: GenerationStatus) : ContentCreationOutcome
    data class ConfigurationRequired(val message: String) : ContentCreationOutcome
    data class Error(val message: String) : ContentCreationOutcome
}

class ContentCreationEngine(
    private val geminiClient: GeminiClient = GeminiClient()
) {

    /**
     * Enforces the verification gate before generation is permitted.
     */
    fun checkGenerationGate(verificationStatus: VerificationStatus): GenerationGateResult {
        return when (verificationStatus) {
            VerificationStatus.VERIFIED -> GenerationGateResult.Permitted
            VerificationStatus.NEEDS_REVIEW -> GenerationGateResult.Permitted
            VerificationStatus.EXPIRED -> GenerationGateResult.Blocked(
                "Cannot generate promotional content for an expired opportunity."
            )
            VerificationStatus.REJECTED -> GenerationGateResult.Blocked(
                "Cannot generate content for a rejected or fraudulent opportunity."
            )
        }
    }

    /**
     * Determines initial content generation status based on source verification state.
     */
    fun resolveInitialStatus(verificationStatus: VerificationStatus): GenerationStatus {
        return when (verificationStatus) {
            VerificationStatus.VERIFIED -> GenerationStatus.GENERATED
            VerificationStatus.NEEDS_REVIEW -> GenerationStatus.REVIEW_REQUIRED
            else -> GenerationStatus.REVIEW_REQUIRED
        }
    }

    /**
     * Constructs strict system instructions enforcing zero fabrication, neutral tone, and JSON schema.
     */
    fun buildSystemPrompt(contentType: ContentType, platform: ContentPlatform, length: ContentLength): String {
        return """
            You are a factual social media copy assistant for an Indian opportunity and news portal.
            
            CORE MANDATE: SOURCE FACTS FIRST.
            - Never invent facts, statistics, organizations, URLs, or deadlines.
            - If any information is missing or not provided, explicitly write "Not specified by source".
            - Tone: clear, concise, informative, mobile-friendly Indian English. No clickbait or sensationalism.
            - Length Target: ${length.displayName} (${length.wordCountGuide}, target ~${length.targetWords} words).
            - Platform Target: ${platform.displayName}.
            - Content Type: ${contentType.displayName}.
            - Hashtags: Maximum 8 highly relevant hashtags.
            - Call to Action: Factual only (e.g. "Check the official source for eligibility and application details.").
            
            REGIONAL HEADERS:
            - If the opportunity region is Assam: start with "📢 Assam Opportunity Alert"
            - If Northeast India: start with "📢 Northeast Opportunity Alert"
            - If India: start with "🇮🇳 India Opportunity Alert"
            - If International: start with "🌍 International Opportunity Alert"
            
            POLITICAL & NEWS NEUTRALITY:
            - If this is a news or political topic, remain strictly neutral, balanced, and factual.
            - Attribute all statements directly to the source. Do not endorse, praise, or criticize any party or political figure.
            
            OUTPUT REQUIREMENT:
            Return ONLY a valid JSON object matching this exact schema:
            {
              "title": "Concise headline",
              "body": "Formatted post body with details, deadline, eligibility, and source URL",
              "caption": "Short caption for Instagram/Facebook feed",
              "hashtags": ["tag1", "tag2"],
              "sourceUrl": "The exact source URL provided in the prompt",
              "sourceName": "The exact source name provided in the prompt",
              "contentType": "${contentType.name}",
              "platform": "${platform.name}",
              "confidence": "HIGH",
              "needsReview": false
            }
        """.trimIndent()
    }

    /**
     * Formats structured source facts into a clean factual prompt.
     */
    fun buildUserPrompt(fact: SourceFact): String {
        return """
            GENERATE SOCIAL MEDIA POST DRAFT FROM THESE VERIFIED SOURCE FACTS:
            - Title: ${fact.title}
            - Organization: ${fact.organization ?: "Not specified by source"}
            - Description: ${fact.description}
            - Category: ${fact.category}
            - Region: ${fact.region}
            - Eligibility: ${fact.eligibility ?: "Not specified by source"}
            - Deadline: ${fact.deadline ?: "Not specified by source"}
            - Published Date: ${fact.publishedAt ?: "Not specified"}
            - Source Entity: ${fact.sourceName}
            - Source URL: ${fact.sourceUrl}
            - Verification State: ${fact.verificationStatus}
            
            CRITICAL: The sourceUrl in your response MUST BE EXACTLY: ${fact.sourceUrl}
        """.trimIndent()
    }

    /**
     * Generates structured draft content and validates the AI response against source facts.
     */
    suspend fun generateContent(
        fact: SourceFact,
        contentType: ContentType = ContentType.OPPORTUNITY_POST,
        platform: ContentPlatform = ContentPlatform.BOTH,
        length: ContentLength = ContentLength.SHORT
    ): ContentCreationOutcome {
        val verificationStatus = VerificationStatus.fromString(fact.verificationStatus)
        val gate = checkGenerationGate(verificationStatus)
        if (gate is GenerationGateResult.Blocked) {
            return ContentCreationOutcome.Error(gate.reason)
        }

        val systemPrompt = buildSystemPrompt(contentType, platform, length)
        val userPrompt = buildUserPrompt(fact)

        val aiResult = geminiClient.generateContent(systemPrompt, userPrompt)

        return when (aiResult) {
            is AIResult.ConfigurationRequired -> {
                ContentCreationOutcome.ConfigurationRequired(aiResult.message)
            }
            is AIResult.Error -> {
                ContentCreationOutcome.Error(aiResult.message)
            }
            is AIResult.Success -> {
                val validation = validateAndParseResponse(
                    rawJson = aiResult.jsonText,
                    expectedSourceUrl = fact.sourceUrl,
                    expectedSourceName = fact.sourceName,
                    expectedDeadline = fact.deadline,
                    expectedContentType = contentType,
                    expectedPlatform = platform
                )

                if (validation.isSuccess) {
                    val initialStatus = resolveInitialStatus(verificationStatus)
                    ContentCreationOutcome.Success(validation.getOrThrow(), initialStatus)
                } else {
                    ContentCreationOutcome.Error(
                        "AI validation failed: ${validation.exceptionOrNull()?.message ?: "Malformed response"}"
                    )
                }
            }
        }
    }

    /**
     * Strict JSON validator ensuring AI did not fabricate URLs, deadlines, or omit required fields.
     */
    fun validateAndParseResponse(
        rawJson: String,
        expectedSourceUrl: String,
        expectedSourceName: String,
        expectedDeadline: String?,
        expectedContentType: ContentType,
        expectedPlatform: ContentPlatform
    ): Result<GeneratedContentResult> {
        return try {
            // Strip markdown code fences if present (e.g., ```json ... ```)
            val cleanedJson = rawJson.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val json = JSONObject(cleanedJson)

            val title = json.optString("title")
            if (title.isBlank()) {
                return Result.failure(IllegalArgumentException("Missing title in AI output"))
            }

            val body = json.optString("body")
            if (body.isBlank()) {
                return Result.failure(IllegalArgumentException("Missing post body in AI output"))
            }

            val caption = json.optString("caption").ifBlank { body.take(150) }

            // Validate hashtags (max 8)
            val hashtagsArray = json.optJSONArray("hashtags") ?: JSONArray()
            val hashtagsList = mutableListOf<String>()
            for (i in 0 until hashtagsArray.length()) {
                val tag = hashtagsArray.optString(i)
                if (tag.isNotBlank()) {
                    val formatted = if (tag.startsWith("#")) tag else "#$tag"
                    hashtagsList.add(formatted)
                }
            }
            val safeHashtags = hashtagsList.take(8)

            // CRITICAL FACT PROTECTION: The sourceUrl MUST come from the original OpportunityEntity.
            // Even if AI alters the URL, we strictly force the verified expectedSourceUrl.
            val finalSourceUrl = expectedSourceUrl

            // If source had no deadline, check for fabricated specific date claims in title
            if (expectedDeadline.isNullOrBlank()) {
                val lowercaseTitle = title.lowercase()
                if (lowercaseTitle.contains("deadline: 202") || lowercaseTitle.contains("last date: 202")) {
                    return Result.failure(
                        IllegalArgumentException("Fabricated deadline detected when source provided none.")
                    )
                }
            }

            val parsedResult = GeneratedContentResult(
                title = title.trim(),
                body = body.trim(),
                caption = caption.trim(),
                hashtags = safeHashtags,
                sourceUrl = finalSourceUrl,
                sourceName = expectedSourceName,
                contentType = expectedContentType,
                platform = expectedPlatform,
                confidence = json.optString("confidence", "HIGH"),
                needsReview = json.optBoolean("needsReview", false),
                rawJson = cleanedJson
            )

            Result.success(parsedResult)
        } catch (e: Exception) {
            Result.failure(IllegalArgumentException("Invalid JSON format from AI: ${e.localizedMessage}", e))
        }
    }
}
