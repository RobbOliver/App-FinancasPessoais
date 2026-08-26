package com.robson.financas.ui.designsystem

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Leve encolhimento no toque — a microinteração "pressed" reaproveitada por cards e botões. */
@Composable
fun Modifier.pressScale(interactionSource: InteractionSource, pressedScale: Float = 0.98f): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) pressedScale else 1f, label = "pressScale")
    return this.scale(scale)
}

/**
 * Brilho colorido em volta do elemento (CTAs/FAB) via sombra tingida — evita depender de
 * `Modifier.blur()` (API 31+), seguro no minSdk 30 do app.
 */
fun Modifier.hudGlow(
    color: Color,
    elevation: Dp = 18.dp,
    shape: Shape = CircleShape,
): Modifier = this.shadow(
    elevation = elevation,
    shape = shape,
    ambientColor = color,
    spotColor = color,
)
