package com.robson.financas.ui.common

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.dp
import com.robson.financas.ui.theme.GreenIncome
import com.robson.financas.ui.theme.RedExpense
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class MonthBarData(
    val label: String,
    val incomeCents: Long,
    val expenseCents: Long,
)

@Composable
fun MonthlyBarChart(data: List<MonthBarData>, modifier: Modifier = Modifier) {
    val maxValue = (data.maxOfOrNull { it.incomeCents + it.expenseCents } ?: 0L).coerceAtLeast(1L)

    val progress = remember(data) { data.map { Animatable(0f) } }
    LaunchedEffect(data) {
        progress.forEachIndexed { index, animatable ->
            launch {
                delay(index * 60L)
                animatable.animateTo(1f, animationSpec = tween(650, easing = FastOutSlowInEasing))
            }
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        data.forEachIndexed { index, month ->
            val anim = progress[index].value
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Canvas(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                ) {
                    val maxV = maxValue.toFloat()
                    val total = (month.incomeCents + month.expenseCents).toFloat()
                    val totalFraction = ((total / maxV) * anim).coerceIn(0f, 1f)
                    val totalH = size.height * totalFraction
                    if (totalH <= 0f) return@Canvas

                    val barW = size.width
                    val left = 0f
                    val bottomY = size.height
                    val barTopY = bottomY - totalH
                    val radius = 4.dp.toPx()

                    val incomeH = if (total > 0f) totalH * (month.incomeCents / total) else 0f
                    val expenseH = totalH - incomeH

                    // Clip everything to a rounded rect so the bar has rounded top corners
                    val clipPath = Path().apply {
                        addRoundRect(
                            RoundRect(
                                left = left,
                                top = barTopY,
                                right = left + barW,
                                bottom = bottomY,
                                topLeftCornerRadius = CornerRadius(radius),
                                topRightCornerRadius = CornerRadius(radius),
                                bottomLeftCornerRadius = CornerRadius(0f),
                                bottomRightCornerRadius = CornerRadius(0f),
                            ),
                        )
                    }
                    clipPath(clipPath) {
                        // Expense on top (red)
                        if (expenseH > 0f) {
                            drawRect(
                                color = RedExpense,
                                topLeft = Offset(left, barTopY),
                                size = Size(barW, expenseH),
                            )
                        }
                        // Income at bottom (green)
                        if (incomeH > 0f) {
                            drawRect(
                                color = GreenIncome,
                                topLeft = Offset(left, barTopY + expenseH),
                                size = Size(barW, incomeH),
                            )
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    month.label,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
