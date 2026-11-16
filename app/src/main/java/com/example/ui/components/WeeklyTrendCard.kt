package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.repository.DailyTrendPoint
import com.example.ui.theme.DeepOceanInk
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.OceanicPrimary
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SkyBackground
import com.example.ui.theme.SkySoftAccent
import com.example.ui.theme.SubtleIce

@Composable
fun WeeklyMonthlySummaryCard(
    title: String,
    weeklyValueText: String,
    weeklySubtitle: String,
    monthlyValueText: String,
    monthlySubtitle: String,
    dailyTrend: List<DailyTrendPoint>,
    dailyGoalValue: Int,
    accentColor: Color = OceanicPrimary,
    formatBarValue: (Int) -> String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = DeepOceanInk,
            )
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SummaryStatTile(
                    label = "This Week",
                    value = weeklyValueText,
                    subtitle = weeklySubtitle,
                    accentColor = accentColor,
                    modifier = Modifier.weight(1f),
                )
                SummaryStatTile(
                    label = "This Month (30d)",
                    value = monthlyValueText,
                    subtitle = monthlySubtitle,
                    accentColor = accentColor,
                    modifier = Modifier.weight(1f),
                )
            }

            if (dailyTrend.isNotEmpty()) {
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "7-Day Breakdown",
                    style = MaterialTheme.typography.labelMedium,
                    color = MutedSlate,
                )
                Spacer(modifier = Modifier.height(10.dp))

                val maxValue = maxOf(
                    dailyGoalValue.coerceAtLeast(1),
                    dailyTrend.maxOfOrNull { it.totalValue } ?: 1,
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 112.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    dailyTrend.forEach { point ->
                        val ratio = (point.totalValue.toFloat() / maxValue.toFloat()).coerceIn(0f, 1f)
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                        ) {
                            Text(
                                text = if (point.totalValue > 0) formatBarValue(point.totalValue) else "0",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (point.isToday) accentColor else MutedSlate,
                                maxLines = 1,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.62f)
                                    .height(68.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SkyBackground.copy(alpha = 0.65f)),
                                contentAlignment = Alignment.BottomCenter,
                            ) {
                                val barHeightDp = (68f * ratio).dp.coerceAtLeast(
                                    if (point.totalValue > 0) 6.dp else 0.dp,
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(barHeightDp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (point.isToday) accentColor else SkySoftAccent.copy(alpha = 0.95f),
                                        ),
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = point.shortLabel,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (point.isToday) FontWeight.Bold else FontWeight.Normal,
                                color = if (point.isToday) DeepOceanInk else MutedSlate,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryStatTile(
    label: String,
    value: String,
    subtitle: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = SubtleIce,
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MutedSlate,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = accentColor,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MutedSlate,
            )
        }
    }
}
