package com.robson.financas.ui.dashboard

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.robson.financas.data.local.relation.AccountWithBalance
import com.robson.financas.ui.common.DonutChart
import com.robson.financas.ui.common.MonthlyBarChart
import com.robson.financas.ui.common.TransactionListItem
import com.robson.financas.ui.common.label
import com.robson.financas.ui.designsystem.AppCard
import com.robson.financas.ui.designsystem.AppFab
import com.robson.financas.ui.designsystem.CardLabel
import com.robson.financas.ui.designsystem.EmptyState
import com.robson.financas.ui.designsystem.FabClearance
import com.robson.financas.ui.designsystem.HeroCard
import com.robson.financas.ui.designsystem.SurfaceLevel
import com.robson.financas.ui.designsystem.scanlineOverlay
import com.robson.financas.ui.theme.DataTextStyle
import com.robson.financas.ui.theme.EyebrowStyle
import com.robson.financas.ui.theme.GreenIncome
import com.robson.financas.ui.theme.RedExpense
import com.robson.financas.ui.theme.Spacing
import com.robson.financas.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onAddTransaction: () -> Unit,
    onEditTransaction: (Long) -> Unit,
    onOpenAccounts: () -> Unit,
    onScanQrCode: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var showActionSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Resumo") }) },
        floatingActionButton = {
            AppFab(onClick = { showActionSheet = true }, contentDescription = "Nova transação", icon = Icons.Filled.Add)
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
            contentPadding = PaddingValues(
                start = Spacing.lg,
                top = Spacing.lg,
                end = Spacing.lg,
                bottom = Spacing.lg + FabClearance,
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg),
        ) {
            item {
                TotalBalanceCard(
                    totalCents = uiState.totalBalanceCents,
                    hideBalances = uiState.hideBalances,
                    onToggleHideBalances = viewModel::toggleHideBalances,
                )
            }
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
                AppCard(modifier = Modifier.fillMaxWidth(), onClick = onOpenAccounts) {
                    CardLabel("Contas")
                    if (uiState.dashboardAccounts.isEmpty()) {
                        Text(
                            "Nenhuma conta selecionada pra aparecer aqui — ajuste em Configurações.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = Spacing.sm),
                        )
                    } else {
                        uiState.dashboardAccounts.forEach { item ->
                            AccountBalanceRow(item = item, hideBalances = uiState.hideBalances, modifier = Modifier.padding(top = Spacing.md))
                        }
                    }
                }
            }
            item {
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    CardLabel("Despesas por categoria")
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
                    CardLabel("Receita x despesa")
                    MonthlyBarChart(
                        data = uiState.monthlyHistory,
                        modifier = Modifier.padding(top = Spacing.md),
                    )
                }
            }
            item {
                CardLabel("Últimas transações", modifier = Modifier.padding(horizontal = Spacing.xs))
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
                    AppCard(
                        modifier = Modifier.fillMaxWidth().animateItem(),
                        contentPadding = PaddingValues(0.dp),
                    ) {
                        TransactionListItem(
                            item = item,
                            onClick = { onEditTransaction(item.transaction.id) },
                        )
                    }
                }
            }
        }
    }

    if (showActionSheet) {
        NewEntrySheet(
            onDismiss = { showActionSheet = false },
            onManualEntry = {
                showActionSheet = false
                onAddTransaction()
            },
            onScanQrCode = {
                showActionSheet = false
                onScanQrCode()
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewEntrySheet(onDismiss: () -> Unit, onManualEntry: () -> Unit, onScanQrCode: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm)) {
            NewEntryOption(icon = Icons.Filled.Add, label = "Lançamento manual", onClick = onManualEntry)
            NewEntryOption(icon = Icons.Filled.QrCodeScanner, label = "Escanear nota fiscal", onClick = onScanQrCode)
        }
    }
}

@Composable
private fun NewEntryOption(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = Spacing.md),
        )
    }
}

@Composable
private fun PendingSummaryCard(pendingIncomeCents: Long, pendingExpenseCents: Long) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        level = SurfaceLevel.Elevated,
    ) {
        CardLabel("Pendência e alertas")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Spacing.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            if (pendingIncomeCents > 0) {
                Text(
                    "A receber: ${CurrencyFormatter.formatCents(pendingIncomeCents)}",
                    style = MaterialTheme.typography.bodyMedium.merge(DataTextStyle),
                    color = GreenIncome,
                )
            }
            if (pendingExpenseCents > 0) {
                Text(
                    "A pagar: ${CurrencyFormatter.formatCents(pendingExpenseCents)}",
                    style = MaterialTheme.typography.bodyMedium.merge(DataTextStyle),
                    color = RedExpense,
                )
            }
        }
    }
}

private const val HIDDEN_BALANCE_PLACEHOLDER = "R$ ••••••"

@Composable
private fun TotalBalanceCard(totalCents: Long, hideBalances: Boolean, onToggleHideBalances: () -> Unit) {
    HeroCard(
        modifier = Modifier
            .fillMaxWidth()
            .scanlineOverlay(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CardLabel("Saldo total")
            IconButton(onClick = onToggleHideBalances) {
                Icon(
                    if (hideBalances) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = if (hideBalances) "Mostrar saldos" else "Ocultar saldos",
                )
            }
        }
        Text(
            if (hideBalances) HIDDEN_BALANCE_PLACEHOLDER else CurrencyFormatter.formatCents(totalCents),
            style = MaterialTheme.typography.displaySmall.merge(DataTextStyle),
            modifier = Modifier.padding(top = Spacing.xs),
        )
    }
}

@Composable
private fun AccountBalanceRow(item: AccountWithBalance, hideBalances: Boolean, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(item.account.name, style = MaterialTheme.typography.bodyLarge)
            Text(
                item.account.type.label(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            if (hideBalances) HIDDEN_BALANCE_PLACEHOLDER else CurrencyFormatter.formatCents(item.balanceCents),
            style = MaterialTheme.typography.bodyLarge.merge(DataTextStyle),
            color = if (item.balanceCents >= 0) GreenIncome else RedExpense,
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
            Text(label.uppercase(), style = MaterialTheme.typography.labelSmall.merge(EyebrowStyle))
        }
        Text(
            CurrencyFormatter.formatCents(amountCents),
            style = MaterialTheme.typography.titleMedium.merge(DataTextStyle),
            color = color,
            modifier = Modifier.padding(top = Spacing.xs),
        )
    }
}
