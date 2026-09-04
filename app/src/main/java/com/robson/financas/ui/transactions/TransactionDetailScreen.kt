package com.robson.financas.ui.transactions

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import com.robson.financas.ui.theme.GreenIncome
import com.robson.financas.ui.theme.BlueAdvance
import com.robson.financas.ui.common.CurrencyInputField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
import java.time.LocalDate

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
    var showPaymentDateDialog by remember { mutableStateOf(false) }
    var showAdvanceDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.deleted) {
        if (uiState.deleted) onBack()
    }

    val details = uiState.transaction

    Scaffold(
        bottomBar = {
            if (details != null && !details.transaction.isPaid) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.lg),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    Button(
                        onClick = { showPaymentDateDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = GreenIncome),
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium,
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.padding(end = Spacing.sm))
                        Text("Marcar como pago", style = MaterialTheme.typography.labelLarge)
                    }
                    if (details.transaction.type == TransactionType.INCOME) {
                        Button(
                            onClick = { showAdvanceDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = BlueAdvance),
                            modifier = Modifier.weight(1f),
                            shape = MaterialTheme.shapes.medium,
                        ) {
                            Icon(Icons.Filled.Bolt, contentDescription = null, modifier = Modifier.padding(end = Spacing.sm))
                            Text("Adiantamento", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        },
        topBar = {
            TopAppBar(
                expandedHeight = 40.dp,
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

    if (showPaymentDateDialog && details != null) {
        PaymentDateDialog(
            initialAmountCents = details.transaction.amountCents,
            onConfirm = { date, amount ->
                viewModel.markAsPaid(date, amount)
                showPaymentDateDialog = false
            },
            onDismiss = { showPaymentDateDialog = false },
        )
    }

    if (showAdvanceDialog && details != null) {
        AdvanceDialog(
            maxAmountCents = details.transaction.amountCents,
            onConfirm = { amount, date ->
                viewModel.advancePayment(amount, date)
                showAdvanceDialog = false
            },
            onDismiss = { showAdvanceDialog = false },
        )
    }
}

@Composable
private fun PaymentDateDialog(
    initialAmountCents: Long,
    onConfirm: (LocalDate, Long) -> Unit,
    onDismiss: () -> Unit,
) {
    val today = LocalDate.now()
    var selectedDate by remember { mutableStateOf(today) }
    var amountCents by remember { mutableStateOf(initialAmountCents) }

    val shortcuts = listOf(
        "Hoje" to today,
        "Ontem" to today.minusDays(1),
        "há 2 dias" to today.minusDays(2),
        "há 15 dias" to today.minusDays(15),
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Quando foi pago?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                CurrencyInputField(
                    amountCents = amountCents,
                    onAmountChange = { amountCents = it },
                    label = "Valor pago",
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = DateFormatter.formatShort(selectedDate),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    shortcuts.forEach { (label, date) ->
                        FilterChip(
                            selected = selectedDate == date,
                            onClick = { selectedDate = date },
                            label = { Text(label) },
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedDate, amountCents) },
                colors = ButtonDefaults.buttonColors(containerColor = GreenIncome),
                enabled = amountCents > 0,
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    )
}

@Composable
private fun AdvanceDialog(
    maxAmountCents: Long,
    onConfirm: (Long, LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val today = LocalDate.now()
    var selectedDate by remember { mutableStateOf(today) }
    var amountCents by remember { mutableStateOf(0L) }

    val shortcuts = listOf(
        "Hoje" to today,
        "Ontem" to today.minusDays(1),
        "há 2 dias" to today.minusDays(2),
        "há 15 dias" to today.minusDays(15),
    )

    val isValid = amountCents in 1..maxAmountCents

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adiantamento de receita") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                Text(
                    "Máximo: ${CurrencyFormatter.formatCents(maxAmountCents)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                CurrencyInputField(
                    amountCents = amountCents,
                    onAmountChange = { amountCents = it.coerceAtMost(maxAmountCents) },
                    label = "Quanto foi adiantado?",
                    isError = amountCents > maxAmountCents,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = DateFormatter.formatShort(selectedDate),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    shortcuts.forEach { (label, date) ->
                        FilterChip(
                            selected = selectedDate == date,
                            onClick = { selectedDate = date },
                            label = { Text(label) },
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(amountCents, selectedDate) },
                colors = ButtonDefaults.buttonColors(containerColor = BlueAdvance),
                enabled = isValid,
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    )
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
