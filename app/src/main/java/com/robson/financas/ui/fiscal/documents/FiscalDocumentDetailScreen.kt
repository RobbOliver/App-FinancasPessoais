package com.robson.financas.ui.fiscal.documents

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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.robson.financas.data.local.relation.fiscal.PurchaseItemWithDetails
import com.robson.financas.ui.designsystem.AppCard
import com.robson.financas.ui.fiscal.common.ClassificationStatusPill
import com.robson.financas.ui.theme.RedExpense
import com.robson.financas.ui.theme.Spacing
import com.robson.financas.util.CurrencyFormatter
import com.robson.financas.util.DateFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiscalDocumentDetailScreen(
    onBack: () -> Unit,
    viewModel: FiscalDocumentDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val document = uiState.document

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(document?.let { DateFormatter.formatShort(it.issuedAt) } ?: "Nota fiscal") },
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
            item {
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Total da nota", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                document?.let { CurrencyFormatter.formatCents(it.totalCents) } ?: "—",
                                style = MaterialTheme.typography.titleLarge,
                            )
                        }
                    }
                    if (document?.needsAttention == true) {
                        Text(
                            "O total dos itens não bate exatamente com o total da nota — confira antes de confirmar.",
                            style = MaterialTheme.typography.bodySmall,
                            color = RedExpense,
                            modifier = Modifier.padding(top = Spacing.sm),
                        )
                    }
                }
            }
            item {
                Text(
                    "Itens (${uiState.items.size})",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = Spacing.sm),
                )
            }
            items(uiState.items, key = { it.item.id }) { item ->
                ItemRow(item)
            }
        }
    }
}

@Composable
private fun ItemRow(item: PurchaseItemWithDetails) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.item.originalDescription, style = MaterialTheme.typography.bodyLarge)
                Text(
                    listOfNotNull(item.categoryName, item.subcategoryName, item.microcategoryName).joinToString(" › ")
                        .ifBlank { "Sem categoria" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                ClassificationStatusPill(item.item.classificationStatus, modifier = Modifier.padding(top = Spacing.xs))
            }
            Text(CurrencyFormatter.formatCents(item.item.totalPriceCents), style = MaterialTheme.typography.bodyLarge)
        }
    }
}
