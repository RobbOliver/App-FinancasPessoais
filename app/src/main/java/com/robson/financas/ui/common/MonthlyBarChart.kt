package com.robson.financas.ui.common

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    val maxValue = (data.maxOfOrNull { maxOf(it.incomeCents, it.expenseCents) } ?: 0L).coerceAtLeast(1L)

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
            val monthProgress = progress[index].value
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Box(
                        modifier = Modifier
                            .width(8.dp)
                            .fillMaxHeight(fraction = ((month.incomeCents.toFloat() / maxValue) * monthProgress).coerceIn(0f, 1f))
                            .background(GreenIncome, RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)),
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Box(
                        modifier = Modifier
                            .width(8.dp)
                            .fillMaxHeight(fraction = ((month.expenseCents.toFloat() / maxValue) * monthProgress).coerceIn(0f, 1f))
                            .background(RedExpense, RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)),
                    )
                }
                Text(
                    month.label,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}
