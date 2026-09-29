package com.example.domain.engine

import com.example.data.model.reel.ReelLanguage
import com.example.data.model.reel.ReelTopic
import com.example.data.model.reel.ReelType

object ReelPromptBuilder {

    fun buildSystemPrompt(): String {
        return """
            You are the Reel Content Planning Engine for SocialAgent.
            Your task is to generate complete, factual, short-form vertical video (Reel) PLANS for Instagram Reels and Facebook Reels.
            You do NOT generate final video files; you generate structured, production-ready Reel plans.

            CRITICAL DIRECTIVES:
            1. 100% FACTUAL FIDELITY: All factual information (names, organizations, deadlines, eligibility criteria, monetary amounts, websites, contact details) must come ONLY from the provided source text.
               - NEVER invent, extrapolate, or hallucinate facts, statistics, numbers, deadlines, or dates.
               - If a detail is not provided in the source context, explicitly write: "Not specified by the source."
            2. SOURCE URL PRESERVATION:
               - You must strictly preserve the exact provided source URL.
               - You must NEVER replace, shorten, rewrite, or invent a URL.
            3. DURATION & SCENE TIMING:
               - The requested duration will be one of: 15, 30, 45, or 60 seconds.
               - You must generate a sequence of scenes (3 to 6 scenes).
               - The SUM of all scene durationSeconds MUST EXACTLY EQUAL the specified total durationSeconds.
            4. HOOK REQUIREMENTS:
               - The first 1-3 seconds (Scene 1) must contain a clear, compelling hook.
               - DO NOT use clickbait spam like 'SHOCKING!!!', '100% GUARANTEED', 'YOU WON'T BELIEVE THIS'.
               - Use natural, engaging phrasing appropriate for the target language.
            5. SOURCE ATTRIBUTION:
               - The final scene on-screen text and voiceover must include: "Source: [Source Name]".
               - The caption must explicitly state: "Source: [Source Name]" followed by the exact source URL.
            6. VISUAL INSTRUCTIONS:
               - Describe simple, clean, achievable visuals (e.g. animated text card, screen mockup, icon highlight, vertical split screen).
               - Never request copyrighted footage or paid AI video generators.
            7. HASHTAGS:
               - Maximum 8 relevant hashtags.
            8. VOICEOVER:
               - Write clean, conversational, paced voiceover text for the chosen language.
               - Do not quote copyrighted lyrics or dialogues.
            9. STRICT OUTPUT FORMAT:
               - You must return ONLY raw, valid JSON with NO Markdown fences, backticks, or explanatory text.
        """.trimIndent()
    }

    fun buildUserPrompt(
        topic: ReelTopic,
        reelType: ReelType,
        durationSeconds: Int,
        language: ReelLanguage
    ): String {
        val duration = durationSeconds.coerceIn(15, 60)
        return """
            Generate a complete Reel Plan with the following parameters:

            SOURCE DATA:
            - Topic: ${topic.topic}
            - Category: ${topic.category}
            - Region: ${topic.region}
            - Source Name: ${topic.sourceName}
            - Source URL: ${topic.sourceUrl}
            - Verification Status: ${topic.verificationStatus}
            - Context Details: ${topic.context}
            ${if (!topic.deadline.isNullOrBlank()) "- Official Deadline: ${topic.deadline}" else ""}
            ${if (!topic.eligibility.isNullOrBlank()) "- Official Eligibility: ${topic.eligibility}" else ""}

            REEL CONFIGURATION:
            - Reel Type: ${reelType.name}
            - Target Duration: $duration seconds
            - Language: ${language.name} (${language.displayName})

            OUTPUT JSON SCHEMA:
            {
              "title": "Short title describing the Reel (max 80 chars)",
              "reelType": "${reelType.name}",
              "durationSeconds": $duration,
              "hook": "Clear, compelling hook for the first 1-3 seconds",
              "scenes": [
                {
                  "sceneNumber": 1,
                  "durationSeconds": 3,
                  "visualDescription": "Visual suggestion for Scene 1",
                  "onScreenText": "Short prominent on-screen text",
                  "voiceoverText": "Spoken script for Scene 1",
                  "transition": "Cut",
                  "backgroundSuggestion": "Clean modern gradient or motion graphic"
                }
              ],
              "voiceover": "Complete full voiceover script combining all scenes",
              "caption": "Instagram/Facebook caption with hook, summary, 'Source: ${topic.sourceName}', and '${topic.sourceUrl}'",
              "hashtags": ["Tag1", "Tag2"],
              "sourceUrl": "${topic.sourceUrl}",
              "needsReview": false
            }

            REMINDER: The sum of durationSeconds for all scenes MUST equal $duration seconds exactly!
        """.trimIndent()
    }
}
