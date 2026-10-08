package com.smmiqbal.fluidtracker

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "fluid_tracker_prefs")

class WaterRepository(private val context: Context) {

    companion object {
        private val GLASS_COUNT_KEY = intPreferencesKey("glass_count")
        private val GOAL_COUNT_KEY = intPreferencesKey("goal_count")
        private val TOTAL_VOLUME_ML_KEY = intPreferencesKey("total_volume_ml")
        private val GOAL_VOLUME_ML_KEY = intPreferencesKey("goal_volume_ml")
        private val INTAKE_LOGS_KEY = stringPreferencesKey("intake_logs")
        private val WEEKLY_VOLUMES_KEY = stringPreferencesKey("weekly_volumes")
        private val REMINDERS_ENABLED_KEY = booleanPreferencesKey("reminders_enabled")
        private val SELECTED_DRINK_KEY = stringPreferencesKey("selected_drink")
        private val SELECTED_THEME_KEY = stringPreferencesKey("selected_theme")
        private val STREAK_DAYS_KEY = intPreferencesKey("streak_days")
        private val BEST_STREAK_KEY = intPreferencesKey("best_streak")
        private val LIFETIME_DRINKS_KEY = intPreferencesKey("lifetime_drinks")
        private val VITALITY_SCORE_KEY = intPreferencesKey("vitality_score")
        private val STREAK_FREEZE_KEY = intPreferencesKey("streak_freeze")
        private val LAST_ACTIVE_DATE_KEY = stringPreferencesKey("last_active_date")
        private val SELECTED_WIDGET_QUOTE_KEY = intPreferencesKey("selected_widget_quote")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
    }

    val glassCount: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[GLASS_COUNT_KEY] ?: 0
    }

    val goalCount: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[GOAL_COUNT_KEY] ?: 8
    }

    val totalVolumeMl: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[TOTAL_VOLUME_ML_KEY] ?: ((prefs[GLASS_COUNT_KEY] ?: 0) * 250)
    }

    val goalVolumeMl: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[GOAL_VOLUME_ML_KEY] ?: 2000
    }

    val remindersEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[REMINDERS_ENABLED_KEY] ?: true
    }

    val selectedDrink: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[SELECTED_DRINK_KEY] ?: "water"
    }

    val selectedTheme: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[SELECTED_THEME_KEY] ?: "celestial"
    }

    // Starts cleanly from Day 1
    val streakDays: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[STREAK_DAYS_KEY] ?: 1
    }

    val bestStreak: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[BEST_STREAK_KEY] ?: 1
    }

    val lifetimeDrinks: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[LIFETIME_DRINKS_KEY] ?: 0
    }

    val vitalityScore: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[VITALITY_SCORE_KEY] ?: 0
    }

    val streakFreezeCount: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[STREAK_FREEZE_KEY] ?: 3
    }

    val selectedWidgetQuote: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[SELECTED_WIDGET_QUOTE_KEY] ?: -1 // -1 = Auto-Rotate
    }

    val userName: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[USER_NAME_KEY] ?: "S. M. Mahmud Iqbal"
    }

    val intakeLogs: Flow<List<IntakeLog>> = context.dataStore.data.map { prefs ->
        val raw = prefs[INTAKE_LOGS_KEY] ?: ""
        if (raw.isBlank()) {
            emptyList()
        } else {
            raw.split(";").mapNotNull { entry ->
                val parts = entry.split("|")
                if (parts.size >= 4) {
                    val id = parts[0].toLongOrNull() ?: System.currentTimeMillis()
                    val time = parts[1]
                    val ml = parts[2].toIntOrNull() ?: 250
                    val date = parts[3]
                    IntakeLog(id = id, timeStr = time, volumeMl = ml, dateStr = date)
                } else null
            }
        }
    }

    // 7-day weekly history matching reference TUE, WED, THU, FRI, SAT, SUN, MON
    val weeklyHistory: Flow<List<DayHistory>> = context.dataStore.data.map { prefs ->
        val rawWeekly = prefs[WEEKLY_VOLUMES_KEY] ?: "1400,1650,1200,1800,1500,1350"
        val dayVolumes = rawWeekly.split(",").mapNotNull { it.toIntOrNull() }
        val currentDayVolume = prefs[TOTAL_VOLUME_ML_KEY] ?: ((prefs[GLASS_COUNT_KEY] ?: 0) * 250)
        val goal = prefs[GOAL_VOLUME_ML_KEY] ?: 2000

        // Weekday labels ending at current day (like reference TUE..MON)
        val labels = listOf("TUE", "WED", "THU", "FRI", "SAT", "SUN", "MON")
        val result = mutableListOf<DayHistory>()
        for (i in 0..5) {
            val vol = if (i < dayVolumes.size) dayVolumes[i] else 1400
            result.add(DayHistory(dayLabel = labels[i], dateStr = "", volumeMl = vol, goalMl = goal, isToday = false))
        }
        // Current day (MON / Today)
        result.add(DayHistory(dayLabel = labels[6], dateStr = LocalDate.now().toString(), volumeMl = currentDayVolume, goalMl = goal, isToday = true))
        result
    }

    // Midnight Auto-reset & Date transition handler
    suspend fun checkDateTransition() {
        val todayStr = LocalDate.now().toString()
        val prefs = context.dataStore.data.first()
        val lastDate = prefs[LAST_ACTIVE_DATE_KEY]

        if (lastDate == null) {
            context.dataStore.edit {
                it[LAST_ACTIVE_DATE_KEY] = todayStr
                if (!it.contains(STREAK_DAYS_KEY)) it[STREAK_DAYS_KEY] = 1
                if (!it.contains(BEST_STREAK_KEY)) it[BEST_STREAK_KEY] = 1
            }
            return
        }

        if (lastDate != todayStr) {
            val yesterdayCount = prefs[GLASS_COUNT_KEY] ?: 0
            val yesterdayVolume = prefs[TOTAL_VOLUME_ML_KEY] ?: 0
            val currentStreak = prefs[STREAK_DAYS_KEY] ?: 1
            val best = prefs[BEST_STREAK_KEY] ?: 1
            val goal = prefs[GOAL_COUNT_KEY] ?: 8

            // Shift weekly history string
            val rawWeekly = prefs[WEEKLY_VOLUMES_KEY] ?: "1400,1650,1200,1800,1500,1350"
            val parts = rawWeekly.split(",").toMutableList()
            if (parts.size >= 6) {
                parts.removeAt(0)
            }
            parts.add(yesterdayVolume.toString())
            val updatedWeekly = parts.joinToString(",")

            context.dataStore.edit { p ->
                p[LAST_ACTIVE_DATE_KEY] = todayStr
                p[GLASS_COUNT_KEY] = 0 // Reset daily count
                p[TOTAL_VOLUME_ML_KEY] = 0 // Reset daily volume
                p[INTAKE_LOGS_KEY] = "" // Reset daily intake logs
                p[WEEKLY_VOLUMES_KEY] = updatedWeekly

                if (yesterdayCount >= (goal * 0.75).toInt() || yesterdayVolume >= 1500) {
                    val newStreak = currentStreak + 1
                    p[STREAK_DAYS_KEY] = newStreak
                    if (newStreak > best) {
                        p[BEST_STREAK_KEY] = newStreak
                    }
                    val currentScore = p[VITALITY_SCORE_KEY] ?: 0
                    p[VITALITY_SCORE_KEY] = currentScore + 25 // Daily streak bonus points
                }
            }
            WaterAppWidgetProvider.updateAllWidgets(context)
        }
    }

    suspend fun addIntake(volumeMl: Int) {
        val now = LocalTime.now()
        val timeStr = now.format(DateTimeFormatter.ofPattern("hh:mm a"))
        val dateStr = LocalDate.now().toString()
        val logId = System.currentTimeMillis()

        context.dataStore.edit { prefs ->
            val currentGlasses = prefs[GLASS_COUNT_KEY] ?: 0
            val currentVol = prefs[TOTAL_VOLUME_ML_KEY] ?: (currentGlasses * 250)
            val newVol = currentVol + volumeMl
            prefs[TOTAL_VOLUME_ML_KEY] = newVol

            val newGlasses = (newVol / 250).coerceAtLeast(currentGlasses + 1)
            prefs[GLASS_COUNT_KEY] = newGlasses

            val lifetime = prefs[LIFETIME_DRINKS_KEY] ?: 0
            prefs[LIFETIME_DRINKS_KEY] = lifetime + 1

            val currentScore = prefs[VITALITY_SCORE_KEY] ?: 0
            val goalVol = prefs[GOAL_VOLUME_ML_KEY] ?: 2000
            val bonus = if (newVol >= goalVol && currentVol < goalVol) 50 else (volumeMl / 10).coerceAtLeast(10)
            prefs[VITALITY_SCORE_KEY] = currentScore + bonus

            // Append to intake logs
            val existing = prefs[INTAKE_LOGS_KEY] ?: ""
            val newEntry = "$logId|$timeStr|$volumeMl|$dateStr"
            prefs[INTAKE_LOGS_KEY] = if (existing.isBlank()) newEntry else "$newEntry;$existing"
        }
        WaterAppWidgetProvider.updateAllWidgets(context)
    }

    suspend fun deleteIntake(logId: Long) {
        context.dataStore.edit { prefs ->
            val existing = prefs[INTAKE_LOGS_KEY] ?: ""
            if (existing.isNotBlank()) {
                val entries = existing.split(";").toMutableList()
                val target = entries.find { it.startsWith("$logId|") }
                if (target != null) {
                    val parts = target.split("|")
                    val vol = parts.getOrNull(2)?.toIntOrNull() ?: 250
                    entries.remove(target)
                    prefs[INTAKE_LOGS_KEY] = entries.joinToString(";")

                    val currentVol = prefs[TOTAL_VOLUME_ML_KEY] ?: 0
                    val newVol = (currentVol - vol).coerceAtLeast(0)
                    prefs[TOTAL_VOLUME_ML_KEY] = newVol
                    prefs[GLASS_COUNT_KEY] = (newVol / 250).coerceAtLeast(0)
                }
            }
        }
        WaterAppWidgetProvider.updateAllWidgets(context)
    }

    suspend fun increment() {
        addIntake(250)
    }

    suspend fun reset() {
        context.dataStore.edit { prefs ->
            prefs[GLASS_COUNT_KEY] = 0
            prefs[TOTAL_VOLUME_ML_KEY] = 0
            prefs[INTAKE_LOGS_KEY] = ""
        }
        WaterAppWidgetProvider.updateAllWidgets(context)
    }

    suspend fun setGoalVolume(goalMl: Int) {
        context.dataStore.edit { prefs ->
            prefs[GOAL_VOLUME_ML_KEY] = goalMl
            prefs[GOAL_COUNT_KEY] = (goalMl / 250).coerceAtLeast(1)
        }
        WaterAppWidgetProvider.updateAllWidgets(context)
    }

    suspend fun setDrink(drinkId: String) {
        context.dataStore.edit { prefs ->
            prefs[SELECTED_DRINK_KEY] = "water"
        }
        WaterAppWidgetProvider.updateAllWidgets(context)
    }

    suspend fun setTheme(themeId: String) {
        context.dataStore.edit { prefs ->
            prefs[SELECTED_THEME_KEY] = themeId
        }
        WaterAppWidgetProvider.updateAllWidgets(context)
    }

    suspend fun setUserName(name: String) {
        context.dataStore.edit { prefs ->
            prefs[USER_NAME_KEY] = name.trim().ifBlank { "S. M. Mahmud Iqbal" }
        }
        WaterAppWidgetProvider.updateAllWidgets(context)
    }

    suspend fun setWidgetQuote(index: Int) {
        context.dataStore.edit { prefs ->
            prefs[SELECTED_WIDGET_QUOTE_KEY] = index
        }
        WaterAppWidgetProvider.updateAllWidgets(context)
    }

    suspend fun useStreakFreeze(): Boolean {
        var used = false
        context.dataStore.edit { prefs ->
            val freezes = prefs[STREAK_FREEZE_KEY] ?: 3
            if (freezes > 0) {
                prefs[STREAK_FREEZE_KEY] = freezes - 1
                val current = prefs[STREAK_DAYS_KEY] ?: 1
                val newStreak = current + 1
                prefs[STREAK_DAYS_KEY] = newStreak
                val best = prefs[BEST_STREAK_KEY] ?: 1
                if (newStreak > best) {
                    prefs[BEST_STREAK_KEY] = newStreak
                }
                used = true
            }
        }
        if (used) {
            WaterAppWidgetProvider.updateAllWidgets(context)
        }
        return used
    }

    suspend fun setRemindersEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[REMINDERS_ENABLED_KEY] = enabled
        }
        if (enabled) {
            NotificationHelper.scheduleReminders(context)
        } else {
            NotificationHelper.cancelReminders(context)
        }
    }
}
