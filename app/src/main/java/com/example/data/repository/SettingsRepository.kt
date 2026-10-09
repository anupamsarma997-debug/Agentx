package com.example.data.repository

import com.example.data.local.settings.AppSettings
import com.example.data.local.settings.SettingsDataStore
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class SettingsRepository(private val settingsDataStore: SettingsDataStore) {

    val settings: Flow<AppSettings> = settingsDataStore.settingsFlow

    suspend fun setFreeMode(enabled: Boolean) {
        settingsDataStore.updateFreeMode(enabled)
    }

    suspend fun setAutomationEnabled(enabled: Boolean) {
        settingsDataStore.updateAutomationEnabled(enabled)
    }

    suspend fun setAutomationWindow(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int) {
        settingsDataStore.updateAutomationWindow(startHour, startMinute, endHour, endMinute)
    }

    suspend fun setDailyTargets(postTarget: Int, reelTarget: Int) {
        settingsDataStore.updateDailyTargets(postTarget, reelTarget)
    }

    suspend fun setTimezone(timezone: String) {
        settingsDataStore.updateTimezone(timezone)
    }

    suspend fun updateScoutSettings(
        assam: Boolean,
        northeast: Boolean,
        india: Boolean,
        international: Boolean,
        news: Boolean
    ) {
        settingsDataStore.updateScoutSettings(assam, northeast, india, international, news)
    }

    suspend fun incrementTodayPostCount() {
        val todayKey = getTodayDateKey()
        settingsDataStore.incrementPostCount(todayKey)
    }

    suspend fun incrementTodayReelCount() {
        val todayKey = getTodayDateKey()
        settingsDataStore.incrementReelCount(todayKey)
    }

    suspend fun resetTodayCounts() {
        val todayKey = getTodayDateKey()
        settingsDataStore.resetCounts(todayKey)
    }

    suspend fun checkAndResetDailyCounts() {
        val todayKey = getTodayDateKey()
        settingsDataStore.checkAndResetDailyCounts(todayKey)
    }

    suspend fun updateCustomGeminiApiKey(key: String) {
        settingsDataStore.updateCustomGeminiApiKey(key)
    }

    private fun getTodayDateKey(): String {
        return try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            sdf.format(java.util.Date())
        } catch (_: Throwable) {
            "today"
        }
    }
}
