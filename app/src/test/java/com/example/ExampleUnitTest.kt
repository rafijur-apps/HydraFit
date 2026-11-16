package com.example

import com.example.reminder.HydraVibrationHelper
import java.util.Calendar
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun activeHours_coversTenAmThroughTwoAm() {
        assertTrue(HydraVibrationHelper.isWithinActiveHours(calendarAt(10, 0)))
        assertTrue(HydraVibrationHelper.isWithinActiveHours(calendarAt(15, 30)))
        assertTrue(HydraVibrationHelper.isWithinActiveHours(calendarAt(23, 59)))
        assertTrue(HydraVibrationHelper.isWithinActiveHours(calendarAt(0, 0)))
        assertTrue(HydraVibrationHelper.isWithinActiveHours(calendarAt(1, 45)))
        assertTrue(HydraVibrationHelper.isWithinActiveHours(calendarAt(2, 0)))
    }

    @Test
    fun activeHours_excludesTwoOhOneAmThroughNineFiftyNineAm() {
        assertFalse(HydraVibrationHelper.isWithinActiveHours(calendarAt(2, 1)))
        assertFalse(HydraVibrationHelper.isWithinActiveHours(calendarAt(5, 0)))
        assertFalse(HydraVibrationHelper.isWithinActiveHours(calendarAt(9, 59)))
    }

    @Test
    fun vibrationPattern_matchesSilentDualPulse() {
        assertArrayEquals(
            longArrayOf(0L, 300L, 100L, 300L),
            HydraVibrationHelper.SILENT_PULSE_PATTERN,
        )
    }

    private fun calendarAt(hour: Int, minute: Int): Calendar {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }
}
