package com.robson.financas.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val AppDarkColors = darkColorScheme(
    primary = NeonBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF00315C),
    onPrimaryContainer = NeonBlueLight,
    secondary = NeonBlueLight,
    onSecondary = PureBlack,
    secondaryContainer = SurfaceVariantGray,
    onSecondaryContainer = OnSurfaceGray,
    tertiary = NeonBlueLight,
    onTertiary = PureBlack,
    tertiaryContainer = SurfaceVariantGray,
    onTertiaryContainer = NeonBlueLight,
    background = PureBlack,
    onBackground = OnSurfaceGray,
    surface = SurfaceGray,
    onSurface = OnSurfaceGray,
    surfaceVariant = SurfaceVariantGray,
    onSurfaceVariant = OnSurfaceMutedGray,
    outline = OutlineGray,
    outlineVariant = OutlineGray,
    error = RedExpense,
    onError = Color.White,
    errorContainer = Color(0xFF4A1410),
    onErrorContainer = RedExpense,
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun FinancasPessoaisTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppDarkColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
