package com.robson.financas.ui.fiscal.scanning

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.robson.financas.ui.common.CurrencyInputField
import com.robson.financas.ui.designsystem.AppCard
import com.robson.financas.ui.designsystem.AppOutlinedButton
import com.robson.financas.ui.designsystem.AppPrimaryButton
import com.robson.financas.ui.designsystem.AppTextField
import com.robson.financas.ui.designsystem.CardLabel
import com.robson.financas.ui.theme.DataTextStyle
import com.robson.financas.ui.theme.RedExpense
import com.robson.financas.ui.theme.Spacing
import com.robson.financas.util.CurrencyFormatter
import com.robson.financas.util.DateFormatter

/**
 * Mostra o progresso da extração por IA e, ao terminar, uma pré-visualização editável dos
 * itens antes de gravar — nunca salva direto (ver doc de [AiExtractionViewModel]).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiExtractionScreen(
    onBack: () -> Unit,
    onGoToXmlImport: () -> Unit,
    onSaved: (Long) -> Unit,
    viewModel: AiExtractionViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        (uiState as? AiExtractionUiState.Saved)?.let { onSaved(it.documentId) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                expandedHeight = 40.dp,
                title = { Text("Itens extraídos por IA") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )
        },
    ) { innerPadding ->
        when (val state = uiState) {
            is AiExtractionUiState.Loading -> LoadingBody(innerPadding)
            is AiExtractionUiState.Saved -> LoadingBody(innerPadding)
            is AiExtractionUiState.NoNetwork -> MessageBody(
                innerPadding = innerPadding,
                title = "Sem conexão com a internet",
                subtitle = "A extração por IA precisa de internet para buscar a nota. Conecte-se e tente de novo.",
                primaryLabel = "Tentar de novo",
                onPrimary = viewModel::retry,
                secondaryLabel = "Importar XML manualmente",
                onSecondary = onGoToXmlImport,
            )
            is AiExtractionUiState.ApiKeyMissing -> MessageBody(
                innerPadding = innerPadding,
                title = "Chave da OpenRouter não configurada",
                subtitle = "Cadastre sua chave de API em Configurações para usar a extração automática.",
                primaryLabel = "Importar XML manualmente",
                onPrimary = onGoToXmlImport,
            )
            is AiExtractionUiState.Failed -> MessageBody(
                innerPadding = innerPadding,
                title = "Não foi possível extrair os itens",
                subtitle = state.message,
                primaryLabel = "Tentar de novo",
                onPrimary = viewModel::retry,
                secondaryLabel = "Importar XML manualmente",
                onSecondary = onGoToXmlImport,
            )
            is AiExtractionUiState.Preview -> PreviewBody(
                innerPadding = innerPadding,
                state = state,
                onDescriptionChange = viewModel::updateItemDescription,
                onQuantityChange = viewModel::updateItemQuantity,
                onUnitPriceChange = viewModel::updateItemUnitPrice,
                onTotalPriceChange = viewModel::updateItemTotalPrice,
                onRemoveItem = viewModel::removeItem,
                onTotalChange = viewModel::updateTotal,
                onSave = viewModel::save,
            )
        }
    }
}

@Composable
private fun LoadingBody(innerPadding: PaddingValues) {
    Box(
        modifier = Modifier.fillMaxSize().padding(innerPadding),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Text(
                "Buscando a nota e lendo os itens com IA...",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = Spacing.lg),
            )
        }
    }
}

@Composable
private fun MessageBody(
    innerPadding: PaddingValues,
    title: String,
    subtitle: String,
    primaryLabel: String,
    onPrimary: () -> Unit,
    secondaryLabel: String? = null,
    onSecondary: (() -> Unit)? = null,
) {
    Box(
        modifier = Modifier.fillMaxSize().padding(innerPadding).padding(Spacing.lg),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Filled.Error, contentDescription = null, tint = RedExpense, modifier = Modifier.size(48.dp))
            Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = Spacing.lg))
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Spacing.sm),
            )
            AppPrimaryButton(
                text = primaryLabel,
                onClick = onPrimary,
                modifier = Modifier.fillMaxWidth().padding(top = Spacing.xl),
            )
            if (secondaryLabel != null && onSecondary != null) {
                AppOutlinedButton(
                    text = secondaryLabel,
                    onClick = onSecondary,
                    modifier = Modifier.fillMaxWidth().padding(top = Spacing.sm),
                )
            }
        }
    }
}

@Composable
private fun PreviewBody(
    innerPadding: PaddingValues,
    state: AiExtractionUiState.Preview,
    onDescriptionChange: (Int, String) -> Unit,
    onQuantityChange: (Int, String) -> Unit,
    onUnitPriceChange: (Int, Long) -> Unit,
    onTotalPriceChange: (Int, Long) -> Unit,
    onRemoveItem: (Int) -> Unit,
    onTotalChange: (Long) -> Unit,
    onSave: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(innerPadding),
        contentPadding = PaddingValues(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        item {
            Text(
                "Confira os itens extraídos por IA antes de salvar — corrija o que precisar.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item {
            AppCard(modifier = Modifier.fillMaxWidth()) {
                CardLabel("Estabelecimento")
                Text(
                    state.issuerName ?: "Não identificado",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = Spacing.xs),
                )
                Text(
                    DateFormatter.formatDayMonth(state.issuedAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        items(state.items, key = { it.id }) { item ->
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CardLabel("Item")
                    IconButton(onClick = { onRemoveItem(item.id) }) {
                        Icon(Icons.Filled.Close, contentDescription = "Remover item")
                    }
                }
                AppTextField(
                    value = item.description,
                    onValueChange = { onDescriptionChange(item.id, it) },
                    label = "Descrição",
                    modifier = Modifier.fillMaxWidth().padding(top = Spacing.sm),
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = Spacing.sm),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    AppTextField(
                        value = item.quantity,
                        onValueChange = { onQuantityChange(item.id, it) },
                        label = "Qtd (${item.unit})",
                        modifier = Modifier.weight(1f),
                    )
                    CurrencyInputField(
                        amountCents = item.unitPriceCents,
                        onAmountChange = { onUnitPriceChange(item.id, it) },
                        label = "Unitário",
                        modifier = Modifier.weight(1f),
                    )
                    CurrencyInputField(
                        amountCents = item.totalPriceCents,
                        onAmountChange = { onTotalPriceChange(item.id, it) },
                        label = "Total",
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        item {
            AppCard(modifier = Modifier.fillMaxWidth()) {
                CardLabel("Total da nota")
                CurrencyInputField(
                    amountCents = state.totalCents,
                    onAmountChange = onTotalChange,
                    label = "Total",
                    modifier = Modifier.fillMaxWidth().padding(top = Spacing.sm),
                )
                Text(
                    "Soma dos itens: ${CurrencyFormatter.formatCents(state.items.sumOf { it.totalPriceCents })}",
                    style = MaterialTheme.typography.bodySmall.merge(DataTextStyle),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Spacing.xs),
                )
            }
        }
        item {
            AppPrimaryButton(
                text = if (state.isSaving) "Salvando..." else "Salvar lançamento",
                onClick = onSave,
                enabled = !state.isSaving && state.items.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
