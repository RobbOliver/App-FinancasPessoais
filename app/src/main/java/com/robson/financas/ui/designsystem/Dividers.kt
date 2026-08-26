package com.robson.financas.ui.designsystem

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Fio de 1dp no topo da superfície — separa chrome (nav bar, top bar) do conteúdo sem sombra. */
fun Modifier.drawTopDivider(color: Color, thickness: Dp = 1.dp): Modifier = this.drawWithContent {
    drawContent()
    drawLine(
        color = color,
        start = Offset(0f, 0f),
        end = Offset(size.width, 0f),
        strokeWidth = thickness.toPx(),
    )
}

/**
 * "Tick" técnico no canto superior esquerdo — o traço curto que identifica um painel como
 * HUD (mesma leitura do `::before` usado no mockup aprovado). Uso pontual: cards de destaque,
 * nunca em toda superfície com borda.
 */
fun Modifier.hudCornerTick(
    color: Color,
    inset: Dp = 16.dp,
    length: Dp = 22.dp,
    thickness: Dp = 1.dp,
): Modifier = this.drawWithContent {
    drawContent()
    val insetPx = inset.toPx()
    val lengthPx = length.toPx()
    drawLine(
        color = color,
        start = Offset(insetPx, 0f),
        end = Offset(insetPx + lengthPx, 0f),
        strokeWidth = thickness.toPx(),
    )
}
