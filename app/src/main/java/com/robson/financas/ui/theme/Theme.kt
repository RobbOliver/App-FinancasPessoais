package com.robson.financas.ui.theme

/*
 * Design system: "Neon HUD" — painel de cockpit/sci-fi.
 *
 * Conceito: fundo quase preto, bordas finas ciano (com "tick" técnico de canto em
 * `Dividers.kt#hudCornerTick`), texto de dado em monoespaçada, glow ciano controlado nos
 * elementos interativos, scanline sutil em hero sections (`Texture.kt#scanlineOverlay`).
 * Profundidade ainda vem de CONTRASTE entre níveis de cinza, não de elevação/sombra pesada
 * — essa base do redesign anterior foi mantida; o que muda é o acento (ciano/magenta no
 * lugar do azul) e a forma (cantos quase retos no lugar de arredondados).
 *
 * Paleta: preto profundo (background) → grafite (background secundário) → 3 níveis de
 * cinza elevado (superfícies) → ciano técnico (accent/CTA/foco/dados) com magenta como
 * acento secundário raro (nunca em CTA) — ver `Color.kt` para os tokens nomeados.
 *
 * Regra de aplicação: textura (grão/grade de pontos/scanline, `Texture.kt`) só em hero
 * sections e estados vazios, nunca sobre texto de leitura densa ou formulários. Componentes
 * de base ficam em `ui/designsystem/`; as telas não usam `Card`/`OutlinedTextField` crus.
 */

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val AppDarkColors = darkColorScheme(
    primary = HudCyan,
    onPrimary = Color.White,
    primaryContainer = AccentMutedSurface,
    onPrimaryContainer = HudCyanLight,
    inversePrimary = HudCyan,
    secondary = HudCyanLight,
    onSecondary = PureBlack,
    secondaryContainer = SurfaceElevated,
    onSecondaryContainer = OnSurfaceGray,
    tertiary = HudCyanLight,
    onTertiary = PureBlack,
    tertiaryContainer = SurfaceElevated,
    onTertiaryContainer = HudCyanLight,
    background = BgDeep,
    onBackground = OnSurfaceGray,
    surface = SurfaceGray,
    onSurface = OnSurfaceGray,
    surfaceVariant = SurfaceVariantGray,
    onSurfaceVariant = TextSecondary,
    surfaceTint = HudCyan,
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
    extraSmall = RoundedCornerShape(2.dp),
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(6.dp),
    large = RoundedCornerShape(8.dp),
    extraLarge = RoundedCornerShape(12.dp),
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
