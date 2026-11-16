package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DeepOceanInk
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.OceanicBright
import com.example.ui.theme.OceanicPrimary
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SkyBackground
import com.example.ui.theme.SkySoftAccent
import com.example.ui.theme.SubtleIce

@Composable
fun HydraProgressRingCard(
    progressFraction: Float,
    percentage: Int,
    primaryValueText: String,
    secondaryValueText: String,
    remainingText: String,
    goalReached: Boolean,
    accentStart: Color = OceanicBright,
    accentEnd: Color = OceanicPrimary,
    trackColor: Color = SkyBackground,
    ringTestTag: String,
    modifier: Modifier = Modifier,
) {
    val animatedSweep by animateFloatAsState(
        targetValue = progressFraction.coerceIn(0f, 1f) * 360f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "progress_ring_sweep",
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag(ringTestTag),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier.size(220.dp),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokePx = 18.dp.toPx()
                    val inset = strokePx / 2f + 6.dp.toPx()
                    val arcSize = Size(size.width - inset * 2f, size.height - inset * 2f)
                    val topLeft = Offset(inset, inset)

                    // Soft inner glow backdrop
                    drawCircle(
                        color = SubtleIce,
                        radius = (size.minDimension / 2f) - inset - strokePx * 0.45f,
                    )

                    // Background track ring
                    drawArc(
                        color = trackColor,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokePx, cap = StrokeCap.Round),
                    )

                    // Progress arc
                    if (animatedSweep > 0.5f) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = listOf(accentStart, accentEnd, accentStart),
                            ),
                            startAngle = -90f,
                            sweepAngle = animatedSweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokePx, cap = StrokeCap.Round),
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 24.dp),
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (goalReached) accentEnd.copy(alpha = 0.14f) else SubtleIce,
                    ) {
                        Text(
                            text = "$percentage%",
                            style = MaterialTheme.typography.labelLarge,
                            color = accentEnd,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = primaryValueText,
                        style = MaterialTheme.typography.displaySmall,
                        color = DeepOceanInk,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = secondaryValueText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedSlate,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (goalReached) accentEnd.copy(alpha = 0.12f) else SkyBackground.copy(alpha = 0.7f),
                ) {
                    Text(
                        text = remainingText,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (goalReached) accentEnd else DeepOceanInk,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }
        }
    }
}
