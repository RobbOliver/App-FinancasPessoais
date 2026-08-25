package com.robson.financas.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.robson.financas.ui.common.TransactionListItem
import com.robson.financas.ui.theme.GreenIncome
import com.robson.financas.ui.theme.RedExpense
import com.robson.financas.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onAddTransaction: () -> Unit,
    onEditTransaction: (Long) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Resumo") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTransaction) {
                Icon(Icons.Filled.Add, contentDescription = "Nova transação")
            }
        },
    ) { innerPadding ->
        if (uiState.accounts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "Cadastre uma conta para começar.",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { TotalBalanceCard(totalCents = uiState.totalBalanceCents) }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
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
                    )
                }
            } else {
                items(uiState.recentTransactions, key = { it.transaction.id }) { item ->
                    Card {
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
private fun TotalBalanceCard(totalCents: Long) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Saldo total",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                CurrencyFormatter.formatCents(totalCents),
                style = MaterialTheme.typography.headlineMedium,
            )
        }
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
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.padding(end = 4.dp))
                Text(label, style = MaterialTheme.typography.bodySmall)
            }
            Text(
                CurrencyFormatter.formatCents(amountCents),
                style = MaterialTheme.typography.titleMedium,
                color = color,
            )
        }
    }
}
