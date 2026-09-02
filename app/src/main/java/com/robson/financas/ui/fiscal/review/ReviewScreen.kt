package com.robson.financas.ui.fiscal.review

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.robson.financas.data.local.relation.fiscal.PurchaseItemWithDetails
import com.robson.financas.ui.designsystem.AppCard
import com.robson.financas.ui.designsystem.AppOutlinedButton
import com.robson.financas.ui.designsystem.AppPrimaryButton
import com.robson.financas.ui.designsystem.EmptyState
import com.robson.financas.ui.fiscal.common.ClassificationStatusPill
import com.robson.financas.ui.theme.Spacing
import com.robson.financas.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    onBack: () -> Unit,
    viewModel: ReviewViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Revisão (${uiState.items.size})") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )
        },
    ) { innerPadding ->
        if (uiState.items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(Spacing.lg),
                contentAlignment = Alignment.Center,
            ) {
                EmptyState(
                    icon = Icons.Filled.CheckCircle,
                    title = "Tudo revisado",
                    subtitle = "Nenhum item precisa da sua atenção agora.",
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                items(uiState.items, key = { it.item.id }) { item ->
                    ReviewItemCard(
                        item = item,
                        onConfirm = { viewModel.confirm(item.item.id) },
                        onCorrect = { viewModel.openPicker(item.item.id) },
                        onIgnore = { viewModel.ignore(item.item.id) },
                        onEditProduct = { viewModel.openProductDialog(item.item.id) },
                    )
                }
            }
        }
    }

    if (uiState.pickerForItemId != null) {
        MicrocategoryPickerSheet(
            options = uiState.classificationOptions,
            onDismiss = viewModel::dismissPicker,
            onPick = { option, scope ->
                viewModel.correct(uiState.pickerForItemId!!, option, scope)
            },
        )
    }

    val productDialogItem = uiState.items.find { it.item.id == uiState.productDialogForItemId }
    if (productDialogItem != null) {
        ProductIdentityDialog(
            brand = productDialogItem.productBrand,
            genericName = productDialogItem.productGenericName ?: productDialogItem.item.originalDescription,
            onDismiss = viewModel::dismissProductDialog,
            onSave = { brand, genericName -> viewModel.updateProductIdentity(productDialogItem.item.id, brand, genericName) },
        )
    }
}

@Composable
private fun ReviewItemCard(
    item: PurchaseItemWithDetails,
    onConfirm: () -> Unit,
    onCorrect: () -> Unit,
    onIgnore: () -> Unit,
    onEditProduct: () -> Unit,
) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.item.originalDescription, style = MaterialTheme.typography.bodyLarge)
                Text(
                    listOfNotNull(item.categoryName, item.subcategoryName, item.microcategoryName)
                        .joinToString(" › ")
                        .ifBlank { "Sem sugestão de departamento" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(CurrencyFormatter.formatCents(item.item.totalPriceCents), style = MaterialTheme.typography.bodyLarge)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Spacing.xs),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Marca: ${item.productBrand ?: "—"} · Produto: ${item.productGenericName ?: "—"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onEditProduct) {
                Text("Editar", style = MaterialTheme.typography.labelSmall)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Spacing.xs),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            ClassificationStatusPill(item.item.classificationStatus)
        }

        item.item.classificationReason?.let { reason ->
            Text(
                reason,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Spacing.xs),
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            if (item.item.categoryId != null) {
                AppOutlinedButton(text = "Confirmar", onClick = onConfirm, modifier = Modifier.weight(1f))
            }
            AppPrimaryButton(text = "Corrigir", onClick = onCorrect, modifier = Modifier.weight(1f))
        }
        AppOutlinedButton(
            text = "Ignorar",
            onClick = onIgnore,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Spacing.xs),
        )
    }
}
