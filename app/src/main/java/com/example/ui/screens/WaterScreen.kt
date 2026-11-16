package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.local.WaterLogEntity
import com.example.ui.components.HydraProgressRingCard
import com.example.ui.components.NumericInputDialog
import com.example.ui.components.WeeklyMonthlySummaryCard
import com.example.ui.theme.CoralAlert
import com.example.ui.theme.DeepOceanInk
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.OceanicBright
import com.example.ui.theme.OceanicDark
import com.example.ui.theme.OceanicPrimary
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SkyBackground
import com.example.ui.theme.SkySoftAccent
import com.example.ui.theme.SubtleIce
import com.example.ui.viewmodel.WaterUiState
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WaterScreen(
    state: WaterUiState,
    onAddWater: (Int) -> Unit,
    onDeleteWaterLog: (Long) -> Unit,
    onUpdateGoal: (Int) -> Unit,
    onToggleSilentReminder: (Boolean) -> Unit,
    onTestVibration: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showGoalDialog by remember { mutableStateOf(false) }
    var showCustomDialog by remember { mutableStateOf(false) }

    val numberFormat = remember { NumberFormat.getIntegerInstance(Locale.US) }
    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.US) }

    val goalText = numberFormat.format(state.goalMl)
    val consumedText = "${numberFormat.format(state.consumedMl)} ml"
    val remainingLabel = if (state.consumedMl >= state.goalMl) {
        val extra = state.consumedMl - state.goalMl
        if (extra > 0) "Daily goal reached! (+${numberFormat.format(extra)} ml)" else "Daily goal reached!"
    } else {
        "${numberFormat.format(state.remainingMl)} ml remaining"
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("water_list"),
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
                        text = "HydraFit",
                        style = MaterialTheme.typography.headlineMedium,
                        color = DeepOceanInk,
                    )
                    Text(
                        text = "Daily Water Intake",
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
                        .testTag("edit_water_goal_button"),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = "Goal: $goalText ml",
                            style = MaterialTheme.typography.labelLarge,
                            color = OceanicPrimary,
                        )
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit daily water goal",
                            tint = OceanicPrimary,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }

        // Prominent Circular Progress Indicator
        item {
            HydraProgressRingCard(
                progressFraction = state.progressFraction,
                percentage = state.percentage,
                primaryValueText = consumedText,
                secondaryValueText = "of $goalText ml daily goal",
                remainingText = remainingLabel,
                goalReached = state.consumedMl >= state.goalMl,
                accentStart = OceanicBright,
                accentEnd = OceanicPrimary,
                ringTestTag = "water_progress_ring",
            )
        }

        // Quick-Add Controls
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
                        text = "Quick Add Water",
                        style = MaterialTheme.typography.titleMedium,
                        color = DeepOceanInk,
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        WaterQuickAddButton(
                            label = "+100 ml",
                            testTag = "add_water_100",
                            onClick = { onAddWater(100) },
                            modifier = Modifier.weight(1f),
                        )
                        WaterQuickAddButton(
                            label = "+250 ml",
                            testTag = "add_water_250",
                            onClick = { onAddWater(250) },
                            modifier = Modifier.weight(1f),
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        WaterQuickAddButton(
                            label = "+500 ml",
                            testTag = "add_water_500",
                            onClick = { onAddWater(500) },
                            modifier = Modifier.weight(1f),
                        )
                        OutlinedButton(
                            onClick = { showCustomDialog = true },
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.5.dp, OceanicPrimary),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = SubtleIce,
                                contentColor = OceanicPrimary,
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("add_water_custom"),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Custom ml",
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        }

        // Background Silent Reminder Card
        item {
            SilentReminderCard(
                enabled = state.silentReminderEnabled,
                isActiveHoursNow = state.isCurrentlyActiveHours,
                onToggle = onToggleSilentReminder,
                onTestVibration = onTestVibration,
            )
        }

        // Weekly & Monthly Summary Stats (Total Liters)
        item {
            val weeklyLiters = formatLiters(state.periodStats.weeklyTotal)
            val monthlyLiters = formatLiters(state.periodStats.monthlyTotal)
            WeeklyMonthlySummaryCard(
                title = "Weekly & Monthly Summary",
                weeklyValueText = "$weeklyLiters L",
                weeklySubtitle = "${state.periodStats.weeklyActiveDays}/7 active days",
                monthlyValueText = "$monthlyLiters L",
                monthlySubtitle = "${state.periodStats.monthlyActiveDays}/30 active days",
                dailyTrend = state.periodStats.dailyTrend,
                dailyGoalValue = state.goalMl,
                accentColor = OceanicPrimary,
                formatBarValue = { ml ->
                    if (ml >= 1000) String.format(Locale.US, "%.1fL", ml / 1000f) else "${ml}"
                },
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
                    text = "Today's Logs",
                    style = MaterialTheme.typography.titleMedium,
                    color = DeepOceanInk,
                )
                Text(
                    text = "${state.todayLogs.size} ${if (state.todayLogs.size == 1) "entry" else "entries"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedSlate,
                )
            }
        }

        if (state.todayLogs.isEmpty()) {
            item {
                EmptyLogCard(
                    icon = Icons.Filled.WaterDrop,
                    message = "No water logged today yet. Tap a quick-add button above to start hydrating!",
                )
            }
        } else {
            items(
                items = state.todayLogs,
                key = { it.id },
            ) { log ->
                WaterLogRowCard(
                    log = log,
                    timeText = timeFormat.format(Date(log.timestampMillis)),
                    formattedMl = numberFormat.format(log.amountMl),
                    onDelete = { onDeleteWaterLog(log.id) },
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    if (showGoalDialog) {
        NumericInputDialog(
            title = "Edit Daily Water Goal",
            subtitle = "Set your daily hydration target in milliliters (e.g., 2000 ml = 2 Liters).",
            initialValue = state.goalMl.toString(),
            unitLabel = "ml",
            presets = listOf(1500, 2000, 2500, 3000, 3500),
            minValue = 250,
            maxValue = 10000,
            confirmLabel = "Save Goal",
            inputTestTag = "water_goal_input",
            confirmTestTag = "water_goal_confirm",
            onDismiss = { showGoalDialog = false },
            onConfirm = { newGoal ->
                onUpdateGoal(newGoal)
                showGoalDialog = false
            },
        )
    }

    if (showCustomDialog) {
        NumericInputDialog(
            title = "Custom Water Amount",
            subtitle = "Enter the volume you drank in milliliters.",
            initialValue = "350",
            unitLabel = "ml",
            presets = listOf(150, 300, 350, 600, 750),
            minValue = 10,
            maxValue = 5000,
            confirmLabel = "Add Water",
            inputTestTag = "water_custom_input",
            confirmTestTag = "water_custom_confirm",
            onDismiss = { showCustomDialog = false },
            onConfirm = { ml ->
                onAddWater(ml)
                showCustomDialog = false
            },
        )
    }
}

@Composable
private fun WaterQuickAddButton(
    label: String,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = OceanicPrimary,
            contentColor = PureWhite,
        ),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
        modifier = modifier
            .height(50.dp)
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
private fun SilentReminderCard(
    enabled: Boolean,
    isActiveHoursNow: Boolean,
    onToggle: (Boolean) -> Unit,
    onTestVibration: () -> Unit,
) {
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (enabled) SkyBackground else SubtleIce),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = if (enabled) Icons.Filled.Vibration else Icons.Filled.NotificationsOff,
                            contentDescription = null,
                            tint = if (enabled) OceanicPrimary else MutedSlate,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    Column {
                        Text(
                            text = "Enable Silent Reminder",
                            style = MaterialTheme.typography.titleMedium,
                            color = DeepOceanInk,
                        )
                        Text(
                            text = "Vibration only every 90 min • No audio",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedSlate,
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Switch(
                    checked = enabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = PureWhite,
                        checkedTrackColor = OceanicPrimary,
                        uncheckedThumbColor = OceanicDark,
                        uncheckedTrackColor = SkyBackground,
                        uncheckedBorderColor = SkySoftAccent,
                    ),
                    modifier = Modifier.testTag("silent_reminder_toggle"),
                )
            }

            AnimatedVisibility(visible = enabled) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = SubtleIce,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isActiveHoursNow) {
                                        "Active Window (10:00 AM – 2:00 AM)"
                                    } else {
                                        "Quiet Hours (2:01 AM – 9:59 AM)"
                                    },
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isActiveHoursNow) OceanicPrimary else MutedSlate,
                                )
                                Text(
                                    text = "Pulse pattern: 300ms – 100ms – 300ms",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MutedSlate,
                                )
                            }
                            TextButton(
                                onClick = onTestVibration,
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("test_vibration_button"),
                            ) {
                                Text(
                                    text = "Test Pulse",
                                    color = OceanicPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WaterLogRowCard(
    log: WaterLogEntity,
    timeText: String,
    formattedMl: String,
    onDelete: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("water_log_item_${log.id}"),
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
                        .background(SkyBackground),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.WaterDrop,
                        contentDescription = null,
                        tint = OceanicPrimary,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Column {
                    Text(
                        text = "+$formattedMl ml",
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
                    .testTag("delete_water_log_${log.id}"),
            ) {
                Icon(
                    imageVector = Icons.Filled.DeleteOutline,
                    contentDescription = "Delete water entry",
                    tint = CoralAlert,
                )
            }
        }
    }
}

@Composable
fun EmptyLogCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    message: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = PureWhite.copy(alpha = 0.88f),
        border = BorderStroke(1.dp, SkySoftAccent),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = OceanicBright,
                modifier = Modifier.size(34.dp),
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MutedSlate,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private fun formatLiters(totalMl: Int): String {
    val liters = totalMl / 1000.0
    return String.format(Locale.US, "%.2f", liters)
}
