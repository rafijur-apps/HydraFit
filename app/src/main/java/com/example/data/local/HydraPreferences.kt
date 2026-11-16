package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.hydraDataStore by preferencesDataStore(name = "hydrafit_prefs")

data class HydraSettings(
    val waterGoalMl: Int = DEFAULT_WATER_GOAL_ML,
    val exerciseGoalMins: Int = DEFAULT_EXERCISE_GOAL_MINS,
    val silentReminderEnabled: Boolean = false,
    val lastReminderEpochMillis: Long = 0L,
) {
    companion object {
        const val DEFAULT_WATER_GOAL_ML = 2000
        const val DEFAULT_EXERCISE_GOAL_MINS = 15
    }
}

class HydraPreferences(private val context: Context) {
    private object Keys {
        val WATER_GOAL_ML: Preferences.Key<Int> = intPreferencesKey("water_goal_ml")
        val EXERCISE_GOAL_MINS: Preferences.Key<Int> = intPreferencesKey("exercise_goal_mins")
        val SILENT_REMINDER_ENABLED: Preferences.Key<Boolean> =
            booleanPreferencesKey("silent_reminder_enabled")
        val LAST_REMINDER_EPOCH_MILLIS: Preferences.Key<Long> =
            longPreferencesKey("last_reminder_epoch_millis")
    }

    val settingsFlow: Flow<HydraSettings> = context.hydraDataStore.data.map { prefs ->
        HydraSettings(
            waterGoalMl = prefs[Keys.WATER_GOAL_ML] ?: HydraSettings.DEFAULT_WATER_GOAL_ML,
            exerciseGoalMins = prefs[Keys.EXERCISE_GOAL_MINS]
                ?: HydraSettings.DEFAULT_EXERCISE_GOAL_MINS,
            silentReminderEnabled = prefs[Keys.SILENT_REMINDER_ENABLED] ?: false,
            lastReminderEpochMillis = prefs[Keys.LAST_REMINDER_EPOCH_MILLIS] ?: 0L,
        )
    }

    suspend fun getSettingsSnapshot(): HydraSettings = settingsFlow.first()

    suspend fun setWaterGoalMl(goalMl: Int) {
        context.hydraDataStore.edit { prefs ->
            prefs[Keys.WATER_GOAL_ML] = goalMl.coerceIn(250, 10000)
        }
    }

    suspend fun setExerciseGoalMins(goalMins: Int) {
        context.hydraDataStore.edit { prefs ->
            prefs[Keys.EXERCISE_GOAL_MINS] = goalMins.coerceIn(1, 300)
        }
    }

    suspend fun setSilentReminderEnabled(enabled: Boolean) {
        context.hydraDataStore.edit { prefs ->
            prefs[Keys.SILENT_REMINDER_ENABLED] = enabled
        }
    }

    suspend fun updateLastReminderEpochMillis(epochMillis: Long) {
        context.hydraDataStore.edit { prefs ->
            prefs[Keys.LAST_REMINDER_EPOCH_MILLIS] = epochMillis
        }
    }
}
