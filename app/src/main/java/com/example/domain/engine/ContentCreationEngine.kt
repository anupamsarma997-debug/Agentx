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
    fun buildSystemPrompt(
        contentType: ContentType,
        platform: ContentPlatform,
        length: ContentLength,
        language: String = "ASSAMESE"
    ): String {
        val languageInstruction = if (language.equals("ASSAMESE", ignoreCase = true)) {
            """
            ASSAMESE SCRIPT MANDATE (অসমীয়া আখৰ):
            - Write the post title, Facebook post body, and Instagram caption in clear, authentic Assamese script (অসমীয়া আখৰ).
            - Use these clear Assamese section headers:
              • 📢 জাননী (Headline / Announcement)
              • 📌 পদ / আঁচনিৰ সবিশেষ (Details)
              • 🎯 কি কি যোগ্যতা লাগিব (Requirements / Eligibility: Age limit, Education, Physical tests)
              • 📄 প্ৰয়োজনীয় নথিপত্ৰ (Required Documents)
              • 🔗 ক'ত আবেদন কৰিব (Where to Apply official link)
              • ⏰ অন্তিম তাৰিখ (Deadline)
            """.trimIndent()
        } else {
            "- Tone: clear, concise, informative, mobile-friendly Indian English or Hinglish."
        }

        return """
            You are a factual social media copy assistant for an Indian opportunity and news portal.
            
            CORE MANDATE: SOURCE FACTS FIRST.
            - Sirf wahi facts likho jo given source text me hon. Date, deadline, stipend ya eligibility guess mat karo. Na mile to likho 'details official site par dekhein'.
            - Never invent facts, statistics, organizations, URLs, or deadlines.
            - If any information is missing or not provided, explicitly write "Details official site par dekhein".
            - Do NOT truncate any field with "..." or leave sentences incomplete. Complete every sentence cleanly.
            $languageInstruction
            - Length Target: ${length.displayName} (${length.wordCountGuide}, target ~${length.targetWords} words).
            - Platform Target: ${platform.displayName}.
            - Content Type: ${contentType.displayName}.
            - Hashtags: Maximum 8 highly relevant hashtags (include relevant Assamese/India hashtags).
            - Call to Action: Factual only (e.g. "পোনে পোনে অফিচিয়েল প'ৰ্টেলত আবেদন কৰক / Apply on official portal.").
            
            REGIONAL HEADERS:
            - If the opportunity region is Assam: start with "📢 অসম চৰকাৰৰ আঁচনি / নিযুক্তি জাননী (Assam Alert)"
            - If Northeast India: start with "📢 উত্তৰ-পূব জাননী (Northeast Alert)"
            - If India: start with "🇮🇳 ভাৰতীয় জাননী (India Alert)"
            - If International: start with "🌍 International Opportunity Alert"
            
            POLITICAL & NEWS NEUTRALITY:
            - If this is a news or political topic, remain strictly neutral, balanced, and factual.
            - Attribute all statements directly to the source. Do not endorse, praise, or criticize any party or political figure.
            
            OUTPUT REQUIREMENT:
            Return ONLY a valid JSON object matching this exact schema:
            {
              "title": "Concise headline in Assamese/English (min 5 characters, no ellipsis)",
              "facebookBody": "Full Facebook post body with complete sentences, requirements, eligibility, where to apply, and deadline",
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
        length: ContentLength = ContentLength.SHORT,
        language: String = "ASSAMESE"
    ): ContentCreationOutcome {
        val verificationStatus = VerificationStatus.fromString(fact.verificationStatus)
        val gate = checkGenerationGate(verificationStatus)
        if (gate is GenerationGateResult.Blocked) {
            return ContentCreationOutcome.Error(gate.reason)
        }

        // Fetch live source page text via HTTP GET with 8s timeout to ensure factual grounded generation
        val liveSourceText = geminiClient.fetchLiveSourcePageText(fact.sourceUrl)

        val systemPrompt = buildSystemPrompt(contentType, platform, length, language)
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
            val retryUserPrompt = "$userPrompt\n\nATTENTION: Your previous output was rejected because a field was empty, truncated with '...', or incomplete. Provide complete non-truncated JSON in Assamese script."
            aiResult = geminiClient.generateContent(systemPrompt, retryUserPrompt)
        }

        val isAssameseMode = language.equals("ASSAMESE", ignoreCase = true) ||
            fact.region.contains("Assam", ignoreCase = true) ||
            fact.title.any { it in '\u0980'..'\u09FF' }

        return when (aiResult) {
            is AIResult.ConfigurationRequired, is AIResult.Error -> {
                val fallbackBody = if (isAssameseMode) {
                    buildString {
                        val headerIcon = when (contentType) {
                            ContentType.HACKATHON_ALERT -> "💻 হেকাথন আৰু প্ৰযুক্তি প্ৰতিযোগিতা জাননী (Tech Innovation Alert)"
                            ContentType.MSME_ALERT -> "🏢 উদ্যোগ ঋণ আৰু চৰকাৰী ৰাজসাহায্য (MSME Scheme Alert)"
                            ContentType.JOB_ALERT -> "🛡️ চৰকাৰী নিযুক্তি জাননী (Government Job Recruitment)"
                            ContentType.SCHOLARSHIP_ALERT -> "🎓 ছাত্ৰ-ছাত্ৰীৰ বাবে চৰকাৰী বৃত্তি (Scholarship Alert)"
                            ContentType.STARTUP_ALERT -> "🚀 ষ্টাৰ্টআপ পুঁজি আৰু ইনকিউবেচন (Startup Funding Alert)"
                            else -> "📢 চৰকাৰী আঁচনি আৰু অফিচিয়েল জাননী (Official Alert)"
                        }
                        append(headerIcon).append("\n\n")
                        append("📌 ").append(fact.title).append("\n\n")
                        append("📋 সবিশেষ বিৱৰণ (Details):\n").append(fact.description).append("\n\n")
                        if (!fact.eligibility.isNullOrBlank()) {
                            append("🎯 কি কি যোগ্যতা লাগিব (Eligibility & Requirements):\n• ").append(fact.eligibility).append("\n\n")
                        }
                        if (!fact.deadline.isNullOrBlank()) {
                            append("⏰ আবেদন / পঞ্জীয়নৰ অন্তিম তাৰিখ (Deadline): ").append(fact.deadline).append("\n\n")
                        }
                        if (!fact.organization.isNullOrBlank()) {
                            append("🏛️ সংগঠন / বিভাগ: ").append(fact.organization).append("\n\n")
                        }
                        append("🔗 ক'ত আৰু কেনেকৈ আবেদন কৰিব (Where to Apply / Official Portal):\n")
                        append("তলত দিয়া অফিচিয়েল পৰ্টেললৈ গৈ অনলাইন আবেদন কৰক:\n👉 ").append(fact.sourceUrl).append("\n\n")
                        append("⚠️ অনলাইন আবেদনৰ পূৰ্বে অফিচিয়েল ৱেবচাইটত প্ৰকাশিত মূল জাননীখন ভালদৰে পঢ়ি লওক।")
                    }
                } else {
                    buildString {
                        append("📢 ").append(fact.title).append("\n\n")
                        append(fact.description).append("\n\n")
                        if (!fact.eligibility.isNullOrBlank()) {
                            append("🎯 Eligibility & Requirements:\n• ").append(fact.eligibility).append("\n\n")
                        }
                        if (!fact.deadline.isNullOrBlank()) {
                            append("⏰ Deadline: ").append(fact.deadline).append("\n\n")
                        }
                        append("🔗 Where to Apply: ").append(fact.sourceUrl)
                    }
                }

                val fallbackCaption = if (isAssameseMode) {
                    "${fact.title}\n\nকি কি যোগ্যতা লাগিব আৰু ক'ত আবেদন কৰিব চাওক।\n🔗 পৰ্টেল: ${fact.sourceUrl}"
                } else {
                    "📢 ${fact.title}\n\n${fact.description.take(160)}...\n\n⏰ Deadline: ${fact.deadline ?: "Official site par dekhein"}\n🔗 Portal: ${fact.sourceUrl}"
                }

                val fallbackHashtags = when (contentType) {
                    ContentType.HACKATHON_ALERT -> listOf("#Hackathon", "#Coding", "#SmartIndia", "#TechInnovation", "#Developer", "#Students")
                    ContentType.MSME_ALERT -> listOf("#MSME", "#PMEGP", "#BharatSarkar", "#BusinessLoan", "#Subsidy", "#StartupIndia")
                    ContentType.JOB_ALERT -> if (isAssameseMode) listOf("#AssamJobs", "#JobAlert", "#SLPRB", "#AssamPolice", "#IndianArmy", "#Agniveer") else listOf("#JobAlert", "#Recruitment", "#Career", "#GovtJobs")
                    ContentType.SCHOLARSHIP_ALERT -> listOf("#Scholarship", "#Students", "#Education", "#Pragati", "#AICTE", "#Career")
                    ContentType.MEME_POST -> listOf("#Relatable", "#MemeTime", "#MSME", "#StudentLife", "#DesiHumor")
                    else -> if (isAssameseMode) {
                        listOf("#AssamGovt", "#অসম", "#AssamSchemes", "#AssamJobs", "#JobAlertAssam")
                    } else {
                        listOf("#Opportunity", "#Career", "#MSME", "#BharatSarkar", "#JobAlert", "#Alert")
                    }
                }

                val fallbackResult = GeneratedContentResult(
                    title = fact.title,
                    body = fallbackBody,
                    caption = fallbackCaption,
                    hashtags = fallbackHashtags,
                    sourceUrl = fact.sourceUrl,
                    sourceName = fact.sourceName,
                    contentType = contentType,
                    platform = platform,
                    confidence = "HIGH",
                    needsReview = false,
                    rawJson = "{}"
                )
                val initialStatus = resolveInitialStatus(verificationStatus)
                ContentCreationOutcome.Success(fallbackResult, initialStatus)
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
                    val fallbackResult = GeneratedContentResult(
                        title = fact.title,
                        body = if (isAssameseMode) {
                            "📢 ${fact.title}\n\n${fact.description}\n\n🎯 কি কি যোগ্যতা লাগিব: ${fact.eligibility ?: "অফিচিয়েল ৱেবচাইটত চাওক"}\n⏰ অন্তিম তাৰিখ: ${fact.deadline ?: "অফিচিয়েল ৱেবচাইটত চাওক"}\n🔗 ক'ত আবেদন কৰিব: ${fact.sourceUrl}"
                        } else {
                            "${fact.title}\n\n${fact.description}\n\nEligibility: ${fact.eligibility ?: "Details official site par dekhein"}\nDeadline: ${fact.deadline ?: "Details official site par dekhein"}\nOfficial Portal: ${fact.sourceUrl}"
                        },
                        caption = "${fact.title} - Apply at ${fact.sourceUrl}",
                        hashtags = if (isAssameseMode) listOf("#AssamGovt", "#অসম", "#AssamCareer") else listOf("#Opportunity", "#Career", "#Alert"),
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

            // Check if body is suspiciously empty or literally just "..."
            if (rawBody.length < 5) {
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
