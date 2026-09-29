package com.example.data.local.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "social_agent_settings")

data class AppSettings(
    val freeMode: Boolean = true,
    val automationEnabled: Boolean = true,
    val automationStartHour: Int = 10,
    val automationStartMinute: Int = 0,
    val automationEndHour: Int = 11,
    val automationEndMinute: Int = 0,
    val timezone: String = "Asia/Kolkata",
    val dailyPostTarget: Int = 8,
    val dailyReelTarget: Int = 2,
    val todayPostCount: Int = 0,
    val todayReelCount: Int = 0,
    val lastCountDate: String = "",
    val assamPriority: Boolean = true,
    val northeastPriority: Boolean = true,
    val indiaOpportunities: Boolean = true,
    val internationalOpportunities: Boolean = false,
    val newsCollection: Boolean = true
)

class SettingsDataStore(private val context: Context) {

    companion object {
        val KEY_FREE_MODE = booleanPreferencesKey("free_mode")
        val KEY_AUTOMATION_ENABLED = booleanPreferencesKey("automation_enabled")
        val KEY_AUTOMATION_START_HOUR = intPreferencesKey("automation_start_hour")
        val KEY_AUTOMATION_START_MINUTE = intPreferencesKey("automation_start_minute")
        val KEY_AUTOMATION_END_HOUR = intPreferencesKey("automation_end_hour")
        val KEY_AUTOMATION_END_MINUTE = intPreferencesKey("automation_end_minute")
        val KEY_TIMEZONE = stringPreferencesKey("timezone")
        val KEY_DAILY_POST_TARGET = intPreferencesKey("daily_post_target")
        val KEY_DAILY_REEL_TARGET = intPreferencesKey("daily_reel_target")
        val KEY_TODAY_POST_COUNT = intPreferencesKey("today_post_count")
        val KEY_TODAY_REEL_COUNT = intPreferencesKey("today_reel_count")
        val KEY_LAST_COUNT_DATE = stringPreferencesKey("last_count_date")
        val KEY_ASSAM_PRIORITY = booleanPreferencesKey("assam_priority")
        val KEY_NORTHEAST_PRIORITY = booleanPreferencesKey("northeast_priority")
        val KEY_INDIA_OPPORTUNITIES = booleanPreferencesKey("india_opportunities")
        val KEY_INTERNATIONAL_OPPORTUNITIES = booleanPreferencesKey("international_opportunities")
        val KEY_NEWS_COLLECTION = booleanPreferencesKey("news_collection")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs ->
            AppSettings(
                freeMode = prefs[KEY_FREE_MODE] ?: true,
                automationEnabled = prefs[KEY_AUTOMATION_ENABLED] ?: true,
                automationStartHour = prefs[KEY_AUTOMATION_START_HOUR] ?: 10,
                automationStartMinute = prefs[KEY_AUTOMATION_START_MINUTE] ?: 0,
                automationEndHour = prefs[KEY_AUTOMATION_END_HOUR] ?: 11,
                automationEndMinute = prefs[KEY_AUTOMATION_END_MINUTE] ?: 0,
                timezone = prefs[KEY_TIMEZONE] ?: "Asia/Kolkata",
                dailyPostTarget = prefs[KEY_DAILY_POST_TARGET] ?: 8,
                dailyReelTarget = prefs[KEY_DAILY_REEL_TARGET] ?: 2,
                todayPostCount = prefs[KEY_TODAY_POST_COUNT] ?: 0,
                todayReelCount = prefs[KEY_TODAY_REEL_COUNT] ?: 0,
                lastCountDate = prefs[KEY_LAST_COUNT_DATE] ?: "",
                assamPriority = prefs[KEY_ASSAM_PRIORITY] ?: true,
                northeastPriority = prefs[KEY_NORTHEAST_PRIORITY] ?: true,
                indiaOpportunities = prefs[KEY_INDIA_OPPORTUNITIES] ?: true,
                internationalOpportunities = prefs[KEY_INTERNATIONAL_OPPORTUNITIES] ?: false,
                newsCollection = prefs[KEY_NEWS_COLLECTION] ?: true
            )
        }

    suspend fun updateScoutSettings(
        assam: Boolean,
        northeast: Boolean,
        india: Boolean,
        international: Boolean,
        news: Boolean
    ) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ASSAM_PRIORITY] = assam
            prefs[KEY_NORTHEAST_PRIORITY] = northeast
            prefs[KEY_INDIA_OPPORTUNITIES] = india
            prefs[KEY_INTERNATIONAL_OPPORTUNITIES] = international
            prefs[KEY_NEWS_COLLECTION] = news
        }
    }

    suspend fun updateFreeMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_FREE_MODE] = enabled
        }
    }

    suspend fun updateAutomationEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_AUTOMATION_ENABLED] = enabled
        }
    }

    suspend fun updateAutomationWindow(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_AUTOMATION_START_HOUR] = startHour
            prefs[KEY_AUTOMATION_START_MINUTE] = startMinute
            prefs[KEY_AUTOMATION_END_HOUR] = endHour
            prefs[KEY_AUTOMATION_END_MINUTE] = endMinute
        }
    }

    suspend fun updateDailyTargets(postTarget: Int, reelTarget: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DAILY_POST_TARGET] = postTarget
            prefs[KEY_DAILY_REEL_TARGET] = reelTarget
        }
    }

    suspend fun updateTimezone(timezone: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_TIMEZONE] = timezone
        }
    }

    suspend fun resetCounts(dateKey: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_TODAY_POST_COUNT] = 0
            prefs[KEY_TODAY_REEL_COUNT] = 0
            prefs[KEY_LAST_COUNT_DATE] = dateKey
        }
    }

    suspend fun incrementPostCount(dateKey: String) {
        context.dataStore.edit { prefs ->
            val currentDate = prefs[KEY_LAST_COUNT_DATE] ?: ""
            if (currentDate != dateKey) {
                prefs[KEY_LAST_COUNT_DATE] = dateKey
                prefs[KEY_TODAY_POST_COUNT] = 1
                prefs[KEY_TODAY_REEL_COUNT] = 0
            } else {
                val current = prefs[KEY_TODAY_POST_COUNT] ?: 0
                prefs[KEY_TODAY_POST_COUNT] = current + 1
            }
        }
    }

    suspend fun incrementReelCount(dateKey: String) {
        context.dataStore.edit { prefs ->
            val currentDate = prefs[KEY_LAST_COUNT_DATE] ?: ""
            if (currentDate != dateKey) {
                prefs[KEY_LAST_COUNT_DATE] = dateKey
                prefs[KEY_TODAY_POST_COUNT] = 0
                prefs[KEY_TODAY_REEL_COUNT] = 1
            } else {
                val current = prefs[KEY_TODAY_REEL_COUNT] ?: 0
                prefs[KEY_TODAY_REEL_COUNT] = current + 1
            }
        }
    }
}
