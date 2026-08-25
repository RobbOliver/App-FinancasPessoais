package com.robson.financas.ui.transactions

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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.robson.financas.data.local.entity.TransactionEntity
import com.robson.financas.ui.common.ConfirmDeleteDialog
import com.robson.financas.ui.common.TransactionListItem
import com.robson.financas.util.DateFormatter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    onAddTransaction: () -> Unit,
    onEditTransaction: (Long) -> Unit,
    viewModel: TransactionsViewModel = hiltViewModel(),
) {
    val filter by viewModel.filter.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    var pendingDelete by remember { mutableStateOf<TransactionEntity?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val grouped = remember(transactions) { transactions.groupBy { it.transaction.date } }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Transações") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTransaction) {
                Icon(Icons.Filled.Add, contentDescription = "Nova transação")
            }
        },
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SimpleFilterDropdown(
                    label = "Conta",
                    selectedLabel = accounts.find { it.id == filter.accountId }?.name ?: "Todas",
                    options = listOf("Todas" to null) + accounts.map { it.name to it.id },
                    onSelected = viewModel::updateAccountFilter,
                    modifier = Modifier.weight(1f),
                )
                SimpleFilterDropdown(
                    label = "Categoria",
                    selectedLabel = categories.find { it.id == filter.categoryId }?.name ?: "Todas",
                    options = listOf("Todas" to null) + categories.map { it.name to it.id },
                    onSelected = viewModel::updateCategoryFilter,
                    modifier = Modifier.weight(1f),
                )
            }
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = filter.onlyCurrentMonth,
                    onClick = { viewModel.toggleCurrentMonth(!filter.onlyCurrentMonth) },
                    label = { Text("Este mês") },
                )
                FilterChip(
                    selected = filter.onlyNeedsReview,
                    onClick = { viewModel.toggleNeedsReview(!filter.onlyNeedsReview) },
                    label = { Text("Pendências") },
                )
            }

            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "Nenhuma transação encontrada.",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    grouped.forEach { (date, items) ->
                        item {
                            Text(
                                text = DateFormatter.formatDayMonth(date),
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp),
                            )
                        }
                        items(items, key = { it.transaction.id }) { item ->
                            TransactionListItem(
                                item = item,
                                onClick = { onEditTransaction(item.transaction.id) },
                                onDeleteClick = { pendingDelete = item.transaction },
                            )
                        }
                    }
                }
            }
        }
    }

    pendingDelete?.let { transaction ->
        ConfirmDeleteDialog(
            title = "Excluir transação",
            message = "Tem certeza que deseja excluir esta transação?",
            onConfirm = {
                viewModel.deleteTransaction(transaction)
                pendingDelete = null
                coroutineScope.launch { snackbarHostState.showSnackbar("Transação excluída") }
            },
            onDismiss = { pendingDelete = null },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SimpleFilterDropdown(
    label: String,
    selectedLabel: String,
    options: List<Pair<String, Long?>>,
    onSelected: (Long?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (optionLabel, id) ->
                DropdownMenuItem(
                    text = { Text(optionLabel) },
                    onClick = {
                        onSelected(id)
                        expanded = false
                    },
                )
            }
        }
    }
}
