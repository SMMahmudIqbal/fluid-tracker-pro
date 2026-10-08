package com.smmiqbal.fluidtracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalTime

class WaterViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WaterRepository(application)

    init {
        viewModelScope.launch {
            repository.checkDateTransition()
        }
    }

    val glassCount: StateFlow<Int> = repository.glassCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val goalCount: StateFlow<Int> = repository.goalCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 8)

    val totalVolumeMl: StateFlow<Int> = repository.totalVolumeMl
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val goalVolumeMl: StateFlow<Int> = repository.goalVolumeMl
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 2000)

    val remindersEnabled: StateFlow<Boolean> = repository.remindersEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val selectedDrinkId: StateFlow<String> = repository.selectedDrink
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "water")

    val selectedThemeId: StateFlow<String> = repository.selectedTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "celestial")

    val userName: StateFlow<String> = repository.userName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "S. M. Mahmud Iqbal")

    val streakDays: StateFlow<Int> = repository.streakDays
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    val bestStreak: StateFlow<Int> = repository.bestStreak
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    val lifetimeDrinks: StateFlow<Int> = repository.lifetimeDrinks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val vitalityScore: StateFlow<Int> = repository.vitalityScore
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val streakFreezeCount: StateFlow<Int> = repository.streakFreezeCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 3)

    val selectedWidgetQuote: StateFlow<Int> = repository.selectedWidgetQuote
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), -1)

    val intakeLogs: StateFlow<List<IntakeLog>> = repository.intakeLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val weeklyHistory: StateFlow<List<DayHistory>> = repository.weeklyHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dynamic, science-backed hydration insight based on current intake and time of day
    val healthInsight: StateFlow<String> = combine(totalVolumeMl, goalVolumeMl) { vol, goal ->
        val hour = LocalTime.now().hour
        when {
            vol == 0 && hour < 11 ->
                "Morning replenishment: 500ml upon waking stimulates metabolism and restores night fluid loss."
            vol == 0 ->
                "Begin your hydration cycle. Early intake prevents midday fatigue and headaches."
            vol < 750 ->
                "Good start. Consistent cellular hydration supports blood volume and cardiovascular efficiency."
            vol < 1500 ->
                "Steady progress. Optimal hydration maintains mental acuity, focus, and physical endurance."
            vol < goal ->
                "Approaching daily target. Joint lubrication, body temperature regulation, and digestion optimized."
            vol >= goal ->
                "Daily optimal hydration goal achieved. Fluid balance and cellular nutrient transport peaked."
            else ->
                "Maintain consistent hydration throughout the day for balanced energy levels."
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        "Optimal physical and cognitive performance requires consistent hydration."
    )

    fun addIntake(volumeMl: Int) {
        viewModelScope.launch {
            repository.addIntake(volumeMl)
        }
    }

    fun addGlass() {
        addIntake(250)
    }

    fun deleteIntake(logId: Long) {
        viewModelScope.launch {
            repository.deleteIntake(logId)
        }
    }

    fun setGoalVolume(goalMl: Int) {
        viewModelScope.launch {
            repository.setGoalVolume(goalMl)
        }
    }

    fun resetCount() {
        viewModelScope.launch {
            repository.reset()
        }
    }

    fun selectDrink(drinkId: String) {
        viewModelScope.launch {
            repository.setDrink(drinkId)
        }
    }

    fun selectTheme(themeId: String) {
        viewModelScope.launch {
            repository.setTheme(themeId)
        }
    }

    fun setWidgetQuote(index: Int) {
        viewModelScope.launch {
            repository.setWidgetQuote(index)
        }
    }

    fun useStreakFreeze(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.useStreakFreeze()
            onResult(success)
        }
    }

    fun toggleReminders(enabled: Boolean) {
        viewModelScope.launch {
            repository.setRemindersEnabled(enabled)
        }
    }

    fun setUserName(name: String) {
        viewModelScope.launch {
            repository.setUserName(name)
        }
    }
}
