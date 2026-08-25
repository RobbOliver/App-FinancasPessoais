package com.robson.financas.ui.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.robson.financas.ui.theme.GreenIncome
import com.robson.financas.ui.theme.NeonBlueLight

/** Pago (verde) vs. agendado (azul) — toque opcional alterna o status sem abrir a edição. */
@Composable
fun StatusDot(
    isPaid: Boolean,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    var dot = modifier
        .size(10.dp)
        .clip(CircleShape)
        .background(if (isPaid) GreenIncome else NeonBlueLight)
    if (onClick != null) {
        dot = dot.clickable(onClick = onClick)
    }
    Box(modifier = dot)
}
