package com.example.domain.engine

import java.util.Locale

enum class SafetyLevel {
    SAFE,
    NEEDS_REVIEW,
    BLOCKED
}

data class ContentSafetyEvaluation(
    val level: SafetyLevel,
    val reasons: List<String> = emptyList(),
    val isPoliticalPersuasion: Boolean = false,
    val isPoliticalAffairs: Boolean = false,
    val isProhibited: Boolean = false
) {
    val isSafe: Boolean get() = level == SafetyLevel.SAFE
    val needsReview: Boolean get() = level == SafetyLevel.NEEDS_REVIEW
    val isBlocked: Boolean get() = level == SafetyLevel.BLOCKED
}

class ContentSafetyEngine {

    companion object {
        // Severe prohibited terms (violence, hate, threats, sexual exploitation, minors, scams, extremist)
        private val BLOCKED_TERMS = listOf(
            "kill yourself", "die in a fire", "hang yourself", "massacre",
            "terrorist", "bomb threat", "assassinate", "behead", "slaughter",
            "child abuse", "pedo", "cp leak", "underage sex", "leaked nudes",
            "ponzi scheme", "guaranteed 1000% return", "crypto hack", "bank phishing",
            "defraud", "fake identity", "impersonate officer", "credit card hack",
            "subhuman", "genocide", "ethnic cleansing", "rape", "doxx", "doxxing",
            "pipe bomb", "molotov", "suicide guide", "how to kill",
            "phishing credentials", "steal password", "fake bank portal"
        )

        // Hate speech and protected characteristic harm
        private val HATE_SPEECH_PATTERNS = listOf(
            Regex("\\b(hate|despise|destroy)\\s+(all\\s+)?(muslims|hindus|christians|sikhs|tribals|bodos|ahoms|bengalis|biharis)\\b", RegexOption.IGNORE_CASE),
            Regex("\\b(chinki|bangladeshi\\s+infiltrator|illegal\\s+immigrant)\\b", RegexOption.IGNORE_CASE),
            Regex("\\b(untouchable|casteist\\s+slur)\\b", RegexOption.IGNORE_CASE),
            Regex("\\b(women\\s+belong\\s+in\\s+kitchen|disabled\\s+freak)\\b", RegexOption.IGNORE_CASE)
        )

        // Political manipulation / campaign propaganda / voter persuasion / candidate promotion or attacks
        private val POLITICAL_CAMPAIGN_PATTERNS = listOf(
            Regex("\\b(vote\\s+for|vote\\s+against|cast\\s+your\\s+vote\\s+for|don't\\s+vote\\s+for)\\b", RegexOption.IGNORE_CASE),
            Regex("\\b(elect\\s+|defeat\\s+|overthrow\\s+the\\s+government)\\b", RegexOption.IGNORE_CASE),
            Regex("\\b(propaganda|political\\s+bribe|rigged\\s+election|stolen\\s+votes)\\b", RegexOption.IGNORE_CASE),
            Regex("\\b(join\\s+our\\s+rally|party\\s+cadre|political\\s+manifesto)\\b", RegexOption.IGNORE_CASE),
            Regex("\\b(candidate\\s+is\\s+corrupt|corrupt\\s+politician\\s+exposed)\\b", RegexOption.IGNORE_CASE),
            Regex("\\b(vote\\s+bank|demographic\\s+threat|demographic\\s+takeover)\\b", RegexOption.IGNORE_CASE)
        )

        // Political parties and candidate terms that trigger human review (unless it is solely a public government job/grant announcement)
        private val POLITICAL_ENTITIES = listOf(
            "bjp", "congress", "aap", "agp", "aiudf", "bpf", "tmc", "cpi", "cpim",
            "mla candidate", "mp candidate", "election campaign", "election rally",
            "political manifesto", "party president", "opposition leader"
        )

        // General political & electoral affairs terms
        private val POLITICAL_AFFAIRS_TERMS = listOf(
            "election", "elections", "vidhan sabha", "lok sabha", "rajya sabha",
            "by-election", "cabinet minister", "chief minister", "prime minister",
            "assembly session", "ordinance", "bill passed", "controversial policy"
        )

        // Terms indicating public civic opportunities (NOT political persuasion)
        private val CIVIC_OPPORTUNITY_TERMS = listOf(
            "recruitment", "vacancy", "admit card", "examination", "scholarship",
            "seed grant", "subsidy", "fellowship", "portal", "eligibility",
            "apply online", "last date to apply", "application fee", "syllabus", "internship"
        )

        // Sensitive topics requiring human editorial review
        private val SENSITIVE_REVIEW_TERMS = listOf(
            "corruption", "bribe", "scandal", "court hearing", "investigation",
            "arrested", "fir", "police raid", "allegation", "controversy", "defamation",
            "unverified claim", "leaked audio", "scam exposed", "border dispute"
        )

        private fun containsWord(text: String, word: String): Boolean {
            val pattern = Regex("\\b${Regex.escape(word)}\\b", RegexOption.IGNORE_CASE)
            return pattern.containsMatchIn(text)
        }
    }

    /**
     * Unified evaluation across title, body, caption, hashtags, and topic context.
     */
    fun evaluate(
        title: String,
        body: String = "",
        caption: String = "",
        hashtags: List<String> = emptyList(),
        sourceName: String = "",
        category: String = ""
    ): ContentSafetyEvaluation {
        val fullText = "$title $body $caption ${hashtags.joinToString(" ")} $sourceName".lowercase(Locale.ROOT)
        val reasons = mutableListOf<String>()

        // 1. Prohibited terms check
        for (term in BLOCKED_TERMS) {
            if (containsWord(fullText, term)) {
                reasons.add("Contains severe prohibited/harmful term: '$term'")
            }
        }

        // 2. Hate speech patterns check
        for (pattern in HATE_SPEECH_PATTERNS) {
            if (pattern.containsMatchIn(fullText)) {
                reasons.add("Violates hate speech / community safety standard: '${pattern.pattern}'")
            }
        }

        // 3. Political campaign propaganda / voter persuasion check -> BLOCKED
        for (pattern in POLITICAL_CAMPAIGN_PATTERNS) {
            if (pattern.containsMatchIn(fullText)) {
                reasons.add("Electoral persuasion, campaign propaganda, or voter targeting detected: '${pattern.pattern}'")
            }
        }

        if (reasons.isNotEmpty()) {
            return ContentSafetyEvaluation(
                level = SafetyLevel.BLOCKED,
                reasons = reasons,
                isPoliticalPersuasion = true,
                isProhibited = true
            )
        }

        // 4. Check for Political Parties / Candidates
        val hasPoliticalEntity = POLITICAL_ENTITIES.any { containsWord(fullText, it) }
        val hasPoliticalAffairs = POLITICAL_AFFAIRS_TERMS.any { containsWord(fullText, it) }

        // Check if this is a standard public administration opportunity (e.g. Government Job, Assam Scholarship)
        val isCivicOpportunity = CIVIC_OPPORTUNITY_TERMS.any { containsWord(fullText, it) } ||
                category.equals("GOVERNMENT_JOB", ignoreCase = true) ||
                category.equals("SCHOLARSHIP", ignoreCase = true) ||
                category.equals("GRANT", ignoreCase = true) ||
                category.equals("JOB", ignoreCase = true)

        if (hasPoliticalEntity) {
            // Political party/candidate mentioned -> MUST require human review
            return ContentSafetyEvaluation(
                level = SafetyLevel.NEEDS_REVIEW,
                reasons = listOf("Mentions political party or electoral entity; human editorial review required."),
                isPoliticalAffairs = true
            )
        }

        if (hasPoliticalAffairs && !isCivicOpportunity) {
            // General politics/elections without being a pure job/scholarship
            return ContentSafetyEvaluation(
                level = SafetyLevel.NEEDS_REVIEW,
                reasons = listOf("Covers civic/political current affairs; editorial neutrality review required."),
                isPoliticalAffairs = true
            )
        }

        // 5. Sensitive investigation / dispute / defamatory allegations -> NEEDS_REVIEW
        for (term in SENSITIVE_REVIEW_TERMS) {
            if (containsWord(fullText, term)) {
                return ContentSafetyEvaluation(
                    level = SafetyLevel.NEEDS_REVIEW,
                    reasons = listOf("Contains sensitive investigative/controversial keyword: '$term'"),
                    isPoliticalAffairs = false
                )
            }
        }

        return ContentSafetyEvaluation(
            level = SafetyLevel.SAFE,
            reasons = emptyList()
        )
    }
}
