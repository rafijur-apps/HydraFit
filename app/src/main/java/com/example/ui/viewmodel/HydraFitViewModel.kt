package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.ExerciseLogEntity
import com.example.data.local.HydraFitDatabase
import com.example.data.local.HydraPreferences
import com.example.data.local.HydraSettings
import com.example.data.local.WaterLogEntity
import com.example.data.repository.HydraFitRepository
import com.example.data.repository.PeriodStats
import com.example.reminder.HydraReminderScheduler
import com.example.reminder.HydraVibrationHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class HydraFitTab {
    WATER,
    EXERCISE,
}

data class WaterUiState(
    val goalMl: Int = HydraSettings.DEFAULT_WATER_GOAL_ML,
    val consumedMl: Int = 0,
    val remainingMl: Int = HydraSettings.DEFAULT_WATER_GOAL_ML,
    val percentage: Int = 0,
    val progressFraction: Float = 0f,
    val todayLogs: List<WaterLogEntity> = emptyList(),
    val periodStats: PeriodStats = PeriodStats(0, 0, 0, 0, emptyList()),
    val silentReminderEnabled: Boolean = false,
    val isCurrentlyActiveHours: Boolean = HydraVibrationHelper.isWithinActiveHours(),
    val lastReminderEpochMillis: Long = 0L,
)

data class ExerciseUiState(
    val goalMins: Int = HydraSettings.DEFAULT_EXERCISE_GOAL_MINS,
    val completedMins: Int = 0,
    val remainingMins: Int = HydraSettings.DEFAULT_EXERCISE_GOAL_MINS,
    val percentage: Int = 0,
    val progressFraction: Float = 0f,
    val todayLogs: List<ExerciseLogEntity> = emptyList(),
    val periodStats: PeriodStats = PeriodStats(0, 0, 0, 0, emptyList()),
)

@OptIn(ExperimentalCoroutinesApi::class)
class HydraFitViewModel(
    application: Application,
    private val repository: HydraFitRepository,
) : AndroidViewModel(application) {

    private val currentDateKey = MutableStateFlow(repository.todayDateKey())
    private val isActiveHoursFlow = MutableStateFlow(HydraVibrationHelper.isWithinActiveHours())

    private val todayWaterLogs = currentDateKey.flatMapLatest { dateKey ->
        repository.observeTodayWaterLogs(dateKey)
    }

    private val waterStats = currentDateKey.flatMapLatest {
        repository.observeWaterPeriodStats()
    }

    private val todayExerciseLogs = currentDateKey.flatMapLatest { dateKey ->
        repository.observeTodayExerciseLogs(dateKey)
    }

    private val exerciseStats = currentDateKey.flatMapLatest {
        repository.observeExercisePeriodStats()
    }

    val waterUiState: StateFlow<WaterUiState> = combine(
        repository.settingsFlow,
        todayWaterLogs,
        waterStats,
        isActiveHoursFlow,
    ) { settings, logs, stats, isActiveNow ->
        val consumed = logs.sumOf { it.amountMl }
        val goal = settings.waterGoalMl.coerceAtLeast(1)
        val remaining = (goal - consumed).coerceAtLeast(0)
        val fraction = (consumed.toFloat() / goal.toFloat()).coerceIn(0f, 1f)
        val pct = ((consumed.toFloat() / goal.toFloat()) * 100f).toInt().coerceAtLeast(0)
        WaterUiState(
            goalMl = goal,
            consumedMl = consumed,
            remainingMl = remaining,
            percentage = pct,
            progressFraction = fraction,
            todayLogs = logs,
            periodStats = stats,
            silentReminderEnabled = settings.silentReminderEnabled,
            isCurrentlyActiveHours = isActiveNow,
            lastReminderEpochMillis = settings.lastReminderEpochMillis,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WaterUiState(),
    )

    val exerciseUiState: StateFlow<ExerciseUiState> = combine(
        repository.settingsFlow,
        todayExerciseLogs,
        exerciseStats,
    ) { settings, logs, stats ->
        val completed = logs.sumOf { it.minutes }
        val goal = settings.exerciseGoalMins.coerceAtLeast(1)
        val remaining = (goal - completed).coerceAtLeast(0)
        val fraction = (completed.toFloat() / goal.toFloat()).coerceIn(0f, 1f)
        val pct = ((completed.toFloat() / goal.toFloat()) * 100f).toInt().coerceAtLeast(0)
        ExerciseUiState(
            goalMins = goal,
            completedMins = completed,
            remainingMins = remaining,
            percentage = pct,
            progressFraction = fraction,
            todayLogs = logs,
            periodStats = stats,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ExerciseUiState(),
    )

    init {
        viewModelScope.launch {
            while (isActive) {
                refreshDayAndWindowStatus()
                val currentWater = waterUiState.value
                val now = System.currentTimeMillis()
                if (
                    currentWater.silentReminderEnabled &&
                    currentWater.isCurrentlyActiveHours &&
                    currentWater.lastReminderEpochMillis > 0L &&
                    now - currentWater.lastReminderEpochMillis >= HydraVibrationHelper.INTERVAL_MILLIS
                ) {
                    HydraVibrationHelper.vibrateSilentPulse(getApplication())
                    repository.recordReminderTriggered(now)
                }
                delay(60_000L)
            }
        }
    }

    fun refreshDayAndWindowStatus() {
        val latestKey = repository.todayDateKey()
        if (currentDateKey.value != latestKey) {
            currentDateKey.value = latestKey
        }
        isActiveHoursFlow.value = HydraVibrationHelper.isWithinActiveHours()
    }

    fun addWater(amountMl: Int) {
        if (amountMl <= 0) return
        viewModelScope.launch {
            repository.addWaterLog(amountMl)
        }
    }

    fun deleteWaterLog(id: Long) {
        viewModelScope.launch {
            repository.deleteWaterLog(id)
        }
    }

    fun updateWaterGoal(goalMl: Int) {
        if (goalMl < 250) return
        viewModelScope.launch {
            repository.updateWaterGoalMl(goalMl)
        }
    }

    fun toggleSilentReminder(enabled: Boolean) {
        viewModelScope.launch {
            repository.setSilentReminderEnabled(enabled)
            val app = getApplication<Application>()
            if (enabled) {
                repository.recordReminderTriggered(System.currentTimeMillis())
                HydraReminderScheduler.scheduleRepeatingReminder(app)
            } else {
                HydraReminderScheduler.cancelReminder(app)
            }
        }
    }

    fun testSilentVibration(): Boolean {
        return HydraVibrationHelper.vibrateSilentPulse(getApplication())
    }

    fun addExercise(minutes: Int) {
        if (minutes <= 0) return
        viewModelScope.launch {
            repository.addExerciseLog(minutes)
        }
    }

    fun deleteExerciseLog(id: Long) {
        viewModelScope.launch {
            repository.deleteExerciseLog(id)
        }
    }

    fun updateExerciseGoal(goalMins: Int) {
        if (goalMins < 1) return
        viewModelScope.launch {
            repository.updateExerciseGoalMins(goalMins)
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            val dao = HydraFitDatabase.getInstance(application).hydraFitDao()
            val prefs = HydraPreferences(application)
            val repo = HydraFitRepository(dao, prefs)
            return HydraFitViewModel(application, repo) as T
        }
    }
}
