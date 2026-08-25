package com.robson.financas.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.unit.dp

@Composable
fun GoalProgressBar(
    goalCents: Long,
    spentCents: Long,
    modifier: Modifier = Modifier,
) {
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val fillColor = MaterialTheme.colorScheme.primary
    val fraction = if (goalCents > 0) {
        (1f - spentCents.toFloat() / goalCents.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(10.dp),
    ) {
        val cornerRadius = CornerRadius(size.height / 2, size.height / 2)
        drawRoundRect(color = trackColor, cornerRadius = cornerRadius)
        if (fraction > 0f) {
            drawRoundRect(
                color = fillColor,
                size = size.copy(width = size.width * fraction),
                cornerRadius = cornerRadius,
            )
        }
    }
}
