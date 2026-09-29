package com.example.domain.automation

import com.example.data.local.settings.AppSettings

/**
 * FreeTierGuard ensures safety under FREE_MODE.
 *
 * Guarantees:
 * - Generation stops as soon as daily limits are reached.
 * - No paid API upgrade is ever permitted.
 * - Safely stops when quota limits or targets are reached.
 */
class FreeTierGuard {

    fun isFreeModeEnabled(settings: AppSettings): Boolean {
        return settings.freeMode
    }

    fun getDailyPostCount(settings: AppSettings): Int {
        return settings.todayPostCount
    }

    fun getDailyReelCount(settings: AppSettings): Int {
        return settings.todayReelCount
    }

    fun canGeneratePost(settings: AppSettings): GenerationDecision {
        if (!settings.freeMode) {
            // Even outside free mode, we respect configured bounds unless explicitly configured
            return if (settings.todayPostCount >= settings.dailyPostTarget) {
                GenerationDecision.TargetReached(
                    message = "Daily post target of ${settings.dailyPostTarget} reached."
                )
            } else {
                GenerationDecision.Allowed
            }
        }

        // Under FREE_MODE
        if (settings.todayPostCount >= settings.dailyPostTarget) {
            return GenerationDecision.QuotaExhausted(
                reason = "Free-tier daily limit reached: ${settings.todayPostCount}/${settings.dailyPostTarget} posts generated today. Further generation safely suspended."
            )
        }

        return GenerationDecision.Allowed
    }

    fun canGenerateReel(settings: AppSettings): GenerationDecision {
        if (!settings.freeMode) {
            return if (settings.todayReelCount >= settings.dailyReelTarget) {
                GenerationDecision.TargetReached(
                    message = "Daily reel target of ${settings.dailyReelTarget} reached."
                )
            } else {
                GenerationDecision.Allowed
            }
        }

        // Under FREE_MODE
        if (settings.todayReelCount >= settings.dailyReelTarget) {
            return GenerationDecision.QuotaExhausted(
                reason = "Free-tier daily limit reached: ${settings.todayReelCount}/${settings.dailyReelTarget} reels generated today. Further generation safely suspended."
            )
        }

        return GenerationDecision.Allowed
    }

    fun canRunAutomation(settings: AppSettings, isWindowOpen: Boolean): AutomationGateResult {
        if (!settings.automationEnabled) {
            return AutomationGateResult.Blocked(reason = "Automation is turned OFF.")
        }
        if (!isWindowOpen) {
            return AutomationGateResult.Blocked(
                reason = "Outside active automation window (${formatTime(settings.automationStartHour, settings.automationStartMinute)} – ${formatTime(settings.automationEndHour, settings.automationEndMinute)} ${settings.timezone})."
            )
        }
        val postAllowed = canGeneratePost(settings) is GenerationDecision.Allowed
        val reelAllowed = canGenerateReel(settings) is GenerationDecision.Allowed

        if (!postAllowed && !reelAllowed) {
            return AutomationGateResult.Blocked(
                reason = "Today's content targets (${settings.dailyPostTarget} posts, ${settings.dailyReelTarget} reels) are complete."
            )
        }

        return AutomationGateResult.Ready
    }

    private fun formatTime(hour: Int, minute: Int): String {
        val ampm = if (hour >= 12) "PM" else "AM"
        val displayHour = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        return String.format("%02d:%02d %s", displayHour, minute, ampm)
    }
}

sealed interface GenerationDecision {
    data object Allowed : GenerationDecision
    data class TargetReached(val message: String) : GenerationDecision
    data class QuotaExhausted(val reason: String) : GenerationDecision
}

sealed interface AutomationGateResult {
    data object Ready : AutomationGateResult
    data class Blocked(val reason: String) : AutomationGateResult
}
