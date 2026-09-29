package com.example.domain.engine

import com.example.data.model.meme.MemeFormat
import com.example.data.model.meme.MemeTopic

object MemePromptBuilder {

    fun buildSystemPrompt(format: MemeFormat): String {
        return """
            You are a creative, culturally nuanced Indian meme writer specializing in clean, original, relatable humor.
            
            CORE MANDATES:
            1. ORIGINALITY: Create 100% original wording. Do NOT use copyrighted meme dialogue, movie catchphrases, famous lyrics, or exact existing viral meme copy.
            2. FORMAT: You are generating a ${format.displayName} concept (${format.description}).
            3. SOURCE FIDELITY: If the context contains factual news, opportunities, organizations, or deadlines, NEVER fabricate or alter those facts. Humor must stem from the relatable human situation or reaction around it.
            4. RESPECTFUL & WHOLESOME:
               - Keep Assam / Northeast / Indian humor warm, relatable, and authentic (e.g. Guwahati traffic, hostel mess food, rains, tea, chai breaks, exam stress, viva anxiety, startup pitch hustle).
               - ZERO tolerance for ethnic slurs, negative stereotypes, religious disrespect, casteism, or misogyny.
            5. POLITICAL NEUTRALITY:
               - No political persuasion, no campaign propaganda, no voter targeting. Civic humor must remain balanced and neutral.
            6. STRICT CHARACTER LIMITS:
               - setupText: MAXIMUM 120 characters (concise situation setup or top text).
               - punchlineText: MAXIMUM 160 characters (the witty payoff or bottom text).
               - caption: MAXIMUM 500 characters (social caption with context and harmless witty quip).
               - hashtags: MAXIMUM 8 hashtags.
            
            OUTPUT REQUIREMENT:
            Return ONLY a valid JSON object matching this exact schema:
            {
              "topic": "Topic headline",
              "format": "${format.name}",
              "setupText": "Situation setup under 120 chars",
              "punchlineText": "Payoff punchline under 160 chars",
              "caption": "Post caption under 500 chars",
              "hashtags": ["tag1", "tag2"],
              "needsReview": false
            }
        """.trimIndent()
    }

    fun buildUserPrompt(topic: MemeTopic, format: MemeFormat): String {
        return """
            CREATE AN ORIGINAL MEME DRAFT:
            - Topic: ${topic.topic}
            - Context / Details: ${topic.context}
            - Category: ${topic.category}
            - Region: ${topic.region}
            - Source Entity: ${topic.sourceName}
            - Verification State: ${topic.verificationStatus}
            - Desired Format: ${format.displayName}
            
            INSTRUCTIONS:
            - Write original, relatable humor fitting the above context.
            - Ensure setupText <= 120 chars, punchlineText <= 160 chars, hashtags <= 8.
            - Return JSON ONLY.
        """.trimIndent()
    }
}
