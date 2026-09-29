package com.example.domain.engine

import com.example.data.model.meme.MemeSafetyStatus
import java.util.Locale

data class MemeSafetyEvaluation(
    val status: MemeSafetyStatus,
    val reasons: List<String>,
    val isPolitical: Boolean = false
) {
    val isBlocked: Boolean get() = status == MemeSafetyStatus.BLOCKED
    val isSafe: Boolean get() = status == MemeSafetyStatus.SAFE
    val needsReview: Boolean get() = status == MemeSafetyStatus.NEEDS_REVIEW
}

class MemeSafetyEngine {

    companion object {
        // Severe prohibited terms (hate, violence, exploitation, threats, slurs, fraud)
        private val BLOCKED_TERMS = listOf(
            "kill yourself", "die in a fire", "hang yourself", "massacre",
            "terrorist", "bomb threat", "assassinate", "behead", "slaughter",
            "child abuse", "pedo", "cp leak", "underage sex", "leaked nudes",
            "ponzi scheme", "guaranteed 1000% return", "crypto hack", "bank phishing",
            "defraud", "fake identity", "impersonate officer",
            "subhuman", "genocide", "ethnic cleansing", "rape"
        )

        // Hate speech / protected characteristic disparagement
        private val HATE_SPEECH_PATTERNS = listOf(
            Regex("\\b(hate|despise|destroy)\\s+(all\\s+)?(muslims|hindus|christians|sikhs|tribals|bodos|ahoms|bengalis|biharis)\\b", RegexOption.IGNORE_CASE),
            Regex("\\b(chinki|bangladeshi\\s+infiltrator|illegal\\s+immigrant)\\b", RegexOption.IGNORE_CASE),
            Regex("\\b(untouchable|casteist\\s+slur)\\b", RegexOption.IGNORE_CASE),
            Regex("\\b(women\\s+belong\\s+in\\s+kitchen|disabled\\s+freak)\\b", RegexOption.IGNORE_CASE)
        )

        // Political manipulation / campaign propaganda / electoral persuasion
        private val POLITICAL_CAMPAIGN_PATTERNS = listOf(
            Regex("\\b(vote\\s+for|vote\\s+against|cast\\s+your\\s+vote\\s+for|don't\\s+vote\\s+for)\\b", RegexOption.IGNORE_CASE),
            Regex("\\b(elect\\s+|defeat\\s+|overthrow\\s+the\\s+government)\\b", RegexOption.IGNORE_CASE),
            Regex("\\b(propaganda|political\\s+bribe|rigged\\s+election|stolen\\s+votes)\\b", RegexOption.IGNORE_CASE),
            Regex("\\b(join\\s+our\\s+rally|party\\s+cadre|political\\s+manifesto)\\b", RegexOption.IGNORE_CASE)
        )

        // Political & current-affairs topic identifiers (Civic topics must default to NEEDS_REVIEW)
        private val POLITICAL_TOPIC_TERMS = listOf(
            "election", "mla", "mp", "chief minister", "prime minister", "cabinet",
            "bjp", "congress", "aap", "agp", "aiudf", "bpf", "opposition", "ruling party",
            "parliament", "assembly", "governor", "ordinance", "bill passed", "protest march"
        )

        // Review trigger indicators (sensitive topics, unverified claims, spicy jokes)
        private val REVIEW_TRIGGER_TERMS = listOf(
            "corruption", "bribe", "scandal", "court hearing", "investigation",
            "arrested", "fir", "police raid", "allegation", "controversy"
        )

        private fun containsTermAsWord(text: String, term: String): Boolean {
            val pattern = Regex("\\b${Regex.escape(term)}\\b", RegexOption.IGNORE_CASE)
            return pattern.containsMatchIn(text)
        }
    }

    /**
     * Evaluates a meme draft across all text components and source metadata.
     */
    fun evaluate(
        topic: String,
        setupText: String,
        punchlineText: String,
        caption: String,
        isPoliticalExplicit: Boolean = false
    ): MemeSafetyEvaluation {
        val combinedText = "$topic $setupText $punchlineText $caption".lowercase(Locale.ROOT)
        val reasons = mutableListOf<String>()

        // 1. Critical Harms & Extreme Safety Blocks
        for (term in BLOCKED_TERMS) {
            if (containsTermAsWord(combinedText, term) || combinedText.contains(term)) {
                reasons.add("Contains strictly prohibited extreme content or threat: '$term'")
                return MemeSafetyEvaluation(MemeSafetyStatus.BLOCKED, reasons)
            }
        }

        // 2. Hate speech / Protected Characteristic Harms
        for (pattern in HATE_SPEECH_PATTERNS) {
            if (pattern.containsMatchIn(combinedText)) {
                reasons.add("Violates anti-discrimination policy: targeting protected group or ethnicity.")
                return MemeSafetyEvaluation(MemeSafetyStatus.BLOCKED, reasons)
            }
        }

        // 3. Political Campaigning & Voter Persuasion Blocks
        for (pattern in POLITICAL_CAMPAIGN_PATTERNS) {
            if (pattern.containsMatchIn(combinedText)) {
                reasons.add("Electoral persuasion, campaign propaganda, or voter manipulation is prohibited.")
                return MemeSafetyEvaluation(MemeSafetyStatus.BLOCKED, reasons)
            }
        }

        // 4. Political Topic Detection (Defaults to NEEDS_REVIEW) - matched with whole-word boundaries
        val isPoliticalTopic = isPoliticalExplicit || POLITICAL_TOPIC_TERMS.any { containsTermAsWord(combinedText, it) }
        if (isPoliticalTopic) {
            reasons.add("Political or civic affairs context detected. Subject to mandatory human editorial review.")
            return MemeSafetyEvaluation(
                status = MemeSafetyStatus.NEEDS_REVIEW,
                reasons = reasons,
                isPolitical = true
            )
        }

        // 5. Sensitive Allegations & Review Triggers - matched with whole-word boundaries
        val hasReviewTrigger = REVIEW_TRIGGER_TERMS.any { containsTermAsWord(combinedText, it) }
        if (hasReviewTrigger) {
            reasons.add("Contains sensitive claims, legal controversy, or allegations requiring human verification.")
            return MemeSafetyEvaluation(
                status = MemeSafetyStatus.NEEDS_REVIEW,
                reasons = reasons,
                isPolitical = false
            )
        }

        // 6. Clean and wholesome
        return MemeSafetyEvaluation(
            status = MemeSafetyStatus.SAFE,
            reasons = emptyList(),
            isPolitical = false
        )
    }
}
