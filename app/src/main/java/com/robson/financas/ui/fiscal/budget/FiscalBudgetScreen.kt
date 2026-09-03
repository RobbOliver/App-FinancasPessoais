package com.robson.financas.ui.fiscal.budget

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.robson.financas.domain.fiscal.budget.BudgetAlertLevel
import com.robson.financas.domain.fiscal.budget.BudgetStatus
import com.robson.financas.ui.common.GoalProgressBar
import com.robson.financas.ui.designsystem.AppCard
import com.robson.financas.ui.theme.RedExpense
import com.robson.financas.ui.theme.Spacing
import com.robson.financas.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiscalBudgetScreen(
    onBack: () -> Unit,
    viewModel: FiscalBudgetViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                expandedHeight = 40.dp,
                title = { Text("Orçamento por categoria") },
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
            contentPadding = PaddingValues(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            items(uiState.rows, key = { it.option.microcategoryId }) { row ->
                AppCard(modifier = Modifier.fillMaxWidth(), onClick = { viewModel.startEditing(row.option) }) {
                    Text(row.option.name, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "${row.option.categoryName} › ${row.option.subcategoryName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    val status = row.status
                    if (status == null) {
                        Text(
                            "Sem orçamento definido — toque para definir",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = Spacing.sm),
                        )
                    } else {
                        GoalProgressBar(
                            goalCents = status.limitCents,
                            spentCents = status.spentCents,
                            modifier = Modifier.padding(top = Spacing.sm),
                        )
                        Row(modifier = Modifier.fillMaxWidth().padding(top = Spacing.xs), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                "${CurrencyFormatter.formatCents(status.spentCents)} de ${CurrencyFormatter.formatCents(status.limitCents)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            AlertLabel(status)
                        }
                    }
                }
            }
        }
    }

    uiState.editingOption?.let { option ->
        val existing = uiState.rows.firstOrNull { it.option.microcategoryId == option.microcategoryId }?.status
        SetMicrocategoryBudgetDialog(
            microcategoryName = option.name,
            initialAmountCents = existing?.limitCents ?: 0L,
            onDismiss = viewModel::dismissEditing,
            onSave = viewModel::saveBudget,
        )
    }
}

@Composable
private fun AlertLabel(status: BudgetStatus) {
    val (text, isWarning) = when (status.alertLevel) {
        BudgetAlertLevel.EXCEEDED -> "Ultrapassado" to true
        BudgetAlertLevel.REACHED -> "Limite atingido" to true
        BudgetAlertLevel.PROJECTED_OVERSHOOT -> "Tendência de estouro" to true
        BudgetAlertLevel.EIGHTY -> "80% consumido" to true
        BudgetAlertLevel.HALF -> "50% consumido" to false
        BudgetAlertLevel.NONE -> null to false
    }
    if (text != null) {
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            color = if (isWarning) RedExpense else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
