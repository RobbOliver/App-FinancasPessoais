package com.robson.financas.ui.dashboard

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.robson.financas.ui.common.DonutChart
import com.robson.financas.ui.common.GoalProgressBar
import com.robson.financas.ui.common.MonthlyBarChart
import com.robson.financas.ui.common.TransactionListItem
import com.robson.financas.ui.designsystem.AppCard
import com.robson.financas.ui.designsystem.AppFab
import com.robson.financas.ui.designsystem.EmptyState
import com.robson.financas.ui.designsystem.HeroCard
import com.robson.financas.ui.designsystem.SurfaceLevel
import com.robson.financas.ui.designsystem.dotGridOverlay
import com.robson.financas.ui.theme.GreenIncome
import com.robson.financas.ui.theme.RedExpense
import com.robson.financas.ui.theme.Spacing
import com.robson.financas.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onAddTransaction: () -> Unit,
    onEditTransaction: (Long) -> Unit,
    onOpenCreditCards: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Resumo") }) },
        floatingActionButton = {
            AppFab(onClick = onAddTransaction, contentDescription = "Nova transação", icon = Icons.Filled.Add)
        },
    ) { innerPadding ->
        if (uiState.accounts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(Spacing.lg),
                contentAlignment = Alignment.Center,
            ) {
                EmptyState(
                    icon = Icons.Filled.Add,
                    title = "Cadastre uma conta para começar",
                    subtitle = "Suas contas concentram o saldo de todas as transações.",
                )
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg),
        ) {
            item { TotalBalanceCard(totalCents = uiState.totalBalanceCents) }
            if (uiState.hasPending) {
                item {
                    PendingSummaryCard(
                        pendingIncomeCents = uiState.pendingIncomeCents,
                        pendingExpenseCents = uiState.pendingExpenseCents,
                    )
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                ) {
                    SummaryCard(
                        modifier = Modifier.weight(1f),
                        label = "Receitas do mês",
                        amountCents = uiState.monthSummary.incomeCents,
                        color = GreenIncome,
                        icon = Icons.Filled.ArrowUpward,
                    )
                    SummaryCard(
                        modifier = Modifier.weight(1f),
                        label = "Despesas do mês",
                        amountCents = uiState.monthSummary.expenseCents,
                        color = RedExpense,
                        icon = Icons.Filled.ArrowDownward,
                    )
                }
            }
            item {
                if (uiState.creditCards.isEmpty()) {
                    EmptyState(
                        icon = Icons.Filled.CreditCard,
                        title = "Nenhum cartão de crédito cadastrado",
                        subtitle = "Ops! Você ainda não tem nenhum cartão de crédito cadastrado.",
                        actionLabel = "Adicionar novo cartão",
                        onAction = onOpenCreditCards,
                    )
                } else {
                    AppCard(modifier = Modifier.fillMaxWidth(), onClick = onOpenCreditCards) {
                        Text("Cartões de crédito", style = MaterialTheme.typography.titleMedium)
                        uiState.creditCards.forEach { summary ->
                            Column(modifier = Modifier.padding(top = Spacing.md)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(summary.card.name, style = MaterialTheme.typography.bodyLarge)
                                    Text(CurrencyFormatter.formatCents(summary.invoiceTotalCents))
                                }
                                GoalProgressBar(
                                    goalCents = summary.card.limitCents,
                                    spentCents = summary.invoiceTotalCents,
                                    modifier = Modifier.padding(top = Spacing.sm),
                                )
                            }
                        }
                    }
                }
            }
            item {
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Despesas por categoria", style = MaterialTheme.typography.titleMedium)
                    if (uiState.expenseByCategory.isEmpty()) {
                        Text(
                            "Sem despesas este mês.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = Spacing.sm),
                        )
                    } else {
                        DonutChart(
                            slices = uiState.expenseByCategory,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = Spacing.md),
                        )
                    }
                }
            }
            item {
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Receita x despesa", style = MaterialTheme.typography.titleMedium)
                    MonthlyBarChart(
                        data = uiState.monthlyHistory,
                        modifier = Modifier.padding(top = Spacing.md),
                    )
                }
            }
            item {
                Text(
                    "Últimas transações",
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            if (uiState.recentTransactions.isEmpty()) {
                item {
                    Text(
                        "Nenhuma transação lançada ainda.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                items(uiState.recentTransactions, key = { it.transaction.id }) { item ->
                    AppCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(0.dp)) {
                        TransactionListItem(
                            item = item,
                            onClick = { onEditTransaction(item.transaction.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PendingSummaryCard(pendingIncomeCents: Long, pendingExpenseCents: Long) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        level = SurfaceLevel.Elevated,
    ) {
        Text("Pendência e alertas", style = MaterialTheme.typography.titleSmall)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Spacing.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            if (pendingIncomeCents > 0) {
                Text(
                    "A receber: ${CurrencyFormatter.formatCents(pendingIncomeCents)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GreenIncome,
                )
            }
            if (pendingExpenseCents > 0) {
                Text(
                    "A pagar: ${CurrencyFormatter.formatCents(pendingExpenseCents)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = RedExpense,
                )
            }
        }
    }
}

@Composable
private fun TotalBalanceCard(totalCents: Long) {
    HeroCard(modifier = Modifier.fillMaxWidth().dotGridOverlay()) {
        Text(
            "Saldo total",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            CurrencyFormatter.formatCents(totalCents),
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier.padding(top = Spacing.xs),
        )
    }
}

@Composable
private fun SummaryCard(
    label: String,
    amountCents: Long,
    color: androidx.compose.ui.graphics.Color,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier, level = SurfaceLevel.Elevated) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.padding(end = Spacing.xs))
            Text(label, style = MaterialTheme.typography.bodySmall)
        }
        Text(
            CurrencyFormatter.formatCents(amountCents),
            style = MaterialTheme.typography.titleMedium,
            color = color,
            modifier = Modifier.padding(top = Spacing.xs),
        )
    }
}
