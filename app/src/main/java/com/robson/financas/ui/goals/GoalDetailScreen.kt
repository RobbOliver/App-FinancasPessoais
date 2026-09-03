package com.robson.financas.ui.goals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.robson.financas.data.local.relation.GoalCategoryDetail
import com.robson.financas.ui.common.ConfirmDeleteDialog
import com.robson.financas.ui.common.GoalProgressBar
import com.robson.financas.ui.designsystem.AppCard
import com.robson.financas.ui.theme.GreenIncome
import com.robson.financas.ui.theme.RedExpense
import com.robson.financas.ui.theme.Spacing
import com.robson.financas.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDetailScreen(
    onBack: () -> Unit,
    viewModel: GoalDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val expenseCategories by viewModel.expenseCategories.collectAsState()
    val editingGoal by viewModel.editingGoal.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.deleted) {
        if (uiState.deleted) onBack()
    }

    val goal = uiState.goal
    val overBudget = goal != null && uiState.spentCents > goal.amountCents

    Scaffold(
        topBar = {
            TopAppBar(
                expandedHeight = 40.dp,
                title = { Text(goal?.name ?: "Meta") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    if (goal != null) {
                        IconButton(onClick = viewModel::openEditGoal) {
                            Icon(Icons.Filled.Edit, contentDescription = "Editar meta")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Excluir meta")
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        if (goal != null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                item {
                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(
                                    "Planejado",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(CurrencyFormatter.formatCents(goal.amountCents), style = MaterialTheme.typography.titleMedium)
                            }
                            Column {
                                Text(
                                    "Gasto",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    CurrencyFormatter.formatCents(uiState.spentCents),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (overBudget) RedExpense else MaterialTheme.colorScheme.onSurface,
                                )
                            }
                            Column {
                                Text(
                                    if (overBudget) "Estourado" else "Restante",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    CurrencyFormatter.formatCents(kotlin.math.abs(goal.amountCents - uiState.spentCents)),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (overBudget) RedExpense else GreenIncome,
                                )
                            }
                        }
                        GoalProgressBar(
                            goalCents = goal.amountCents,
                            spentCents = uiState.spentCents,
                            modifier = Modifier.padding(top = Spacing.md),
                        )
                    }
                }
                if (uiState.allocatedCents != goal.amountCents) {
                    item {
                        Text(
                            "Edite a meta e defina quanto cada categoria pode gastar do total.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = Spacing.xs),
                        )
                    }
                }
                item {
                    Text(
                        "Por categoria",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = Spacing.sm),
                    )
                }
                items(uiState.categoryDetails, key = { it.categoryId }) { detail ->
                    CategoryDetailRow(detail)
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

    if (showDeleteDialog) {
        ConfirmDeleteDialog(
            title = "Excluir meta",
            message = "Essa meta será apagada. Os lançamentos das categorias não são afetados.",
            onConfirm = {
                viewModel.deleteGoal()
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false },
        )
    }
}

@Composable
private fun CategoryDetailRow(detail: GoalCategoryDetail) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(detail.categoryName, style = MaterialTheme.typography.bodyLarge)
            Text(
                if (detail.allocatedCents > 0) {
                    "${CurrencyFormatter.formatCents(detail.spentCents)} de ${CurrencyFormatter.formatCents(detail.allocatedCents)}"
                } else {
                    CurrencyFormatter.formatCents(detail.spentCents)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = if (detail.isOverBudget) RedExpense else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (detail.allocatedCents > 0) {
            GoalProgressBar(
                goalCents = detail.allocatedCents,
                spentCents = detail.spentCents,
                overBudget = detail.isOverBudget,
                modifier = Modifier.padding(top = Spacing.sm),
            )
            if (detail.isOverBudget) {
                Text(
                    "${CurrencyFormatter.formatCents(-detail.remainingCents)} acima da fatia",
                    style = MaterialTheme.typography.bodySmall,
                    color = RedExpense,
                    modifier = Modifier.padding(top = Spacing.xs),
                )
            }
        }
    }
}
