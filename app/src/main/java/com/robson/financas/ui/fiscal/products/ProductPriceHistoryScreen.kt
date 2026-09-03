package com.robson.financas.ui.fiscal.products

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
import com.robson.financas.data.local.relation.fiscal.EstablishmentPricePoint
import com.robson.financas.ui.designsystem.AppCard
import com.robson.financas.ui.fiscal.common.PriceTrendSparkline
import com.robson.financas.ui.theme.GreenIncome
import com.robson.financas.ui.theme.Spacing
import com.robson.financas.util.CurrencyFormatter
import com.robson.financas.util.DateFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductPriceHistoryScreen(
    onBack: () -> Unit,
    viewModel: ProductPriceHistoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                expandedHeight = 40.dp,
                title = { Text(uiState.product?.normalizedName ?: "Histórico de preço") },
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
                    val summary = uiState.summary
                    if (summary == null) {
                        Text("Sem histórico de preço ainda.", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            StatColumn("Menor", CurrencyFormatter.formatCents(summary.minNormalizedCents))
                            StatColumn("Médio", CurrencyFormatter.formatCents(summary.avgNormalizedCents))
                            StatColumn("Maior", CurrencyFormatter.formatCents(summary.maxNormalizedCents))
                        }
                        Text(
                            "${summary.purchaseCount} compra(s) registrada(s)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = Spacing.sm),
                        )
                        PriceTrendSparkline(
                            pricesCents = uiState.history.reversed().map { it.normalizedPriceCents },
                            modifier = Modifier.padding(top = Spacing.md),
                        )
                    }
                }
            }

            if (uiState.comparison.isNotEmpty()) {
                item {
                    Text(
                        "Comparação entre estabelecimentos",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = Spacing.sm),
                    )
                }
                items(uiState.comparison, key = { "${it.establishmentId}-${it.purchasedAt}" }) { point ->
                    ComparisonRow(point, isCheapest = point == uiState.comparison.first())
                }
            }

            item {
                Text(
                    "Histórico de compras",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = Spacing.sm),
                )
            }
            items(uiState.history, key = { it.id }) { entry ->
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(DateFormatter.formatShort(entry.purchasedAt), style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "${CurrencyFormatter.formatCents(entry.normalizedPriceCents)}/${entry.normalizedUnit}",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatColumn(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun ComparisonRow(point: EstablishmentPricePoint, isCheapest: Boolean) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(point.establishmentName ?: "Estabelecimento desconhecido", style = MaterialTheme.typography.bodyMedium)
            Text(
                CurrencyFormatter.formatCents(point.normalizedPriceCents),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isCheapest) GreenIncome else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
