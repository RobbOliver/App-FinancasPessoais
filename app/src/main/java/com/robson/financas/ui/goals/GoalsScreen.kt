package com.robson.financas.ui.goals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Flag
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.robson.financas.data.local.relation.GoalProgress
import com.robson.financas.ui.common.GoalProgressBar
import com.robson.financas.ui.designsystem.AppCard
import com.robson.financas.ui.designsystem.AppFab
import com.robson.financas.ui.designsystem.AppPrimaryButton
import com.robson.financas.ui.designsystem.EmptyState
import com.robson.financas.ui.designsystem.FabClearance
import com.robson.financas.ui.theme.GreenIncome
import com.robson.financas.ui.theme.RedExpense
import com.robson.financas.ui.theme.Spacing
import com.robson.financas.util.CurrencyFormatter
import com.robson.financas.util.DateFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(onOpenGoal: (Long) -> Unit, viewModel: GoalsViewModel = hiltViewModel()) {
    val yearMonth by viewModel.yearMonth.collectAsState()
    val rows by viewModel.rows.collectAsState()
    val expenseCategories by viewModel.expenseCategories.collectAsState()
    val hasAnyGoalThisMonth by viewModel.hasAnyGoalThisMonth.collectAsState()
    val previousMonthHasGoals by viewModel.previousMonthHasGoals.collectAsState()
    val editingGoal by viewModel.editingGoal.collectAsState()

    val plannedCents = rows.sumOf { it.amountCents }
    val spentCents = rows.sumOf { it.spentCents }
    val balanceCents = plannedCents - spentCents

    Scaffold(
        topBar = { TopAppBar(expandedHeight = 40.dp, title = { Text("Metas") }) },
        floatingActionButton = {
            AppFab(onClick = viewModel::openNewGoal, contentDescription = "Nova meta", icon = Icons.Filled.Add)
        },
    ) { innerPadding ->
        if (rows.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(Spacing.lg),
                contentAlignment = Alignment.Center,
            ) {
                EmptyState(
                    icon = Icons.Filled.Flag,
                    title = "Nenhuma meta criada ainda",
                    subtitle = "Toque em + e escolha quais categorias fazem parte de cada meta.",
                    actionLabel = "Criar meta",
                    onAction = viewModel::openNewGoal,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(innerPadding),
                contentPadding = PaddingValues(
                    start = Spacing.lg,
                    top = Spacing.lg,
                    end = Spacing.lg,
                    bottom = Spacing.lg + FabClearance,
                ),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
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
                        AppPrimaryButton(
                            text = "Importar metas de ${DateFormatter.formatMonthYear(yearMonth.minusMonths(1))}",
                            onClick = viewModel::importFromPreviousMonth,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
                items(rows, key = { it.goalId }) { row ->
                    GoalRowItem(row = row, onClick = { onOpenGoal(row.goalId) })
                }
            }
        }
    }

    if (editingGoal != null) {
        SetGoalDialog(
            goal = editingGoal!!,
            categories = expenseCategories,
            onDismiss = viewModel::closeEditingGoal,
            onNameChange = viewModel::updateEditingName,
            onAmountChange = viewModel::updateEditingAmount,
            onToggleCategory = viewModel::toggleEditingCategory,
            onCategoryAllocationChange = viewModel::updateEditingCategoryAllocation,
            onSave = viewModel::saveEditingGoal,
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
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
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
private fun GoalRowItem(row: GoalProgress, onClick: () -> Unit) {
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Spacing.xs, bottom = Spacing.xs),
        onClick = onClick,
    ) {
        Text(row.name, style = MaterialTheme.typography.bodyLarge)
        Text(
            row.categoryNames.orEmpty(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        GoalProgressBar(
            goalCents = row.amountCents,
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
                "${CurrencyFormatter.formatCents(row.spentCents)} de ${CurrencyFormatter.formatCents(row.amountCents)}",
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
    }
}
