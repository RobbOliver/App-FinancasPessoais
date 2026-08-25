package com.robson.financas.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val BaseTypography = Typography()

/**
 * Escala tipográfica: fonte de sistema (Roboto) — sem custom font, hierarquia vem de
 * peso/tamanho/letter-spacing. Títulos fortes e compactos (letter-spacing negativo),
 * corpo neutro para leitura, labels/captions com tracking levemente positivo (uso em
 * caixa alta, ex. cabeçalhos de seção).
 */
val AppTypography = BaseTypography.copy(
    displaySmall = BaseTypography.displaySmall.copy(
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.5).sp,
    ),
    headlineLarge = BaseTypography.headlineLarge.copy(
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.5).sp,
    ),
    headlineMedium = BaseTypography.headlineMedium.copy(
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.4).sp,
    ),
    headlineSmall = BaseTypography.headlineSmall.copy(
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.3).sp,
    ),
    titleLarge = BaseTypography.titleLarge.copy(
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.2).sp,
    ),
    titleMedium = BaseTypography.titleMedium.copy(
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.sp,
    ),
    titleSmall = BaseTypography.titleSmall.copy(
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
