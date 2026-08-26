package com.robson.financas.ui.designsystem

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.robson.financas.ui.theme.DotGridOverlayAlpha
import com.robson.financas.ui.theme.GrainOverlayAlpha
import com.robson.financas.ui.theme.HudCyanLight
import kotlin.math.hypot
import kotlin.random.Random

/**
 * Grade de pontos com esmaecimento radial a partir do canto superior direito — a leitura
 * "halftone" do brief, traduzida para mobile sem shader/AGSL: pontos pré-calculados uma vez
 * por tamanho (via `drawWithCache`) e redesenhados como Canvas estático, sem custo por frame.
 * Uso: só em hero sections e estados vazios — nunca atrás de texto de leitura densa.
 */
fun Modifier.dotGridOverlay(
    dotColor: Color = HudCyanLight,
    maxAlpha: Float = DotGridOverlayAlpha,
    spacing: Dp = 18.dp,
    maxRadius: Dp = 1.8.dp,
): Modifier = this.drawWithCache {
    val spacingPx = spacing.toPx()
    val maxRadiusPx = maxRadius.toPx()
    val cols = (size.width / spacingPx).toInt() + 1
    val rows = (size.height / spacingPx).toInt() + 1
    val originX = size.width
    val originY = 0f
    val maxDist = hypot(size.width, size.height).coerceAtLeast(1f)
    val points = buildList(cols * rows) {
        for (r in 0..rows) {
            for (c in 0..cols) {
                val x = c * spacingPx
                val y = r * spacingPx
                val dist = hypot(x - originX, y - originY)
                val falloff = (1f - dist / maxDist).coerceIn(0f, 1f)
                if (falloff > 0.04f) add(Triple(x, y, falloff))
            }
        }
    }
    onDrawBehind {
        points.forEach { (x, y, falloff) ->
            drawCircle(
                color = dotColor.copy(alpha = maxAlpha * falloff),
                radius = (maxRadiusPx * falloff).coerceAtLeast(0.6f),
                center = Offset(x, y),
            )
        }
    }
}

/** Grão discreto e estático — profundidade de textura sutil, quase imperceptível. */
fun Modifier.grainOverlay(
    color: Color = Color.White,
    alpha: Float = GrainOverlayAlpha,
    density: Float = 0.05f,
    seed: Int = 7,
): Modifier = this.drawWithCache {
    val cell = 6.dp.toPx()
    val cols = (size.width / cell).toInt() + 1
    val rows = (size.height / cell).toInt() + 1
    val random = Random(seed)
    val speckles = buildList {
        for (r in 0..rows) {
            for (c in 0..cols) {
                if (random.nextFloat() < density) {
                    val jitterX = random.nextFloat() * cell
                    val jitterY = random.nextFloat() * cell
                    val intensity = 0.3f + random.nextFloat() * 0.7f
                    add(Offset(c * cell + jitterX, r * cell + jitterY) to intensity)
                }
            }
        }
    }
    onDrawBehind {
        speckles.forEach { (offset, intensity) ->
            drawCircle(color = color.copy(alpha = alpha * intensity), radius = 1.1f, center = offset)
        }
    }
}
