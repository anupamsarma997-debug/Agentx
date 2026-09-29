package com.example.domain.automation

import com.example.data.local.settings.AppSettings
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Calculates whether the current time is inside the configured automation window (e.g. 10:00 AM - 11:00 AM IST).
 * Pure calculation on invocation; no persistent background loops or battery drains.
 */
class DailyAutomationManager {

    companion object {
        const val DEFAULT_TIMEZONE = "Asia/Kolkata"
    }

    /**
     * Checks if the automation window is currently open based on current time in the configured timezone.
     */
    fun isAutomationWindowOpen(settings: AppSettings): Boolean {
        return try {
            val zoneId = parseZoneId(settings.timezone)
            val nowInZone = ZonedDateTime.now(zoneId).toLocalTime()

            val startTime = LocalTime.of(settings.automationStartHour, settings.automationStartMinute)
            val endTime = LocalTime.of(settings.automationEndHour, settings.automationEndMinute)

            if (startTime.isBefore(endTime)) {
                !nowInZone.isBefore(startTime) && nowInZone.isBefore(endTime)
            } else {
                // Overnight window handling (e.g., 23:00 to 01:00)
                !nowInZone.isBefore(startTime) || nowInZone.isBefore(endTime)
            }
        } catch (_: Exception) {
            // Fallback safe calculation with default Asia/Kolkata
            try {
                val nowInZone = ZonedDateTime.now(ZoneId.of(DEFAULT_TIMEZONE)).toLocalTime()
                val startTime = LocalTime.of(10, 0)
                val endTime = LocalTime.of(11, 0)
                !nowInZone.isBefore(startTime) && nowInZone.isBefore(endTime)
            } catch (_: Exception) {
                false
            }
        }
    }

    fun isAutomationEnabled(settings: AppSettings): Boolean {
        return settings.automationEnabled
    }

    /**
     * Gets formatted status of the current automation window and current time in configured zone.
     */
    fun getWindowStatusDescription(settings: AppSettings): WindowStatus {
        val isOpen = isAutomationWindowOpen(settings)
        val zoneId = parseZoneId(settings.timezone)
        val currentTime = try {
            ZonedDateTime.now(zoneId).toLocalTime()
        } catch (_: Exception) {
            LocalTime.now()
        }

        val startFormatted = formatTime(settings.automationStartHour, settings.automationStartMinute)
        val endFormatted = formatTime(settings.automationEndHour, settings.automationEndMinute)
        val windowText = "$startFormatted – $endFormatted (${getZoneAbbreviation(settings.timezone)})"

        return WindowStatus(
            isOpen = isOpen,
            windowText = windowText,
            currentTimeFormatted = formatLocalTime(currentTime),
            timezone = settings.timezone
        )
    }

    private fun parseZoneId(zoneString: String): ZoneId {
        return try {
            ZoneId.of(zoneString)
        } catch (_: Exception) {
            ZoneId.of(DEFAULT_TIMEZONE)
        }
    }

    private fun getZoneAbbreviation(zoneString: String): String {
        return if (zoneString == "Asia/Kolkata" || zoneString == "Asia/Calcutta") {
            "IST"
        } else {
            zoneString
        }
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

    private fun formatLocalTime(time: LocalTime): String {
        val hour = time.hour
        val minute = time.minute
        val second = time.second
        val ampm = if (hour >= 12) "PM" else "AM"
        val displayHour = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        return String.format("%02d:%02d:%02d %s", displayHour, minute, second, ampm)
    }
}

data class WindowStatus(
    val isOpen: Boolean,
    val windowText: String,
    val currentTimeFormatted: String,
    val timezone: String
)
