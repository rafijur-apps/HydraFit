package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val HydraFitColorScheme = lightColorScheme(
    primary = OceanicPrimary,
    onPrimary = PureWhite,
    primaryContainer = SkySoftAccent,
    onPrimaryContainer = DeepOceanInk,
    secondary = OceanicDark,
    onSecondary = PureWhite,
    secondaryContainer = SubtleIce,
    onSecondaryContainer = DeepOceanInk,
    tertiary = ExerciseMint,
    onTertiary = PureWhite,
    tertiaryContainer = ExerciseMintSoft,
    onTertiaryContainer = DeepOceanInk,
    background = SkyBackground,
    onBackground = SlateInk,
    surface = PureWhite,
    onSurface = SlateInk,
    surfaceVariant = SubtleIce,
    onSurfaceVariant = MutedSlate,
    outline = SkySoftAccent,
    error = CoralAlert,
    onError = PureWhite,
    errorContainer = CoralSoft,
    onErrorContainer = CoralAlert,
)

private val HydraFitShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
)

@Composable
fun HydraFitTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = HydraFitColorScheme,
        typography = HydraFitTypography,
        shapes = HydraFitShapes,
        content = content,
    )
}
