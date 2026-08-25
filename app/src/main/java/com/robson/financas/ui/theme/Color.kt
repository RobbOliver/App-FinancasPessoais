package com.robson.financas.ui.theme

import androidx.compose.ui.graphics.Color

// Accent — azul escuro sofisticado, sem virar neon/cyberpunk.
val NeonBlue = Color(0xFF0A84FF)
val NeonBlueLight = Color(0xFF64D2FF)

/** Fundo de superfícies tingidas de azul (chips ativos, containers de destaque). */
val AccentMutedSurface = Color(0xFF13233A)
val AccentBorder = Color(0xFF2C4A6E)

// Escala de profundidade — cada nível um degrau mais claro que o de baixo,
// para que a hierarquia venha do contraste de superfície, não de sombra pesada.
val PureBlack = Color(0xFF000000)
val BgDeep = Color(0xFF050506)
val BgSecondary = Color(0xFF0E0E10)
val SurfaceGray = Color(0xFF1A1A1D)
val SurfaceElevated = Color(0xFF212124)
val SurfaceElevatedHigh = Color(0xFF29292D)
val SurfaceElevatedHighest = Color(0xFF313136)
val SurfaceVariantGray = Color(0xFF232326)

val OutlineGray = Color(0xFF3A3A3C)
val BorderSubtle = Color(0x1FFFFFFF)
val BorderSubtleStrong = Color(0x33FFFFFF)

val OnSurfaceGray = Color(0xFFF2F2F5)
val TextSecondary = Color(0xFFA8A8B0)
val TextTertiary = Color(0xFF6E6E76)
val OnSurfaceMutedGray = TextSecondary

val RedExpense = Color(0xFFFF453A)
val GreenIncome = Color(0xFF30D158)

// Alphas usados pelos overlays de textura (grão/grade de pontos) em Texture.kt —
// deliberadamente muito baixos, nunca sobre texto de leitura densa.
const val GrainOverlayAlpha = 0.035f
const val DotGridOverlayAlpha = 0.05f
