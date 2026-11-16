package com.example.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.HydraPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HydraReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val pendingResult = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = HydraPreferences(appContext)
                val settings = prefs.getSettingsSnapshot()
                if (settings.silentReminderEnabled && HydraVibrationHelper.isWithinActiveHours()) {
                    HydraVibrationHelper.vibrateSilentPulse(appContext)
                    prefs.updateLastReminderEpochMillis(System.currentTimeMillis())
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
