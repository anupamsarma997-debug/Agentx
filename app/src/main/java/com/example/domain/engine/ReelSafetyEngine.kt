package com.example.domain.engine

import com.example.data.model.reel.ReelSafetyStatus
import com.example.data.model.reel.ReelScene
import java.util.Locale

data class ReelSafetyEvaluation(
    val status: ReelSafetyStatus,
    val reasons: List<String>,
    val isPolitical: Boolean = false
) {
    val isSafe: Boolean get() = status == ReelSafetyStatus.SAFE
    val needsReview: Boolean get() = status == ReelSafetyStatus.NEEDS_REVIEW
    val isBlocked: Boolean get() = status == ReelSafetyStatus.BLOCKED
}

class ReelSafetyEngine {

    companion object {
        // Severe prohibited terms (hate, violence, exploitation, threats, slurs, scams)
        private val BLOCKED_TERMS = listOf(
            "kill yourself", "die in a fire", "hang yourself", "massacre",
            "terrorist", "bomb threat", "assassinate", "behead", "slaughter",
            "child abuse", "pedo", "cp leak", "underage sex", "leaked nudes",
            "ponzi scheme", "guaranteed 1000% return", "crypto hack", "bank phishing",
            "defraud", "fake identity", "impersonate officer", "credit card hack",
            "subhuman", "genocide", "ethnic cleansing", "rape", "doxx", "doxxing",
            "pipe bomb", "molotov", "suicide guide", "how to kill"
        )

        // Hate speech / Protected Characteristic Harms
        private val HATE_SPEECH_PATTERNS = listOf(
            Regex("\\b(hate|despise|destroy)\\s+(all\\s+)?(muslims|hindus|christians|sikhs|tribals|bodos|ahoms|bengalis|biharis)\\b", RegexOption.IGNORE_CASE),
            Regex("\\b(chinki|bangladeshi\\s+infiltrator|illegal\\s+immigrant)\\b", RegexOption.IGNORE_CASE),
            Regex("\\b(untouchable|casteist\\s+slur)\\b", RegexOption.IGNORE_CASE),
            Regex("\\b(women\\s+belong\\s+in\\s+kitchen|disabled\\s+freak)\\b", RegexOption.IGNORE_CASE)
        )

        // Political manipulation / campaign propaganda / electoral persuasion / candidate attacks
        private val POLITICAL_CAMPAIGN_PATTERNS = listOf(
            Regex("\\b(vote\\s+for|vote\\s+against|cast\\s+your\\s+vote\\s+for|don't\\s+vote\\s+for)\\b", RegexOption.IGNORE_CASE),
            Regex("\\b(elect\\s+|defeat\\s+|overthrow\\s+the\\s+government)\\b", RegexOption.IGNORE_CASE),
            Regex("\\b(propaganda|political\\s+bribe|rigged\\s+election|stolen\\s+votes)\\b", RegexOption.IGNORE_CASE),
            Regex("\\b(join\\s+our\\s+rally|party\\s+cadre|political\\s+manifesto)\\b", RegexOption.IGNORE_CASE),
            Regex("\\b(candidate\\s+is\\s+corrupt|corrupt\\s+politician\\s+exposed)\\b", RegexOption.IGNORE_CASE)
        )

        // Political & current-affairs topic identifiers (Civic topics must default to NEEDS_REVIEW)
        private val POLITICAL_TOPIC_TERMS = listOf(
            "election", "mla", "mp", "chief minister", "prime minister", "cabinet",
            "bjp", "congress", "aap", "agp", "aiudf", "bpf", "opposition", "ruling party",
            "parliament", "assembly", "governor", "ordinance", "bill passed", "protest march",
            "lok sabha", "rajya sabha", "vidhan sabha"
        )

        // Review trigger indicators (sensitive topics, unverified claims, spicy jokes, defamatory claims)
        private val REVIEW_TRIGGER_TERMS = listOf(
            "corruption", "bribe", "scandal", "court hearing", "investigation",
            "arrested", "fir", "police raid", "allegation", "controversy", "defamation",
            "unverified claim", "leaked audio", "scam exposed"
        )

        private fun containsTermAsWord(text: String, term: String): Boolean {
            val pattern = Regex("\\b${Regex.escape(term)}\\b", RegexOption.IGNORE_CASE)
            return pattern.containsMatchIn(text)
        }
    }

    /**
     * Evaluates a reel plan across hook, scenes, voiceover, caption, and topic.
     */
    fun evaluate(
        topic: String,
        hook: String,
        scenes: List<ReelScene>,
        voiceover: String,
        caption: String,
        isPoliticalExplicit: Boolean = false
    ): ReelSafetyEvaluation {
        val scenesText = scenes.joinToString(" ") { "${it.visualDescription} ${it.onScreenText} ${it.voiceoverText}" }
        val combinedText = "$topic $hook $scenesText $voiceover $caption".lowercase(Locale.ROOT)
        val reasons = mutableListOf<String>()

        // 1. Critical Harms & Extreme Safety Blocks
        for (term in BLOCKED_TERMS) {
            if (containsTermAsWord(combinedText, term) || combinedText.contains(term)) {
                reasons.add("Contains strictly prohibited extreme content, threat, or scam: '$term'")
                return ReelSafetyEvaluation(ReelSafetyStatus.BLOCKED, reasons)
            }
        }

        // 2. Hate speech / Protected Characteristic Harms
        for (pattern in HATE_SPEECH_PATTERNS) {
            if (pattern.containsMatchIn(combinedText)) {
                reasons.add("Violates anti-discrimination policy: targeting protected group, community, or ethnicity.")
                return ReelSafetyEvaluation(ReelSafetyStatus.BLOCKED, reasons)
            }
        }

        // 3. Political Campaigning & Voter Persuasion Blocks
        for (pattern in POLITICAL_CAMPAIGN_PATTERNS) {
            if (pattern.containsMatchIn(combinedText)) {
                reasons.add("Electoral persuasion, campaign propaganda, candidate attack, or voter manipulation is strictly blocked.")
                return ReelSafetyEvaluation(ReelSafetyStatus.BLOCKED, reasons)
            }
        }

        // 4. Political Topic Detection (Defaults to NEEDS_REVIEW) - matched with whole-word boundaries
        val isPoliticalTopic = isPoliticalExplicit || POLITICAL_TOPIC_TERMS.any { containsTermAsWord(combinedText, it) }
        if (isPoliticalTopic) {
            reasons.add("Political or civic affairs context detected. Subject to mandatory human editorial review.")
            return ReelSafetyEvaluation(
                status = ReelSafetyStatus.NEEDS_REVIEW,
                reasons = reasons,
                isPolitical = true
            )
        }

        // 5. Sensitive Allegations & Review Triggers - matched with whole-word boundaries
        val hasReviewTrigger = REVIEW_TRIGGER_TERMS.any { containsTermAsWord(combinedText, it) }
        if (hasReviewTrigger) {
            reasons.add("Contains sensitive claims, legal controversy, or allegations requiring human verification.")
            return ReelSafetyEvaluation(
                status = ReelSafetyStatus.NEEDS_REVIEW,
                reasons = reasons,
                isPolitical = false
            )
        }

        // 6. Clean and safe
        return ReelSafetyEvaluation(
            status = ReelSafetyStatus.SAFE,
            reasons = emptyList(),
            isPolitical = false
        )
    }
}
