package com.robson.financas.ui.fiscal.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp

/** Tendência de preço — pontos mais antigos à esquerda. Só o essencial: linha + ponto final destacado. */
@Composable
fun PriceTrendSparkline(pricesCents: List<Long>, modifier: Modifier = Modifier) {
    val lineColor = MaterialTheme.colorScheme.primary
    if (pricesCents.size < 2) return

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp),
    ) {
        val min = pricesCents.min().toFloat()
        val max = pricesCents.max().toFloat()
        val range = (max - min).coerceAtLeast(1f)
        val stepX = size.width / (pricesCents.size - 1)

        fun yFor(value: Long): Float = size.height - ((value - min) / range) * size.height

        val points = pricesCents.mapIndexed { index, value -> Offset(index * stepX, yFor(value)) }

        for (i in 0 until points.size - 1) {
            drawLine(
                color = lineColor,
                start = points[i],
                end = points[i + 1],
                strokeWidth = 4f,
                cap = StrokeCap.Round,
            )
        }
        drawCircle(color = lineColor, radius = 6f, center = points.last())
    }
}
