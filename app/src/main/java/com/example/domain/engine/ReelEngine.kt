package com.example.domain.engine

import com.example.data.local.dao.ReelDao
import com.example.data.local.entity.ReelDraftEntity
import com.example.data.local.settings.AppSettings
import com.example.data.model.opportunity.VerificationStatus
import com.example.data.model.reel.ReelDraft
import com.example.data.model.reel.ReelGenerationOutcome
import com.example.data.model.reel.ReelGenerationStatus
import com.example.data.model.reel.ReelLanguage
import com.example.data.model.reel.ReelSafetyStatus
import com.example.data.model.reel.ReelScene
import com.example.data.model.reel.ReelTopic
import com.example.data.model.reel.ReelType
import com.example.data.remote.ai.AIResult
import com.example.data.remote.ai.GeminiClient
import com.example.domain.automation.FreeTierGuard
import com.example.domain.automation.GenerationDecision
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import java.util.UUID

class ReelEngine(
    private val geminiClient: GeminiClient = GeminiClient(),
    private val safetyEngine: ReelSafetyEngine = ReelSafetyEngine(),
    private val freeTierGuard: FreeTierGuard = FreeTierGuard(),
    private val reelDao: ReelDao? = null
) {

    /**
     * Main entry point to generate a complete Reel Plan.
     */
    suspend fun generateReel(
        topic: ReelTopic,
        reelType: ReelType,
        durationSeconds: Int = 30,
        language: ReelLanguage = ReelLanguage.ENGLISH,
        settings: AppSettings = AppSettings()
    ): ReelGenerationOutcome {
        // 1. Eligibility gate check
        val eligibility = checkSourceEligibility(topic)
        if (eligibility != null) {
            return eligibility
        }

        // 2. Free-tier quota check (before calling Gemini)
        val decision = freeTierGuard.canGenerateReel(settings)
        if (decision is GenerationDecision.QuotaExhausted) {
            return ReelGenerationOutcome.QuotaExhausted(decision.reason)
        }
        if (decision is GenerationDecision.TargetReached) {
            return ReelGenerationOutcome.QuotaExhausted(decision.message)
        }

        // 3. Build prompts
        val systemPrompt = ReelPromptBuilder.buildSystemPrompt()
        val userPrompt = ReelPromptBuilder.buildUserPrompt(topic, reelType, durationSeconds, language)

        // 4. Call Gemini AI
        val aiResult = geminiClient.generateContent(systemPrompt, userPrompt)
        val jsonText = when (aiResult) {
            is AIResult.Success -> aiResult.jsonText
            is AIResult.ConfigurationRequired -> return ReelGenerationOutcome.ConfigurationRequired(aiResult.message)
            is AIResult.Error -> return ReelGenerationOutcome.Error("AI Generation failed: ${aiResult.message}")
        }

        // 5. Parse, validate, and verify the AI output
        return parseAndValidateReel(
            rawText = jsonText,
            topic = topic,
            targetReelType = reelType,
            expectedDuration = durationSeconds,
            language = language
        )
    }

    /**
     * Checks if the source opportunity/topic is eligible for Reel generation.
     */
    fun checkSourceEligibility(topic: ReelTopic): ReelGenerationOutcome? {
        val status = topic.verificationStatus.uppercase(Locale.ROOT)
        if (status == VerificationStatus.REJECTED.name) {
            return ReelGenerationOutcome.SourceIneligible(
                "Source is marked REJECTED and cannot be used for Reel generation."
            )
        }
        if (status == VerificationStatus.EXPIRED.name) {
            return ReelGenerationOutcome.SourceIneligible(
                "Source is EXPIRED and cannot be used for new Reel generation."
            )
        }
        return null
    }

    /**
     * Parses raw JSON and validates all strict Phase 7 rules.
     */
    suspend fun parseAndValidateReel(
        rawText: String,
        topic: ReelTopic,
        targetReelType: ReelType,
        expectedDuration: Int,
        language: ReelLanguage
    ): ReelGenerationOutcome {
        val cleanJson = cleanJsonString(rawText)
        val json = try {
            JSONObject(cleanJson)
        } catch (_: Exception) {
            return ReelGenerationOutcome.ValidationFailed("Invalid JSON returned by AI.")
        }

        // 1. Reel Type check
        val reelTypeStr = json.optString("reelType", targetReelType.name)
        val parsedReelType = ReelType.entries.firstOrNull { it.name.equals(reelTypeStr, ignoreCase = true) }
            ?: return ReelGenerationOutcome.ValidationFailed("Unsupported Reel type '$reelTypeStr'.")

        // 2. Title & Hook check
        val title = json.optString("title", "").trim()
        val hook = json.optString("hook", "").trim()
        if (hook.isBlank()) {
            return ReelGenerationOutcome.ValidationFailed("Missing hook. The Reel plan requires a 1-3 second hook.")
        }

        // 3. Duration check
        val duration = json.optInt("durationSeconds", expectedDuration)
        if (duration > 60) {
            return ReelGenerationOutcome.ValidationFailed("Duration $duration exceeds maximum limit of 60 seconds.")
        }
        if (duration < 15) {
            return ReelGenerationOutcome.ValidationFailed("Duration $duration is below minimum limit of 15 seconds.")
        }

        // 4. Scenes check
        val scenesArray = json.optJSONArray("scenes")
            ?: return ReelGenerationOutcome.ValidationFailed("Missing scenes list in AI output.")
        if (scenesArray.length() == 0) {
            return ReelGenerationOutcome.ValidationFailed("Scenes list cannot be empty.")
        }

        val scenes = mutableListOf<ReelScene>()
        for (i in 0 until scenesArray.length()) {
            val sceneObj = scenesArray.optJSONObject(i)
                ?: return ReelGenerationOutcome.ValidationFailed("Scene at index $i is not a valid JSON object.")
            val sceneNumber = sceneObj.optInt("sceneNumber", i + 1)
            val sceneDuration = sceneObj.optInt("durationSeconds", 0)
            val visualDesc = sceneObj.optString("visualDescription", "").trim()
            val onScreenText = sceneObj.optString("onScreenText", "").trim()
            val voiceoverText = sceneObj.optString("voiceoverText", "").trim()
            val transition = sceneObj.optString("transition", "Cut")
            val bg = if (sceneObj.has("backgroundSuggestion") && !sceneObj.isNull("backgroundSuggestion")) {
                sceneObj.getString("backgroundSuggestion")
            } else null

            scenes.add(
                ReelScene(
                    sceneNumber = sceneNumber,
                    durationSeconds = sceneDuration,
                    visualDescription = visualDesc,
                    onScreenText = onScreenText,
                    voiceoverText = voiceoverText,
                    transition = transition,
                    backgroundSuggestion = bg
                )
            )
        }

        // 5. Total scene duration MUST equal total Reel duration
        val totalSceneDuration = scenes.sumOf { it.durationSeconds }
        if (totalSceneDuration != duration) {
            return ReelGenerationOutcome.ValidationFailed(
                "Scene duration sum ($totalSceneDuration s) does not equal total Reel duration ($duration s)."
            )
        }

        // 6. Voiceover check
        val voiceover = json.optString("voiceover", "").trim().ifBlank {
            scenes.joinToString(" ") { it.voiceoverText }.trim()
        }
        if (voiceover.isBlank()) {
            return ReelGenerationOutcome.ValidationFailed("Missing voiceover script for voiceover-enabled Reel.")
        }

        // 7. Caption and Source Attribution check
        val caption = json.optString("caption", "").trim()
        if (caption.isBlank()) {
            return ReelGenerationOutcome.ValidationFailed("Missing caption in AI output.")
        }

        // Verify source attribution in caption or scenes
        val containsSourceName = caption.contains(topic.sourceName, ignoreCase = true) ||
                scenes.any { it.onScreenText.contains(topic.sourceName, ignoreCase = true) || it.voiceoverText.contains(topic.sourceName, ignoreCase = true) }
        val containsSourceWord = caption.contains("Source:", ignoreCase = true) ||
                scenes.any { it.onScreenText.contains("Source:", ignoreCase = true) }

        if (!containsSourceName && !containsSourceWord) {
            return ReelGenerationOutcome.ValidationFailed(
                "Missing source attribution. The plan must cite 'Source: ${topic.sourceName}'."
            )
        }

        // 8. Source URL protection check
        val aiReturnedUrl = json.optString("sourceUrl", "").trim()
        if (topic.sourceUrl.isNotBlank() && aiReturnedUrl.isNotBlank() && !aiReturnedUrl.equals(topic.sourceUrl, ignoreCase = true)) {
            return ReelGenerationOutcome.ValidationFailed(
                "AI-generated URL ('$aiReturnedUrl') differs from stored source URL ('${topic.sourceUrl}')."
            )
        }

        // 9. Hashtags count check (maximum 8)
        val hashtags = parseHashtags(json.optJSONArray("hashtags"))
        if (hashtags.size > 8) {
            return ReelGenerationOutcome.ValidationFailed(
                "Too many hashtags (${hashtags.size}). Maximum allowed is 8."
            )
        }

        // 10. Fact preservation check: Check for fabricated deadlines if topic deadline was specified or absent
        val deadlineCheck = checkForFabricatedDeadlines(topic, scenes, voiceover, caption)
        if (deadlineCheck != null) {
            return ReelGenerationOutcome.ValidationFailed(deadlineCheck)
        }

        // 11. Duplicate detection
        val sourceId = topic.sourceOpportunityId ?: topic.sourceContentId
        val contentHash = ReelDuplicateDetector.computeHash(
            sourceId = sourceId,
            reelType = parsedReelType.name,
            hook = hook,
            topic = topic.topic
        )

        if (reelDao != null) {
            val existing = reelDao.getReelByContentHash(contentHash)
            if (existing != null) {
                return ReelGenerationOutcome.DuplicateDetected(contentHash)
            }
        }

        // 12. Safety Evaluation
        val safetyEvaluation = safetyEngine.evaluate(
            topic = topic.topic,
            hook = hook,
            scenes = scenes,
            voiceover = voiceover,
            caption = caption,
            isPoliticalExplicit = topic.isPolitical
        )

        if (safetyEvaluation.status == ReelSafetyStatus.BLOCKED) {
            return ReelGenerationOutcome.BlockedBySafety(safetyEvaluation.reasons)
        }

        // 13. Determine generation status based on safety and source verification
        val initialGenerationStatus = when {
            safetyEvaluation.needsReview -> ReelGenerationStatus.REVIEW_REQUIRED
            topic.verificationStatus == VerificationStatus.NEEDS_REVIEW.name -> ReelGenerationStatus.REVIEW_REQUIRED
            else -> ReelGenerationStatus.DRAFT
        }

        // Ensure sourceUrl is strictly the verified source URL
        val finalSourceUrl = if (topic.sourceUrl.isNotBlank()) topic.sourceUrl else aiReturnedUrl

        val draft = ReelDraft(
            id = UUID.randomUUID().toString(),
            sourceOpportunityId = topic.sourceOpportunityId,
            sourceContentId = topic.sourceContentId,
            reelType = parsedReelType,
            title = if (title.isNotBlank()) title else topic.topic,
            hook = hook,
            durationSeconds = duration,
            scenes = scenes,
            voiceover = voiceover,
            caption = caption,
            hashtags = hashtags,
            sourceName = topic.sourceName,
            sourceUrl = finalSourceUrl,
            verificationStatus = topic.verificationStatus,
            safetyStatus = safetyEvaluation.status,
            generationStatus = initialGenerationStatus,
            language = language,
            contentHash = contentHash
        )

        // Persist to Room if DAO provided
        if (reelDao != null) {
            try {
                reelDao.insertReel(ReelDraftEntity.fromDraft(draft))
            } catch (_: Exception) {
                // Ignore persistence errors in mock/unit contexts
            }
        }

        return ReelGenerationOutcome.Success(draft)
    }

    /**
     * Checks if the AI fabricated a deadline date not present in the verified source context.
     */
    private fun checkForFabricatedDeadlines(
        topic: ReelTopic,
        scenes: List<ReelScene>,
        voiceover: String,
        caption: String
    ): String? {
        // If topic has no deadline specified, check if AI claimed a specific fabricated deadline date
        val combined = (scenes.joinToString(" ") { it.onScreenText + " " + it.voiceoverText } + " " + voiceover + " " + caption).lowercase(Locale.ROOT)
        val sourceContext = "${topic.context} ${topic.deadline ?: ""}".lowercase(Locale.ROOT)

        val datePattern = Regex("\\b(\\d{1,2}(?:st|nd|rd|th)?\\s+(?:january|february|march|april|may|june|july|august|september|october|november|december)\\s+\\d{4})\\b", RegexOption.IGNORE_CASE)
        val matches = datePattern.findAll(combined)

        for (match in matches) {
            val dateStr = match.value.lowercase(Locale.ROOT)
            if (!sourceContext.contains(dateStr)) {
                return "Fabricated deadline date detected: '$dateStr' does not exist in the verified source."
            }
        }
        return null
    }

    private fun parseHashtags(jsonArray: JSONArray?): List<String> {
        val list = mutableListOf<String>()
        if (jsonArray == null) return list
        for (i in 0 until jsonArray.length()) {
            val tag = jsonArray.optString(i, "")
                .replace("#", "")
                .trim()
            if (tag.isNotBlank()) {
                list.add(tag)
            }
        }
        return list
    }

    private fun cleanJsonString(raw: String): String {
        return raw.trim()
            .removePrefix("```json")
            .removePrefix("```JSON")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
    }
}
