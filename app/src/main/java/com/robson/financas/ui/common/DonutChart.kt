package com.robson.financas.ui.common

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.robson.financas.data.local.relation.CategoryExpenseSlice
import com.robson.financas.util.CurrencyFormatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DonutChart(slices: List<CategoryExpenseSlice>, modifier: Modifier = Modifier) {
    val total = slices.sumOf { it.totalCents }.coerceAtLeast(1)

    val progress = remember(slices) { slices.map { Animatable(0f) } }
    LaunchedEffect(slices) {
        progress.forEachIndexed { index, animatable ->
            launch {
                delay(index * 70L)
                animatable.animateTo(1f, animationSpec = tween(500, easing = FastOutSlowInEasing))
            }
        }
    }

    Column(modifier = modifier) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(160.dp)) {
                var startAngle = -90f
                val strokeWidth = size.minDimension * 0.22f
                slices.forEachIndexed { index, slice ->
                    val sweep = 360f * slice.totalCents / total
                    val animatedSweep = sweep * progress[index].value
                    drawArc(
                        color = ColorCatalog.toColor(slice.categoryColorHex),
                        startAngle = startAngle,
                        sweepAngle = animatedSweep,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                    )
                    startAngle += sweep
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        slices.forEach { slice ->
            val percent = (slice.totalCents * 100 / total).toInt()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(ColorCatalog.toColor(slice.categoryColorHex)),
                    )
                    Text(
                        slice.categoryName,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
                Text(
                    "$percent% · ${CurrencyFormatter.formatCents(slice.totalCents)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
