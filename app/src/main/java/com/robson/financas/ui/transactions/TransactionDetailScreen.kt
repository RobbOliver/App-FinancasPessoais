package com.robson.financas.ui.transactions

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
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.robson.financas.data.local.entity.TransactionType
import com.robson.financas.data.local.relation.TransactionWithDetails
import com.robson.financas.data.local.relation.fiscal.PurchaseItemWithDetails
import com.robson.financas.ui.common.ConfirmDeleteDialog
import com.robson.financas.ui.common.label
import com.robson.financas.ui.designsystem.AppCard
import com.robson.financas.ui.designsystem.AppOutlinedButton
import com.robson.financas.ui.fiscal.common.ClassificationStatusPill
import com.robson.financas.ui.theme.Spacing
import com.robson.financas.util.CurrencyFormatter
import com.robson.financas.util.DateFormatter

private enum class DetailTab { GERAL, ITENS }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onOpenFiscalDocument: (Long) -> Unit,
    viewModel: TransactionDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(DetailTab.GERAL) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.deleted) {
        if (uiState.deleted) onBack()
    }

    val details = uiState.transaction

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(details?.transaction?.description?.ifBlank { details.categoryName ?: "Transação" } ?: "Transação") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    if (details != null) {
                        IconButton(onClick = { onEdit(details.transaction.id) }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Editar")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Excluir")
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        if (details != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                if (uiState.fiscalDocumentId != null) {
                    TabRow(selectedTabIndex = selectedTab.ordinal) {
                        Tab(selected = selectedTab == DetailTab.GERAL, onClick = { selectedTab = DetailTab.GERAL }, text = { Text("Geral") })
                        Tab(selected = selectedTab == DetailTab.ITENS, onClick = { selectedTab = DetailTab.ITENS }, text = { Text("Itens") })
                    }
                }

                when {
                    selectedTab == DetailTab.ITENS && uiState.fiscalDocumentId != null ->
                        ItemsTab(items = uiState.fiscalItems)
                    else ->
                        GeneralTab(
                            details = details,
                            fiscalDocumentId = uiState.fiscalDocumentId,
                            onOpenFiscalDocument = onOpenFiscalDocument,
                        )
                }
            }
        }
    }

    if (showDeleteDialog) {
        ConfirmDeleteDialog(
            title = "Excluir transação",
            message = "Tem certeza que deseja excluir esta transação?",
            onConfirm = {
                viewModel.deleteTransaction()
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false },
        )
    }
}

@Composable
private fun GeneralTab(
    details: TransactionWithDetails,
    fiscalDocumentId: Long?,
    onOpenFiscalDocument: (Long) -> Unit,
) {
    val transaction = details.transaction
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        item {
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Valor",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(CurrencyFormatter.formatCents(transaction.amountCents), style = MaterialTheme.typography.titleLarge)
                Text(
                    transaction.type.label(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Spacing.xs),
                )
            }
        }
        item { DetailRow(label = "Descrição", value = transaction.description.ifBlank { "—" }) }
        item { DetailRow(label = "Data", value = DateFormatter.formatShort(transaction.date)) }
        item {
            DetailRow(
                label = "Conta",
                value = if (transaction.type == TransactionType.TRANSFER) {
                    "${details.accountName} → ${details.transferToAccountName}"
                } else {
                    details.accountName
                },
            )
        }
        if (details.categoryName != null) {
            item { DetailRow(label = "Categoria", value = details.categoryName) }
        }
        item {
            DetailRow(
                label = "Status",
                value = listOfNotNull(
                    if (transaction.isPaid) "Paga/recebida" else "Pendente",
                    if (transaction.isIgnored) "Ignorada" else null,
                    if (transaction.isFavorite) "Favorita" else null,
                ).joinToString(" · "),
            )
        }
        if (fiscalDocumentId != null) {
            item {
                AppOutlinedButton(
                    text = "Ver nota fiscal completa",
                    icon = { Icon(Icons.Filled.ReceiptLong, contentDescription = null) },
                    onClick = { onOpenFiscalDocument(fiscalDocumentId) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Spacing.sm),
                )
            }
        }
    }
}

@Composable
private fun ItemsTab(items: List<PurchaseItemWithDetails>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        items(items, key = { it.item.id }) { item ->
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.item.originalDescription, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            listOfNotNull(item.categoryName, item.subcategoryName, item.microcategoryName).joinToString(" › ")
                                .ifBlank { "Sem departamento" },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (item.productBrand != null || item.productGenericName != null) {
                            Text(
                                "Marca: ${item.productBrand ?: "—"} · Produto: ${item.productGenericName ?: "—"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        ClassificationStatusPill(item.item.classificationStatus, modifier = Modifier.padding(top = Spacing.xs))
                    }
                    Text(CurrencyFormatter.formatCents(item.item.totalPriceCents), style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = Spacing.xs))
    }
}
