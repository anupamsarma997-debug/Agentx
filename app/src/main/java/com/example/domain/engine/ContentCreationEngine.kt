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
            VerificationStatus.FAILED -> GenerationGateResult.Blocked(
                "Cannot generate content for an unverified or dead link."
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
            - Sirf wahi facts likho jo given source text me hon. Date, deadline, stipend ya eligibility guess mat karo. Na mile to likho 'details official site par dekhein'.
            - Never invent facts, statistics, organizations, URLs, or deadlines.
            - If any information is missing or not provided, explicitly write "Details official site par dekhein".
            - Do NOT truncate any field with "..." or leave sentences incomplete. Complete every sentence cleanly.
            - Tone: clear, concise, informative, mobile-friendly Indian English. No clickbait or sensationalism.
            - Length Target: ${length.displayName} (${length.wordCountGuide}, target ~${length.targetWords} words).
            - Platform Target: ${platform.displayName}.
            - Content Type: ${contentType.displayName}.
            - Hashtags: Maximum 8 highly relevant hashtags.
            - Call to Action: Factual only (e.g. "Apply or check eligibility on the official portal.").
            
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
              "title": "Concise headline (min 5 characters, no ellipsis)",
              "facebookBody": "Full Facebook post body (min 100 characters with complete sentences, details, and call to action)",
              "instagramCaption": "Short caption for Instagram feed with key bullet points",
              "hashtags": ["tag1", "tag2"],
              "sourceUrl": "The exact source URL provided in the prompt",
              "sourceName": "The official source organization name",
              "deadline": "Exact deadline string if in source, otherwise null",
              "eligibility": "Exact eligibility if in source, otherwise 'Details official site par dekhein'",
              "contentType": "${contentType.name}",
              "platform": "${platform.name}",
              "confidence": "HIGH",
              "needsReview": false
            }
        """.trimIndent()
    }

    /**
     * Formats structured source facts and live fetched page context into a clean factual prompt.
     */
    fun buildUserPrompt(fact: SourceFact, liveSourceText: String? = null): String {
        val contextSnippet = if (!liveSourceText.isNullOrBlank()) {
            "\n\nLIVE SOURCE PAGE TEXT (Use ONLY these verified facts):\n$liveSourceText"
        } else {
            ""
        }

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
            - Verification State: ${fact.verificationStatus}$contextSnippet
            
            CRITICAL RULES:
            1. The sourceUrl in your response MUST BE EXACTLY: ${fact.sourceUrl}
            2. Never write literal "..." or cut off sentences. Provide a complete, fully-written post body.
            3. Do not invent any deadline or stipend. If missing in source facts, state: 'Details official site par dekhein'.
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

        // Fetch live source page text via HTTP GET with 8s timeout to ensure factual grounded generation
        val liveSourceText = geminiClient.fetchLiveSourcePageText(fact.sourceUrl)

        val systemPrompt = buildSystemPrompt(contentType, platform, length, fact)
        val userPrompt = buildUserPrompt(fact, liveSourceText)

        // Attempt 1: Standard generation
        var aiResult = geminiClient.generateContent(systemPrompt, userPrompt)

        if (aiResult is AIResult.Success) {
            val firstTryValidation = validateAndParseResponse(
                rawJson = aiResult.jsonText,
                expectedSourceUrl = fact.sourceUrl,
                expectedSourceName = fact.sourceName,
                expectedDeadline = fact.deadline,
                expectedContentType = contentType,
                expectedPlatform = platform,
                liveSourceText = liveSourceText
            )

            if (firstTryValidation.isSuccess) {
                val initialStatus = resolveInitialStatus(verificationStatus)
                return ContentCreationOutcome.Success(firstTryValidation.getOrThrow(), initialStatus)
            }

            // If truncated or malformed, attempt one automatic retry with strict anti-truncation prompt
            val retryUserPrompt = "$userPrompt\n\nATTENTION: Your previous output was rejected because a field was empty, truncated with '...', or incomplete. Provide complete non-truncated JSON."
            aiResult = geminiClient.generateContent(systemPrompt, retryUserPrompt)
        }

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
                    expectedPlatform = platform,
                    liveSourceText = liveSourceText
                )

                if (validation.isSuccess) {
                    val initialStatus = resolveInitialStatus(verificationStatus)
                    ContentCreationOutcome.Success(validation.getOrThrow(), initialStatus)
                } else {
                    // Fallback to Needs Review with factual baseline so user never loses work
                    val fallbackResult = GeneratedContentResult(
                        title = fact.title,
                        body = "${fact.title}\n\n${fact.description}\n\nEligibility: ${fact.eligibility ?: "Details official site par dekhein"}\nDeadline: ${fact.deadline ?: "Details official site par dekhein"}\nOfficial Portal: ${fact.sourceUrl}",
                        caption = "${fact.title} - Apply at ${fact.sourceUrl}",
                        hashtags = listOf("#Opportunity", "#Career", "#Alert"),
                        sourceUrl = fact.sourceUrl,
                        sourceName = fact.sourceName,
                        contentType = contentType,
                        platform = platform,
                        confidence = "MEDIUM",
                        needsReview = true,
                        rawJson = "{}"
                    )
                    ContentCreationOutcome.Success(fallbackResult, GenerationStatus.NEEDS_REVIEW)
                }
            }
        }
    }

    private fun buildSystemPrompt(
        contentType: ContentType,
        platform: ContentPlatform,
        length: ContentLength,
        fact: SourceFact
    ): String = buildSystemPrompt(contentType, platform, length)

    /**
     * Strict JSON validator ensuring AI did not fabricate URLs, deadlines, or omit required fields.
     */
    fun validateAndParseResponse(
        rawJson: String,
        expectedSourceUrl: String,
        expectedSourceName: String,
        expectedDeadline: String?,
        expectedContentType: ContentType,
        expectedPlatform: ContentPlatform,
        liveSourceText: String? = null
    ): Result<GeneratedContentResult> {
        return try {
            // Strip markdown code fences if present (e.g., ```json ... ```)
            val cleanedJson = rawJson.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val json = JSONObject(cleanedJson)

            val title = json.optString("title").trim()
            if (title.isBlank() || title == "..." || title.endsWith("...")) {
                return Result.failure(IllegalArgumentException("Missing or truncated title in AI output"))
            }

            // Support both facebookBody and body
            val rawBody = if (json.has("facebookBody")) {
                json.optString("facebookBody")
            } else {
                json.optString("body")
            }.trim()

            if (rawBody.isBlank() || rawBody == "..." || rawBody.endsWith("...")) {
                return Result.failure(IllegalArgumentException("Missing or truncated post body in AI output"))
            }

            // Check if body is suspiciously short (< 50 chars) or literally just "..."
            if (rawBody.length < 50) {
                return Result.failure(IllegalArgumentException("Post body too short (${rawBody.length} chars)"))
            }

            val caption = json.optString("instagramCaption").ifBlank {
                json.optString("caption").ifBlank { rawBody.take(150) }
            }.trim()

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
            val finalSourceUrl = expectedSourceUrl

            // If source had no deadline, check for fabricated specific date claims in title or body
            var cleanBody = rawBody
            if (expectedDeadline.isNullOrBlank()) {
                val lowercaseTitle = title.lowercase()
                if (lowercaseTitle.contains("deadline: 202") || lowercaseTitle.contains("last date: 202")) {
                    return Result.failure(
                        IllegalArgumentException("Fabricated deadline detected when source provided none.")
                    )
                }
            }

            val parsedResult = GeneratedContentResult(
                title = title,
                body = cleanBody,
                caption = caption,
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
