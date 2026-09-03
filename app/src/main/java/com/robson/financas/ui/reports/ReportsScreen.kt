package com.robson.financas.ui.reports

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.robson.financas.ui.designsystem.AppCard
import com.robson.financas.ui.designsystem.SectionHeader
import com.robson.financas.ui.theme.GreenIncome
import com.robson.financas.ui.theme.Spacing
import com.robson.financas.util.CurrencyFormatter
import com.robson.financas.util.DateFormatter
import java.time.YearMonth

private val RedExpense = Color(0xFFE53935)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    onBack: () -> Unit,
    viewModel: ReportsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                expandedHeight = 40.dp,
                title = { Text("Relatórios") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = Spacing.xl),
        ) {
            // Month navigation for chart
            item {
                MonthNavigationBar(
                    yearMonth = uiState.selectedMonth,
                    onPrevious = viewModel::previousMonth,
                    onNext = viewModel::nextMonth,
                )
            }

            // Summary row
            item {
                val selectedProjection = uiState.projections.find { it.yearMonth == uiState.selectedMonth }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.lg, vertical = Spacing.xs),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    SummaryChip(
                        label = "Receita",
                        valueCents = selectedProjection?.projectedIncomeCents ?: uiState.chartDays.lastOrNull()?.cumulativeIncome ?: 0L,
                        color = GreenIncome,
                        modifier = Modifier.weight(1f),
                    )
                    SummaryChip(
                        label = "Despesa",
                        valueCents = selectedProjection?.projectedExpenseCents ?: uiState.chartDays.lastOrNull()?.cumulativeExpense ?: 0L,
                        color = RedExpense,
                        modifier = Modifier.weight(1f),
                    )
                    val result = selectedProjection?.resultCents
                        ?: ((uiState.chartDays.lastOrNull()?.cumulativeIncome ?: 0L) - (uiState.chartDays.lastOrNull()?.cumulativeExpense ?: 0L))
                    SummaryChip(
                        label = "Resultado",
                        valueCents = result,
                        color = if (result >= 0) GreenIncome else RedExpense,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // Line chart
            item {
                AppCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.lg, vertical = Spacing.xs),
                ) {
                    if (uiState.chartDays.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                "Nenhuma projeção para este mês",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else {
                        FinanceLineChart(
                            days = uiState.chartDays,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .padding(vertical = Spacing.sm),
                        )
                        ChartLegend()
                    }
                }
            }

            // Saldo atual
            item {
                AppCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.lg, vertical = Spacing.xs),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Saldo atual", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            CurrencyFormatter.formatCents(uiState.currentBalanceCents),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.currentBalanceCents >= 0) GreenIncome else RedExpense,
                        )
                    }
                }
            }

            // Section header for 12-month list
            item { SectionHeader("IMPACTO — PRÓXIMOS 12 MESES") }

            // Projection rows
            items(uiState.projections, key = { it.yearMonth.toString() }) { projection ->
                MonthProjectionRow(
                    projection = projection,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.lg, vertical = 2.dp),
                )
            }
        }
    }
}

@Composable
private fun MonthNavigationBar(
    yearMonth: YearMonth,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Mês anterior")
        }
        Text(
            text = DateFormatter.formatMonthYear(yearMonth)
                .replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.titleMedium,
        )
        IconButton(onClick = onNext) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Próximo mês")
        }
    }
}

@Composable
private fun SummaryChip(
    label: String,
    valueCents: Long,
    color: Color,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            CurrencyFormatter.formatCents(valueCents),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = color,
        )
    }
}

@Composable
private fun FinanceLineChart(
    days: List<DailyPoint>,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        if (days.isEmpty()) return@Canvas

        val chartH = size.height * 0.78f
        val barH = size.height * 0.18f
        val barTop = size.height - barH

        val maxVal = maxOf(
            days.maxOf { it.cumulativeIncome },
            days.maxOf { it.cumulativeExpense },
        ).coerceAtLeast(1L)
        val totalDays = days.size.toFloat()

        fun xOf(day: Int) = (day - 0.5f) / totalDays * size.width
        fun yOf(cents: Long) = chartH - (cents.toFloat() / maxVal * chartH)

        // Filled area under income line (green, translucent)
        val incomePath = Path().apply {
            moveTo(xOf(days.first().day), chartH)
            days.forEach { lineTo(xOf(it.day), yOf(it.cumulativeIncome)) }
            lineTo(xOf(days.last().day), chartH)
            close()
        }
        drawPath(incomePath, GreenIncome.copy(alpha = 0.12f), style = Fill)

        // Filled area under expense line (red, translucent)
        val expensePath = Path().apply {
            moveTo(xOf(days.first().day), chartH)
            days.forEach { lineTo(xOf(it.day), yOf(it.cumulativeExpense)) }
            lineTo(xOf(days.last().day), chartH)
            close()
        }
        drawPath(expensePath, RedExpense.copy(alpha = 0.10f), style = Fill)

        // Income line (green)
        val incomeStroke = Path().apply {
            days.forEachIndexed { i, pt ->
                if (i == 0) moveTo(xOf(pt.day), yOf(pt.cumulativeIncome))
                else lineTo(xOf(pt.day), yOf(pt.cumulativeIncome))
            }
        }
        drawPath(incomeStroke, GreenIncome, style = Stroke(width = 2.dp.toPx()))

        // Expense line (red)
        val expenseStroke = Path().apply {
            days.forEachIndexed { i, pt ->
                if (i == 0) moveTo(xOf(pt.day), yOf(pt.cumulativeExpense))
                else lineTo(xOf(pt.day), yOf(pt.cumulativeExpense))
            }
        }
        drawPath(expenseStroke, RedExpense, style = Stroke(width = 2.dp.toPx()))

        // Balance bars at bottom
        val maxAbsBalance = days.maxOf { kotlin.math.abs(it.balance) }.coerceAtLeast(1L)
        val dayWidth = size.width / totalDays
        days.forEach { pt ->
            val fillH = (kotlin.math.abs(pt.balance).toFloat() / maxAbsBalance * barH).coerceAtLeast(1f)
            val barColor = if (pt.balance >= 0) GreenIncome.copy(alpha = 0.75f) else RedExpense.copy(alpha = 0.75f)
            drawRect(
                color = barColor,
                topLeft = Offset(x = (pt.day - 1f) / totalDays * size.width, y = barTop + (barH - fillH)),
                size = Size(dayWidth * 0.85f, fillH),
            )
        }

        // Axis line separating chart from bars
        drawLine(
            color = Color.Gray.copy(alpha = 0.3f),
            start = Offset(0f, chartH),
            end = Offset(size.width, chartH),
            strokeWidth = 0.5.dp.toPx(),
        )
    }
}

@Composable
private fun ChartLegend() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(Spacing.lg, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LegendItem(color = GreenIncome, label = "Receita")
        LegendItem(color = RedExpense, label = "Despesa")
        LegendItem(color = GreenIncome.copy(alpha = 0.75f), label = "Saldo +")
        LegendItem(color = RedExpense.copy(alpha = 0.75f), label = "Saldo −")
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun MonthProjectionRow(
    projection: MonthProjection,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    DateFormatter.formatMonthYear(projection.yearMonth).replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                if (projection.goalStatus == GoalStatus.SIMULATED) {
                    Text(
                        "⚠ Simulado com última meta",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else if (projection.goalStatus == GoalStatus.NONE) {
                    Text(
                        "⚠ Sem metas definidas",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    Text(
                        CurrencyFormatter.formatCents(projection.projectedIncomeCents),
                        style = MaterialTheme.typography.bodySmall,
                        color = GreenIncome,
                    )
                    Text(
                        CurrencyFormatter.formatCents(projection.projectedExpenseCents),
                        style = MaterialTheme.typography.bodySmall,
                        color = RedExpense,
                    )
                }
                val resultColor = if (projection.resultCents >= 0) GreenIncome else RedExpense
                val resultPrefix = if (projection.resultCents >= 0) "+" else ""
                Text(
                    "$resultPrefix${CurrencyFormatter.formatCents(projection.resultCents)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = resultColor,
                )
                Text(
                    "Acum.: ${CurrencyFormatter.formatCents(projection.cumulativeBalanceCents)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (projection.cumulativeBalanceCents >= 0) GreenIncome else RedExpense,
                )
            }
        }
    }
}
