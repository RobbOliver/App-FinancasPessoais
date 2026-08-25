package com.robson.financas.ui.creditcards

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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.robson.financas.ui.designsystem.AppCard
import com.robson.financas.ui.designsystem.AppPrimaryButton
import com.robson.financas.ui.theme.GreenIncome
import com.robson.financas.ui.theme.Spacing
import com.robson.financas.util.CurrencyFormatter
import com.robson.financas.util.DateFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditCardDetailScreen(
    onBack: () -> Unit,
    onAddPurchase: () -> Unit,
    viewModel: CreditCardDetailViewModel = hiltViewModel(),
) {
    val card by viewModel.card.collectAsState()
    val yearMonth by viewModel.yearMonth.collectAsState()
    val purchases by viewModel.purchases.collectAsState()
    val invoiceTotalCents by viewModel.invoiceTotalCents.collectAsState()
    val isPaid by viewModel.isPaid.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(card?.name ?: "Cartão") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddPurchase) {
                Icon(Icons.Filled.Add, contentDescription = "Nova compra")
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = viewModel::prevMonth) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Fatura anterior")
                    }
                    Text(DateFormatter.formatMonthYear(yearMonth), style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = viewModel::nextMonth) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Próxima fatura")
                    }
                }
            }
            item {
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Total da fatura",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(CurrencyFormatter.formatCents(invoiceTotalCents), style = MaterialTheme.typography.headlineSmall)
                    if (isPaid) {
                        Text(
                            "Fatura paga",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GreenIncome,
                            modifier = Modifier.padding(top = Spacing.sm),
                        )
                    } else {
                        AppPrimaryButton(
                            text = "Pagar fatura",
                            onClick = viewModel::payInvoice,
                            enabled = invoiceTotalCents > 0,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = Spacing.md),
                        )
                    }
                }
            }
            item {
                Text("Compras", style = MaterialTheme.typography.titleMedium)
            }
            if (purchases.isEmpty()) {
                item {
                    Text(
                        "Nenhuma compra nesta fatura.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                items(purchases, key = { it.purchase.id }) { item ->
                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(
                                    item.purchase.description.ifBlank { item.categoryName ?: "Compra" },
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                                val subtitle = buildString {
                                    append(DateFormatter.formatShort(item.purchase.purchaseDate))
                                    if (item.purchase.installmentTotal > 1) {
                                        append(" · ${item.purchase.installmentNumber}/${item.purchase.installmentTotal}")
                                    }
                                    item.categoryName?.let { append(" · $it") }
                                }
                                Text(
                                    subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Text(CurrencyFormatter.formatCents(item.purchase.amountCents))
                        }
                    }
                }
            }
        }
    }
}
