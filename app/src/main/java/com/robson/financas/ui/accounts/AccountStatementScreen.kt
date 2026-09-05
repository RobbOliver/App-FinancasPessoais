package com.robson.financas.ui.accounts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.robson.financas.ui.common.CurrencyInputField
import com.robson.financas.ui.common.TransactionListItem
import com.robson.financas.ui.designsystem.AppCard
import com.robson.financas.ui.theme.GreenIncome
import com.robson.financas.ui.theme.RedExpense
import com.robson.financas.ui.theme.Spacing
import com.robson.financas.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountStatementScreen(
    onBack: () -> Unit,
    onOpenTransaction: (Long) -> Unit,
    viewModel: AccountStatementViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAdjustDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                expandedHeight = 40.dp,
                title = { Text(uiState.accountName.ifBlank { "Extrato" }) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = { showAdjustDialog = true }) {
                        Icon(Icons.Filled.Tune, contentDescription = "Ajustar saldo")
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
            item {
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Saldo atual",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        CurrencyFormatter.formatCents(uiState.currentBalanceCents),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.currentBalanceCents >= 0) GreenIncome else RedExpense,
                    )
                }
            }
            items(uiState.rows, key = { it.item.transaction.id }) { row ->
                Column {
                    AppCard(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(0.dp),
                    ) {
                        TransactionListItem(
                            item = row.item,
                            onClick = { onOpenTransaction(row.item.transaction.id) },
                        )
                    }
                    Text(
                        text = if (row.runningBalanceCents != null) {
                            "Saldo após: ${CurrencyFormatter.formatCents(row.runningBalanceCents)}"
                        } else {
                            "Pendente — não afeta o saldo"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (row.runningBalanceCents != null) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.tertiary
                        },
                        modifier = Modifier.padding(start = Spacing.lg, top = 2.dp, bottom = Spacing.xs),
                    )
                }
            }
        }
    }

    if (showAdjustDialog) {
        AdjustBalanceDialog(
            currentBalanceCents = uiState.currentBalanceCents,
            onConfirm = { target ->
                viewModel.adjustBalance(target)
                showAdjustDialog = false
            },
            onDismiss = { showAdjustDialog = false },
        )
    }
}

@Composable
private fun AdjustBalanceDialog(
    currentBalanceCents: Long,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    var targetCents by remember { mutableStateOf(0L) }
    val diff = targetCents - currentBalanceCents

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajustar saldo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                Text(
                    "Saldo atual no app: ${CurrencyFormatter.formatCents(currentBalanceCents)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                CurrencyInputField(
                    amountCents = targetCents,
                    onAmountChange = { targetCents = it },
                    label = "Saldo correto (conforme o banco)",
                    modifier = Modifier.fillMaxWidth(),
                )
                if (targetCents > 0 && diff != 0L) {
                    Text(
                        text = if (diff > 0) {
                            "Cria um lançamento de ajuste de entrada: +${CurrencyFormatter.formatCents(diff)}"
                        } else {
                            "Cria um lançamento de ajuste de saída: -${CurrencyFormatter.formatCents(-diff)}"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (diff > 0) GreenIncome else RedExpense,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(targetCents) },
                enabled = targetCents > 0 && diff != 0L,
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    )
}
