package com.example

import android.app.Application
import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.HydraFitDatabase
import com.example.data.local.HydraPreferences
import com.example.data.repository.HydraFitRepository
import com.example.ui.theme.HydraFitTheme
import com.example.ui.viewmodel.HydraFitViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var application: Application
    private lateinit var context: Context
    private lateinit var db: HydraFitDatabase
    private lateinit var repository: HydraFitRepository

    @Before
    fun setup() {
        application = ApplicationProvider.getApplicationContext()
        context = application
        db = Room.inMemoryDatabaseBuilder(context, HydraFitDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = HydraFitRepository(db.hydraFitDao(), HydraPreferences(context))
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun appNameMatchesHydraFit() {
        val appName = context.getString(R.string.app_name)
        assertEquals("HydraFit", appName)
    }

    @Test
    fun repository_addsAndDeletesWaterAndExerciseLogsOffline() = runBlocking {
        val now = System.currentTimeMillis()
        repository.addWaterLog(250, now)
        repository.addWaterLog(500, now + 1000)
        val todayWater = repository.observeTodayWaterLogs(repository.todayDateKey(now)).first()
        assertEquals(2, todayWater.size)
        assertEquals(750, todayWater.sumOf { it.amountMl })

        repository.deleteWaterLog(todayWater.first().id)
        val afterDelete = repository.observeTodayWaterLogs(repository.todayDateKey(now)).first()
        assertEquals(1, afterDelete.size)

        repository.addExerciseLog(10, now)
        val todayExercise = repository.observeTodayExerciseLogs(repository.todayDateKey(now)).first()
        assertEquals(1, todayExercise.size)
        assertEquals(10, todayExercise.first().minutes)
    }

    @Test
    fun endToEnd_allButtonsAndTabsWorkProperly() {
        val viewModel = HydraFitViewModel(application, repository)
        composeRule.setContent {
            HydraFitTheme {
                HydraFitApp(viewModel = viewModel)
            }
        }

        // 1. Verify default Water tab & edit goal button
        composeRule.onNodeWithTag("water_progress_ring").assertIsDisplayed()
        composeRule.onNodeWithTag("edit_water_goal_button").performClick()
        composeRule.onNodeWithTag("water_goal_input").performTextReplacement("2500")
        composeRule.onNodeWithTag("water_goal_confirm").performClick()
        waitForText("Goal: 2,500 ml")

        // 2. Click all Water quick-add buttons (+100, +250, +500, Custom)
        composeRule.onNodeWithTag("water_list").performScrollToNode(hasTestTag("add_water_100"))
        composeRule.onNodeWithTag("add_water_100").performClick()
        composeRule.onNodeWithTag("add_water_250").performClick()
        composeRule.onNodeWithTag("add_water_500").performClick()
        composeRule.onNodeWithTag("add_water_custom").performClick()
        composeRule.onNodeWithTag("water_custom_input").performTextReplacement("150")
        composeRule.onNodeWithTag("water_custom_confirm").performClick()

        // Total should now be 100 + 250 + 500 + 150 = 1,000 ml (40% of 2,500 ml)
        composeRule.onNodeWithTag("water_list").performScrollToNode(hasTestTag("water_progress_ring"))
        waitForText("1,000 ml")
        composeRule.onNodeWithText("40%").assertIsDisplayed()

        // 3. Test Silent Reminder switch & Test Pulse button
        composeRule.onNodeWithTag("water_list").performScrollToNode(hasTestTag("silent_reminder_toggle"))
        composeRule.onNodeWithTag("silent_reminder_toggle").performClick()
        composeRule.waitUntil(timeoutMillis = 5000) {
            composeRule.onAllNodesWithText("Test Pulse").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("water_list").performScrollToNode(hasTestTag("test_vibration_button"))
        composeRule.onNodeWithTag("test_vibration_button").performClick()
        composeRule.onNodeWithTag("silent_reminder_toggle").performClick()

        // 4. Test deleting one water log entry (deletes most recent 150 ml -> 3 entries, 850 ml)
        composeRule.onNodeWithTag("water_list").performScrollToNode(hasTestTag("water_log_item_4"))
        composeRule.onAllNodesWithContentDescription("Delete water entry").onFirst().performClick()
        waitForText("3 entries")
        composeRule.onNodeWithTag("water_list").performScrollToNode(hasTestTag("water_progress_ring"))
        waitForText("850 ml")

        // 5. Switch to Exercise tab
        composeRule.onNodeWithTag("nav_tab_exercise").performClick()
        composeRule.onNodeWithTag("exercise_progress_ring").assertIsDisplayed()

        // 6. Test Exercise edit goal button
        composeRule.onNodeWithTag("edit_exercise_goal_button").performClick()
        composeRule.onNodeWithTag("exercise_goal_input").performTextReplacement("30")
        composeRule.onNodeWithTag("exercise_goal_confirm").performClick()
        waitForText("Goal: 30 min")

        // 7. Click ALL 7 Exercise quick-add buttons + Custom (+1,+2,+3,+4,+5,+10,+15 + 5 = 45 min)
        composeRule.onNodeWithTag("exercise_list").performScrollToNode(hasTestTag("add_exercise_1"))
        composeRule.onNodeWithTag("add_exercise_1").performClick()
        composeRule.onNodeWithTag("add_exercise_2").performClick()
        composeRule.onNodeWithTag("add_exercise_3").performClick()
        composeRule.onNodeWithTag("add_exercise_4").performClick()
        composeRule.onNodeWithTag("add_exercise_5").performClick()
        composeRule.onNodeWithTag("add_exercise_10").performClick()
        composeRule.onNodeWithTag("add_exercise_15").performClick()
        composeRule.onNodeWithTag("add_exercise_custom").performClick()
        composeRule.onNodeWithTag("exercise_custom_input").performTextReplacement("5")
        composeRule.onNodeWithTag("exercise_custom_confirm").performClick()

        composeRule.onNodeWithTag("exercise_list").performScrollToNode(hasTestTag("exercise_progress_ring"))
        waitForText("45 min")
        composeRule.onNodeWithText("150%").assertIsDisplayed()

        // 8. Delete one exercise entry (5 min -> 7 sessions, 40 min)
        composeRule.onNodeWithTag("exercise_list").performScrollToNode(hasTestTag("exercise_log_item_8"))
        composeRule.onAllNodesWithContentDescription("Delete exercise entry").onFirst().performClick()
        waitForText("7 sessions")
        composeRule.onNodeWithTag("exercise_list").performScrollToNode(hasTestTag("exercise_progress_ring"))
        waitForText("40 min")

        // 9. Switch back to Water tab and confirm state persisted
        composeRule.onNodeWithTag("nav_tab_water").performClick()
        waitForText("Goal: 2,500 ml")
    }

    private fun waitForText(text: String) {
        composeRule.waitUntil(timeoutMillis = 5000) {
            composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(text).assertIsDisplayed()
    }
}
