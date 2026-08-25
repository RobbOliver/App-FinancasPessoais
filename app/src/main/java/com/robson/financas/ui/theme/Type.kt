package com.robson.financas.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val BaseTypography = Typography()

val AppTypography = BaseTypography.copy(
    headlineMedium = BaseTypography.headlineMedium.copy(
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.5).sp,
    ),
    titleLarge = BaseTypography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
    titleMedium = BaseTypography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    titleSmall = BaseTypography.titleSmall.copy(fontWeight = FontWeight.Medium),
    bodyLarge = BaseTypography.bodyLarge.copy(fontWeight = FontWeight.Medium),
)
