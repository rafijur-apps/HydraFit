package com.example.data.repository

import com.example.data.local.ExerciseLogEntity
import com.example.data.local.HydraFitDao
import com.example.data.local.HydraPreferences
import com.example.data.local.HydraSettings
import com.example.data.local.WaterLogEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class DailyTrendPoint(
    val dateKey: String,
    val shortLabel: String,
    val totalValue: Int,
    val isToday: Boolean,
)

data class PeriodStats(
    val weeklyTotal: Int,
    val monthlyTotal: Int,
    val weeklyActiveDays: Int,
    val monthlyActiveDays: Int,
    val dailyTrend: List<DailyTrendPoint>,
)

class HydraFitRepository(
    private val dao: HydraFitDao,
    private val preferences: HydraPreferences,
) {
    val settingsFlow: Flow<HydraSettings> = preferences.settingsFlow

    fun todayDateKey(nowMillis: Long = System.currentTimeMillis()): String =
        formatDateKey(nowMillis)

    fun observeTodayWaterLogs(dateKey: String = todayDateKey()): Flow<List<WaterLogEntity>> =
        dao.observeWaterLogsForDate(dateKey)

    fun observeWaterPeriodStats(nowMillis: Long = System.currentTimeMillis()): Flow<PeriodStats> {
        val monthStartKey = dateKeyDaysAgo(29, nowMillis)
        val weekStartKey = dateKeyDaysAgo(6, nowMillis)
        return dao.observeWaterLogsSince(monthStartKey).map { logs ->
            buildPeriodStats(
                entries = logs.map { it.dateKey to it.amountMl },
                weekStartKey = weekStartKey,
                nowMillis = nowMillis,
            )
        }
    }

    fun observeTodayExerciseLogs(dateKey: String = todayDateKey()): Flow<List<ExerciseLogEntity>> =
        dao.observeExerciseLogsForDate(dateKey)

    fun observeExercisePeriodStats(
        nowMillis: Long = System.currentTimeMillis(),
    ): Flow<PeriodStats> {
        val monthStartKey = dateKeyDaysAgo(29, nowMillis)
        val weekStartKey = dateKeyDaysAgo(6, nowMillis)
        return dao.observeExerciseLogsSince(monthStartKey).map { logs ->
            buildPeriodStats(
                entries = logs.map { it.dateKey to it.minutes },
                weekStartKey = weekStartKey,
                nowMillis = nowMillis,
            )
        }
    }

    suspend fun addWaterLog(amountMl: Int, nowMillis: Long = System.currentTimeMillis()) {
        if (amountMl <= 0) return
        dao.insertWaterLog(
            WaterLogEntity(
                amountMl = amountMl,
                timestampMillis = nowMillis,
                dateKey = formatDateKey(nowMillis),
            ),
        )
    }

    suspend fun deleteWaterLog(id: Long) {
        dao.deleteWaterLogById(id)
    }

    suspend fun addExerciseLog(minutes: Int, nowMillis: Long = System.currentTimeMillis()) {
        if (minutes <= 0) return
        dao.insertExerciseLog(
            ExerciseLogEntity(
                minutes = minutes,
                timestampMillis = nowMillis,
                dateKey = formatDateKey(nowMillis),
            ),
        )
    }

    suspend fun deleteExerciseLog(id: Long) {
        dao.deleteExerciseLogById(id)
    }

    suspend fun updateWaterGoalMl(goalMl: Int) {
        preferences.setWaterGoalMl(goalMl)
    }

    suspend fun updateExerciseGoalMins(goalMins: Int) {
        preferences.setExerciseGoalMins(goalMins)
    }

    suspend fun setSilentReminderEnabled(enabled: Boolean) {
        preferences.setSilentReminderEnabled(enabled)
    }

    suspend fun recordReminderTriggered(epochMillis: Long = System.currentTimeMillis()) {
        preferences.updateLastReminderEpochMillis(epochMillis)
    }

    private fun buildPeriodStats(
        entries: List<Pair<String, Int>>,
        weekStartKey: String,
        nowMillis: Long,
    ): PeriodStats {
        val byDate = mutableMapOf<String, Int>()
        for ((dateKey, value) in entries) {
            byDate[dateKey] = (byDate[dateKey] ?: 0) + value
        }

        val monthlyTotal = byDate.values.sum()
        val monthlyActiveDays = byDate.count { it.value > 0 }

        var weeklyTotal = 0
        var weeklyActiveDays = 0
        for ((dateKey, total) in byDate) {
            if (dateKey >= weekStartKey) {
                weeklyTotal += total
                if (total > 0) weeklyActiveDays++
            }
        }

        val todayKey = formatDateKey(nowMillis)
        val dayLabelFormat = SimpleDateFormat("EEE", Locale.US)
        val trend = (6 downTo 0).map { offset ->
            val cal = Calendar.getInstance().apply {
                timeInMillis = nowMillis
                add(Calendar.DAY_OF_YEAR, -offset)
            }
            val key = formatDateKey(cal.timeInMillis)
            DailyTrendPoint(
                dateKey = key,
                shortLabel = if (key == todayKey) "Today" else dayLabelFormat.format(cal.time),
                totalValue = byDate[key] ?: 0,
                isToday = key == todayKey,
            )
        }

        return PeriodStats(
            weeklyTotal = weeklyTotal,
            monthlyTotal = monthlyTotal,
            weeklyActiveDays = weeklyActiveDays,
            monthlyActiveDays = monthlyActiveDays,
            dailyTrend = trend,
        )
    }

    companion object {
        fun formatDateKey(epochMillis: Long): String {
            val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            return fmt.format(Date(epochMillis))
        }

        fun dateKeyDaysAgo(daysAgo: Int, nowMillis: Long = System.currentTimeMillis()): String {
            val cal = Calendar.getInstance().apply {
                timeInMillis = nowMillis
                add(Calendar.DAY_OF_YEAR, -daysAgo)
            }
            return formatDateKey(cal.timeInMillis)
        }
    }
}
