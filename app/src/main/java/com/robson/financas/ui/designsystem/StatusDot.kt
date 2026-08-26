package com.robson.financas.ui.designsystem

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import com.robson.financas.ui.theme.GreenIncome
import com.robson.financas.ui.theme.HudCyanLight

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
        .background(if (isPaid) GreenIncome else HudCyanLight)
    if (onClick != null) {
        dot = dot.clickable(onClick = onClick)
    }
    Box(modifier = dot)
}

/** Pontinho "ao vivo" do HUD — pulsa escala/alfa continuamente, sem estado (decorativo). */
@Composable
fun LiveDot(
    modifier: Modifier = Modifier,
    color: androidx.compose.ui.graphics.Color = HudCyanLight,
    size: androidx.compose.ui.unit.Dp = 6.dp,
) {
    val transition = rememberInfiniteTransition(label = "liveDotPulse")
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(1200), repeatMode = RepeatMode.Reverse),
        label = "liveDotScale",
    )
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(tween(1200), repeatMode = RepeatMode.Reverse),
        label = "liveDotAlpha",
    )
    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .alpha(alpha)
            .clip(CircleShape)
            .background(color),
    )
}
