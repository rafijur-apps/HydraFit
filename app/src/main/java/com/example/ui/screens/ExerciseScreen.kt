package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.ExerciseLogEntity
import com.example.ui.components.HydraProgressRingCard
import com.example.ui.components.NumericInputDialog
import com.example.ui.components.WeeklyMonthlySummaryCard
import com.example.ui.theme.CoralAlert
import com.example.ui.theme.DeepOceanInk
import com.example.ui.theme.ExerciseMint
import com.example.ui.theme.ExerciseMintSoft
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.OceanicBright
import com.example.ui.theme.OceanicPrimary
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SkySoftAccent
import com.example.ui.theme.SubtleIce
import com.example.ui.viewmodel.ExerciseUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExerciseScreen(
    state: ExerciseUiState,
    onAddExercise: (Int) -> Unit,
    onDeleteExerciseLog: (Long) -> Unit,
    onUpdateGoal: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showGoalDialog by remember { mutableStateOf(false) }
    var showCustomDialog by remember { mutableStateOf(false) }

    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.US) }

    val remainingLabel = if (state.completedMins >= state.goalMins) {
        val extra = state.completedMins - state.goalMins
        if (extra > 0) "Daily goal reached! (+$extra min)" else "Daily goal reached!"
    } else {
        "${state.remainingMins} min remaining"
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("exercise_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Header + Editable Daily Goal Pill
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Exercise",
                        style = MaterialTheme.typography.headlineMedium,
                        color = DeepOceanInk,
                    )
                    Text(
                        text = "Daily Movement Tracker",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedSlate,
                    )
                }

                Surface(
                    onClick = { showGoalDialog = true },
                    shape = RoundedCornerShape(50),
                    color = PureWhite,
                    shadowElevation = 2.dp,
                    border = BorderStroke(1.dp, SkySoftAccent),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("edit_exercise_goal_button"),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = "Goal: ${state.goalMins} min",
                            style = MaterialTheme.typography.labelLarge,
                            color = OceanicPrimary,
                        )
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit daily exercise goal",
                            tint = OceanicPrimary,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }

        // Progress Ring showing minutes completed vs target
        item {
            HydraProgressRingCard(
                progressFraction = state.progressFraction,
                percentage = state.percentage,
                primaryValueText = "${state.completedMins} min",
                secondaryValueText = "of ${state.goalMins} min daily goal",
                remainingText = remainingLabel,
                goalReached = state.completedMins >= state.goalMins,
                accentStart = OceanicBright,
                accentEnd = ExerciseMint,
                ringTestTag = "exercise_progress_ring",
            )
        }

        // Quick-Add Grid: +1m, +2m, +3m, +4m, +5m, +10m, +15m, Custom mins
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    Text(
                        text = "Quick Add Workout",
                        style = MaterialTheme.typography.titleMedium,
                        color = DeepOceanInk,
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ExerciseQuickAddButton("+1m", "add_exercise_1", { onAddExercise(1) }, Modifier.weight(1f))
                        ExerciseQuickAddButton("+2m", "add_exercise_2", { onAddExercise(2) }, Modifier.weight(1f))
                        ExerciseQuickAddButton("+3m", "add_exercise_3", { onAddExercise(3) }, Modifier.weight(1f))
                        ExerciseQuickAddButton("+4m", "add_exercise_4", { onAddExercise(4) }, Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ExerciseQuickAddButton("+5m", "add_exercise_5", { onAddExercise(5) }, Modifier.weight(1f))
                        ExerciseQuickAddButton("+10m", "add_exercise_10", { onAddExercise(10) }, Modifier.weight(1f))
                        ExerciseQuickAddButton("+15m", "add_exercise_15", { onAddExercise(15) }, Modifier.weight(1f))
                        OutlinedButton(
                            onClick = { showCustomDialog = true },
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.5.dp, OceanicPrimary),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = SubtleIce,
                                contentColor = OceanicPrimary,
                            ),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 10.dp),
                            modifier = Modifier
                                .weight(1.35f)
                                .height(48.dp)
                                .testTag("add_exercise_custom"),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Custom",
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        }

        // Weekly & Monthly Workout Breakdown
        item {
            WeeklyMonthlySummaryCard(
                title = "Weekly & Monthly Workout Breakdown",
                weeklyValueText = "${state.periodStats.weeklyTotal} min",
                weeklySubtitle = "${state.periodStats.weeklyActiveDays}/7 active days",
                monthlyValueText = "${state.periodStats.monthlyTotal} min",
                monthlySubtitle = "${state.periodStats.monthlyActiveDays}/30 active days",
                dailyTrend = state.periodStats.dailyTrend,
                dailyGoalValue = state.goalMins,
                accentColor = OceanicPrimary,
                formatBarValue = { mins -> "${mins}m" },
            )
        }

        // Today's Logs Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Today's Workout Logs",
                    style = MaterialTheme.typography.titleMedium,
                    color = DeepOceanInk,
                )
                Text(
                    text = "${state.todayLogs.size} ${if (state.todayLogs.size == 1) "session" else "sessions"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedSlate,
                )
            }
        }

        if (state.todayLogs.isEmpty()) {
            item {
                EmptyLogCard(
                    icon = Icons.Filled.FitnessCenter,
                    message = "No workouts logged today yet. Every minute counts—tap a quick-add button above!",
                )
            }
        } else {
            items(
                items = state.todayLogs,
                key = { it.id },
            ) { log ->
                ExerciseLogRowCard(
                    log = log,
                    timeText = timeFormat.format(Date(log.timestampMillis)),
                    onDelete = { onDeleteExerciseLog(log.id) },
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    if (showGoalDialog) {
        NumericInputDialog(
            title = "Edit Daily Exercise Goal",
            subtitle = "Set your daily active workout target in minutes.",
            initialValue = state.goalMins.toString(),
            unitLabel = "min",
            presets = listOf(15, 20, 30, 45, 60),
            minValue = 1,
            maxValue = 300,
            confirmLabel = "Save Goal",
            inputTestTag = "exercise_goal_input",
            confirmTestTag = "exercise_goal_confirm",
            onDismiss = { showGoalDialog = false },
            onConfirm = { newGoal ->
                onUpdateGoal(newGoal)
                showGoalDialog = false
            },
        )
    }

    if (showCustomDialog) {
        NumericInputDialog(
            title = "Custom Workout Duration",
            subtitle = "Enter the number of active exercise minutes completed.",
            initialValue = "20",
            unitLabel = "min",
            presets = listOf(7, 12, 20, 30, 45),
            minValue = 1,
            maxValue = 300,
            confirmLabel = "Add Workout",
            inputTestTag = "exercise_custom_input",
            confirmTestTag = "exercise_custom_confirm",
            onDismiss = { showCustomDialog = false },
            onConfirm = { mins ->
                onAddExercise(mins)
                showCustomDialog = false
            },
        )
    }
}

@Composable
private fun ExerciseQuickAddButton(
    label: String,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = OceanicPrimary,
            contentColor = PureWhite,
        ),
        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 10.dp),
        modifier = modifier
            .height(48.dp)
            .testTag(testTag),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
    }
}

@Composable
private fun ExerciseLogRowCard(
    log: ExerciseLogEntity,
    timeText: String,
    onDelete: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("exercise_log_item_${log.id}"),
        shape = RoundedCornerShape(18.dp),
        color = PureWhite,
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 6.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(ExerciseMintSoft),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.FitnessCenter,
                        contentDescription = null,
                        tint = ExerciseMint,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Column {
                    Text(
                        text = "+${log.minutes} min",
                        style = MaterialTheme.typography.titleMedium,
                        color = DeepOceanInk,
                    )
                    Text(
                        text = timeText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedSlate,
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("delete_exercise_log_${log.id}"),
            ) {
                Icon(
                    imageVector = Icons.Filled.DeleteOutline,
                    contentDescription = "Delete exercise entry",
                    tint = CoralAlert,
                )
            }
        }
    }
}
