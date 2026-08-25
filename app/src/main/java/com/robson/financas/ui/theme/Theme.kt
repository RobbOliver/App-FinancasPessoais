package com.robson.financas.ui.theme

/*
 * Design system: "premium escuro sóbrio".
 *
 * Conceito: superfícies escuras em camadas — cada nível de card é um degrau de cinza
 * mais claro que o de baixo (profundidade por CONTRASTE, não por elevação/sombra pesada),
 * com bordas finas de baixa opacidade e brilho de destaque concentrado no azul da marca.
 *
 * Paleta: preto profundo (background) → grafite (background secundário) → 3 níveis de
 * cinza elevado (superfícies) → azul escuro sofisticado (accent/CTA/foco) com uma variante
 * "luminosa" usada com moderação (gráficos, glow discreto). Sem roxo, magenta, ciano neon
 * ou gradiente colorido — ver `Color.kt` para os tokens nomeados.
 *
 * Regra de aplicação: textura (grão/grade de pontos, `Texture.kt`) só em hero sections e
 * estados vazios, nunca sobre texto de leitura densa ou formulários. Componentes de base
 * ficam em `ui/designsystem/`; as telas não usam `Card`/`OutlinedTextField` crus.
 */

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
    primaryContainer = AccentMutedSurface,
    onPrimaryContainer = NeonBlueLight,
    inversePrimary = NeonBlue,
    secondary = NeonBlueLight,
    onSecondary = PureBlack,
    secondaryContainer = SurfaceElevated,
    onSecondaryContainer = OnSurfaceGray,
    tertiary = NeonBlueLight,
    onTertiary = PureBlack,
    tertiaryContainer = SurfaceElevated,
    onTertiaryContainer = NeonBlueLight,
    background = BgDeep,
    onBackground = OnSurfaceGray,
    surface = SurfaceGray,
    onSurface = OnSurfaceGray,
    surfaceVariant = SurfaceVariantGray,
    onSurfaceVariant = TextSecondary,
    surfaceTint = NeonBlue,
    surfaceDim = BgDeep,
    surfaceBright = SurfaceElevatedHighest,
    surfaceContainerLowest = BgDeep,
    surfaceContainerLow = SurfaceGray,
    surfaceContainer = SurfaceElevated,
    surfaceContainerHigh = SurfaceElevatedHigh,
    surfaceContainerHighest = SurfaceElevatedHighest,
    outline = BorderSubtleStrong,
    outlineVariant = BorderSubtle,
    inverseSurface = OnSurfaceGray,
    inverseOnSurface = BgDeep,
    error = RedExpense,
    onError = Color.White,
    errorContainer = Color(0xFF3A1410),
    onErrorContainer = RedExpense,
    scrim = Color.Black,
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
