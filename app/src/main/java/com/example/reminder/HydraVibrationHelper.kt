package com.example.reminder

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import java.util.Calendar

object HydraVibrationHelper {
    const val INTERVAL_MINUTES = 90L
    const val INTERVAL_MILLIS = INTERVAL_MINUTES * 60L * 1000L

    // Pattern [300, 100, 300] ms with 0ms initial delay -> silent dual-pulse vibration.
    val SILENT_PULSE_PATTERN = longArrayOf(0L, 300L, 100L, 300L)

    /**
     * Active Hours: 10:00 AM through 02:00 AM (night).
     * Inactive Hours: 02:01 AM through 09:59 AM.
     */
    fun isWithinActiveHours(calendar: Calendar = Calendar.getInstance()): Boolean {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        return when {
            hour >= 10 -> true
            hour < 2 -> true
            hour == 2 && minute == 0 -> true
            else -> false
        }
    }

    fun vibrateSilentPulse(context: Context): Boolean {
        val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

        if (vibrator == null || !vibrator.hasVibrator()) {
            return false
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(SILENT_PULSE_PATTERN, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(SILENT_PULSE_PATTERN, -1)
        }
        return true
    }
}
