package com.cartoniq.lexiforge.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cartoniq.lexiforge.data.AppPrefs
import com.cartoniq.lexiforge.lexiApp
import com.cartoniq.lexiforge.util.AppConstants
import com.cartoniq.lexiforge.work.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val dailyGoal: Int = AppConstants.DEFAULT_DAILY_GOAL,
    val reminderEnabled: Boolean = false,
    val reminderHour: Int = 19,
    val reminderMinute: Int = 0,
    val autoSpeak: Boolean = false,
    val haptics: Boolean = true
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = application.lexiApp().repository
    private val prefs = AppPrefs.get(application)

    private val prefState = MutableStateFlow(readPrefs())

    val uiState: StateFlow<SettingsUiState> = combine(prefState, repo.observeStats()) { p, stats ->
        p.copy(dailyGoal = stats.dailyGoal)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    private fun readPrefs() = SettingsUiState(
        reminderEnabled = prefs.reminderEnabled,
        reminderHour = prefs.reminderHour,
        reminderMinute = prefs.reminderMinute,
        autoSpeak = prefs.autoSpeak,
        haptics = prefs.haptics
    )

    fun setDailyGoal(goal: Int) {
        viewModelScope.launch { repo.setDailyGoal(goal) }
    }

    fun setReminderEnabled(enabled: Boolean) {
        prefs.reminderEnabled = enabled
        val app = getApplication<Application>()
        if (enabled) {
            ReminderScheduler.schedule(app, prefs.reminderHour, prefs.reminderMinute)
        } else {
            ReminderScheduler.cancel(app)
        }
        prefState.value = readPrefs()
    }

    fun setReminderTime(hour: Int, minute: Int) {
        prefs.reminderHour = hour
        prefs.reminderMinute = minute
        if (prefs.reminderEnabled) {
            ReminderScheduler.schedule(getApplication<Application>(), hour, minute)
        }
        prefState.value = readPrefs()
    }

    fun setAutoSpeak(value: Boolean) {
        prefs.autoSpeak = value
        prefState.value = readPrefs()
    }

    fun setHaptics(value: Boolean) {
        prefs.haptics = value
        prefState.value = readPrefs()
    }

    fun resetProgress(onDone: () -> Unit) {
        viewModelScope.launch {
            repo.resetProgress()
            onDone()
        }
    }
}
