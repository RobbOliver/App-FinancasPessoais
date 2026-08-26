package com.robson.financas.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.robson.financas.R

private val BaseTypography = Typography()

/** Display técnico do HUD — usado em display/headline/title. Peso vem de arquivos estáticos. */
val ChakraPetchFamily = FontFamily(
    Font(R.font.chakra_petch_regular, FontWeight.Normal),
    Font(R.font.chakra_petch_medium, FontWeight.Medium),
    Font(R.font.chakra_petch_semibold, FontWeight.SemiBold),
    Font(R.font.chakra_petch_bold, FontWeight.Bold),
)

/**
 * Monoespaçada de dados (valores, rótulos técnicos) — fonte variável, o peso é obtido via
 * eixo `wght` da própria fonte (`FontVariation`), não de arquivos separados.
 */
val JetBrainsMonoFamily = FontFamily(
    Font(R.font.jetbrains_mono, FontWeight.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(R.font.jetbrains_mono, FontWeight.Medium, variationSettings = FontVariation.Settings(FontVariation.weight(500))),
    Font(R.font.jetbrains_mono, FontWeight.SemiBold, variationSettings = FontVariation.Settings(FontVariation.weight(600))),
    Font(R.font.jetbrains_mono, FontWeight.Bold, variationSettings = FontVariation.Settings(FontVariation.weight(700))),
)

/**
 * Estilo para valores/dados (saldo, quantias, percentuais) — monoespaçada com números
 * tabulares, para que colunas de valor alinhem entre si nas listas.
 */
val DataTextStyle = TextStyle(
    fontFamily = JetBrainsMonoFamily,
    fontFeatureSettings = "tnum",
)

/**
 * Rótulo "eyebrow" do HUD — caixa alta, monoespaçada, tracking largo, ciano. É o rótulo que
 * antecede todo dado/título de painel no design aprovado (`// SALDO TOTAL`, `// CARTÃO`...);
 * ficava restrito ao hero do Dashboard antes — agora é o padrão em `SectionHeader`/`CardLabel`.
 */
val EyebrowStyle = TextStyle(
    fontFamily = JetBrainsMonoFamily,
    fontWeight = FontWeight.SemiBold,
    letterSpacing = 1.2.sp,
    color = HudCyanLight,
)

/**
 * Escala tipográfica "Neon HUD": display/headline/title em Chakra Petch (fonte técnica
 * angular, empacotada como recurso local — `app/src/main/res/font/`, licença OFL), body/label
 * seguem a fonte de sistema (Roboto) para manter legibilidade em texto denso. Hierarquia
 * reforçada por peso/tamanho/letter-spacing, como antes.
 */
val AppTypography = BaseTypography.copy(
    displaySmall = BaseTypography.displaySmall.copy(
        fontFamily = ChakraPetchFamily,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.5).sp,
    ),
    headlineLarge = BaseTypography.headlineLarge.copy(
        fontFamily = ChakraPetchFamily,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.5).sp,
    ),
    headlineMedium = BaseTypography.headlineMedium.copy(
        fontFamily = ChakraPetchFamily,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.4).sp,
    ),
    headlineSmall = BaseTypography.headlineSmall.copy(
        fontFamily = ChakraPetchFamily,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.3).sp,
    ),
    titleLarge = BaseTypography.titleLarge.copy(
        fontFamily = ChakraPetchFamily,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.2).sp,
    ),
    titleMedium = BaseTypography.titleMedium.copy(
        fontFamily = ChakraPetchFamily,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.sp,
    ),
    titleSmall = BaseTypography.titleSmall.copy(
        fontFamily = ChakraPetchFamily,
        fontWeight = FontWeight.Medium,
    ),
    bodyLarge = BaseTypography.bodyLarge.copy(
        fontWeight = FontWeight.Normal,
        lineHeight = 22.sp,
    ),
    bodyMedium = BaseTypography.bodyMedium.copy(
        fontWeight = FontWeight.Normal,
        lineHeight = 20.sp,
    ),
    bodySmall = BaseTypography.bodySmall.copy(
        fontWeight = FontWeight.Normal,
        color = TextSecondary,
    ),
    labelLarge = BaseTypography.labelLarge.copy(
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = BaseTypography.labelMedium.copy(
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.8.sp,
    ),
    labelSmall = BaseTypography.labelSmall.copy(
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.4.sp,
        color = TextTertiary,
    ),
)
