package com.robson.financas.ui.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.robson.financas.ui.common.ColorCatalog
import com.robson.financas.ui.common.GoalProgressBar
import com.robson.financas.ui.common.IconCatalog
import com.robson.financas.ui.theme.GreenIncome
import com.robson.financas.ui.theme.RedExpense
import com.robson.financas.util.CurrencyFormatter
import com.robson.financas.util.DateFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(viewModel: GoalsViewModel = hiltViewModel()) {
    val yearMonth by viewModel.yearMonth.collectAsState()
    val rows by viewModel.rows.collectAsState()
    val hasAnyGoalThisMonth by viewModel.hasAnyGoalThisMonth.collectAsState()
    val previousMonthHasGoals by viewModel.previousMonthHasGoals.collectAsState()
    var editingCategoryId by remember { mutableStateOf<Long?>(null) }

    val parents = rows.filter { it.parentCategoryId == null }
    val childrenByParent = rows.filter { it.parentCategoryId != null }.groupBy { it.parentCategoryId }

    val plannedCents = rows.filter { it.hasGoal }.sumOf { it.amountCents ?: 0L }
    val spentCents = rows.filter { it.hasGoal }.sumOf { it.spentCents }
    val balanceCents = plannedCents - spentCents

    Scaffold(
        topBar = { TopAppBar(title = { Text("Metas") }) },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                MonthNavigator(
                    label = DateFormatter.formatMonthYear(yearMonth),
                    onPrevious = viewModel::prevMonth,
                    onNext = viewModel::nextMonth,
                )
            }
            item {
                GoalsSummaryCard(plannedCents = plannedCents, spentCents = spentCents, balanceCents = balanceCents)
            }
            if (!hasAnyGoalThisMonth && previousMonthHasGoals) {
                item {
                    Button(
                        onClick = viewModel::importFromPreviousMonth,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Importar metas de ${DateFormatter.formatMonthYear(yearMonth.minusMonths(1))}")
                    }
                }
            }
            items(parents, key = { it.categoryId }) { parent ->
                Column {
                    GoalRowItem(row = parent, onClick = { editingCategoryId = parent.categoryId })
                    childrenByParent[parent.categoryId].orEmpty().forEach { child ->
                        GoalRowItem(
                            row = child,
                            indented = true,
                            onClick = { editingCategoryId = child.categoryId },
                        )
                    }
                }
            }
        }
    }

    val editingRow = rows.find { it.categoryId == editingCategoryId }
    if (editingRow != null) {
        SetGoalDialog(
            categoryName = editingRow.categoryName,
            initialAmountCents = editingRow.amountCents ?: 0L,
            hasExistingGoal = editingRow.hasGoal,
            onDismiss = { editingCategoryId = null },
            onSave = { amount ->
                viewModel.setGoal(editingRow.categoryId, amount)
                editingCategoryId = null
            },
            onRemove = {
                viewModel.removeGoal(editingRow.categoryId)
                editingCategoryId = null
            },
        )
    }
}

@Composable
private fun MonthNavigator(label: String, onPrevious: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Mês anterior")
        }
        Text(label, style = MaterialTheme.typography.titleMedium)
        IconButton(onClick = onNext) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Próximo mês")
        }
    }
}

@Composable
private fun GoalsSummaryCard(plannedCents: Long, spentCents: Long, balanceCents: Long) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            SummaryStat(label = "Planejado", value = CurrencyFormatter.formatCents(plannedCents))
            SummaryStat(label = "Gasto", value = CurrencyFormatter.formatCents(spentCents))
            SummaryStat(
                label = if (balanceCents >= 0) "Economizado" else "Estourado",
                value = CurrencyFormatter.formatCents(kotlin.math.abs(balanceCents)),
                color = if (balanceCents >= 0) GreenIncome else RedExpense,
            )
        }
    }
}

@Composable
private fun SummaryStat(label: String, value: String, color: Color = Color.Unspecified) {
    Column {
        Text(label, style = MaterialTheme.typography.bodySmall)
        Text(value, style = MaterialTheme.typography.titleMedium, color = color)
    }
}

@Composable
private fun GoalRowItem(row: GoalRow, indented: Boolean = false, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = if (indented) 24.dp else 0.dp, top = 4.dp, bottom = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            if (row.hasGoal) {
                                ColorCatalog.toColor(row.categoryColorHex)
                            } else {
                                MaterialTheme.colorScheme.outline
                            },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = IconCatalog.resolve(row.categoryIcon),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp),
                    )
                }
                Text(
                    row.categoryName,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 12.dp),
                )
            }
            if (row.hasGoal) {
                GoalProgressBar(
                    goalCents = row.amountCents ?: 0L,
                    spentCents = row.spentCents,
                    modifier = Modifier.padding(top = 10.dp),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        "${CurrencyFormatter.formatCents(row.spentCents)} de ${CurrencyFormatter.formatCents(row.amountCents ?: 0L)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (row.remainingCents < 0) {
                        Text(
                            "${CurrencyFormatter.formatCents(-row.remainingCents)} acima da meta",
                            style = MaterialTheme.typography.bodySmall,
                            color = RedExpense,
                        )
                    } else {
                        Text(
                            "${CurrencyFormatter.formatCents(row.remainingCents)} restante",
                            style = MaterialTheme.typography.bodySmall,
                            color = GreenIncome,
                        )
                    }
                }
            } else {
                Text(
                    "Sem meta definida",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}
