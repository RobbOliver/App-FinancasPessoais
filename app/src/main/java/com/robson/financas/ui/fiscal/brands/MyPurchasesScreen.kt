package com.robson.financas.ui.fiscal.brands

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.robson.financas.data.local.entity.fiscal.ProductEntity
import com.robson.financas.data.local.relation.fiscal.PurchaseItemWithDetails
import com.robson.financas.ui.designsystem.AppCard
import com.robson.financas.ui.designsystem.CardLabel
import com.robson.financas.ui.designsystem.EmptyState
import com.robson.financas.ui.designsystem.SurfaceLevel
import com.robson.financas.ui.designsystem.appTextFieldColors
import com.robson.financas.ui.theme.Spacing
import com.robson.financas.util.CurrencyFormatter

private enum class MyPurchasesTab { DASHBOARD, ITENS, MARCAS }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPurchasesScreen(
    onBack: () -> Unit,
    onOpenProduct: (Long) -> Unit,
    onOpenReview: () -> Unit,
    viewModel: MyPurchasesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val filteredItems by viewModel.filteredItems.collectAsState()
    var selectedTab by remember { mutableStateOf(MyPurchasesTab.DASHBOARD) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Minhas compras") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                expandedHeight = 48.dp,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            TabRow(selectedTabIndex = selectedTab.ordinal) {
                Tab(
                    selected = selectedTab == MyPurchasesTab.DASHBOARD,
                    onClick = { selectedTab = MyPurchasesTab.DASHBOARD },
                    text = { Text("Resumo") },
                )
                Tab(
                    selected = selectedTab == MyPurchasesTab.ITENS,
                    onClick = { selectedTab = MyPurchasesTab.ITENS },
                    text = { Text("Itens") },
                )
                Tab(
                    selected = selectedTab == MyPurchasesTab.MARCAS,
                    onClick = { selectedTab = MyPurchasesTab.MARCAS },
                    text = { Text("Marcas") },
                )
            }

            when (selectedTab) {
                MyPurchasesTab.DASHBOARD -> DashboardTab(
                    stats = uiState.dashboardStats,
                    onOpenReview = onOpenReview,
                )
                MyPurchasesTab.ITENS -> ItensTab(
                    items = filteredItems,
                    filter = uiState.itemFilter,
                    onFilterChange = viewModel::updateItemFilter,
                    onOpenProduct = onOpenProduct,
                )
                MyPurchasesTab.MARCAS -> MarcasTab(
                    brandGroups = uiState.brandGroups,
                    onOpenProduct = onOpenProduct,
                    onEditBrand = viewModel::openEditBrand,
                )
            }
        }
    }

    uiState.editBrandForProduct?.let { product ->
        EditBrandDialog(
            product = product,
            onDismiss = viewModel::dismissEditBrand,
            onSave = { brand -> viewModel.saveBrand(product, brand) },
        )
    }
}

@Composable
private fun DashboardTab(
    stats: MyPurchasesDashboardStats,
    onOpenReview: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "ITENS CADASTRADOS",
                    value = "${stats.totalItems}",
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "NOTAS IMPORTADAS",
                    value = "${stats.totalDocuments}",
                )
            }
        }
        item {
            AppCard(modifier = Modifier.fillMaxWidth(), level = SurfaceLevel.Elevated) {
                CardLabel("Total gasto em compras")
                Text(
                    CurrencyFormatter.formatCents(stats.totalSpentCents),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(top = Spacing.xs),
                )
            }
        }
        item {
            AppCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = onOpenReview,
                level = SurfaceLevel.Elevated,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Receipt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = Spacing.md),
                        )
                        Column {
                            Text("Revisão de notas fiscais", style = MaterialTheme.typography.bodyLarge)
                            if (stats.pendingReviewCount > 0) {
                                Text(
                                    "${stats.pendingReviewCount} item(ns) aguardando revisão",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            } else {
                                Text(
                                    "Tudo revisado",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    AppCard(modifier = modifier, level = SurfaceLevel.Elevated) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = Spacing.xs))
    }
}

@Composable
private fun ItensTab(
    items: List<PurchaseItemWithDetails>,
    filter: String,
    onFilterChange: (String) -> Unit,
    onOpenProduct: (Long) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = filter,
            onValueChange = onFilterChange,
            label = { Text("Buscar item") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            colors = appTextFieldColors(),
            shape = MaterialTheme.shapes.medium,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg, vertical = Spacing.md),
        )
        if (items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(Spacing.lg), contentAlignment = Alignment.Center) {
                EmptyState(
                    icon = Icons.Filled.ShoppingCart,
                    title = if (filter.isEmpty()) "Nenhum item ainda" else "Nenhum resultado",
                    subtitle = if (filter.isEmpty()) "Importe notas fiscais para ver seus itens aqui." else "Tente outro termo.",
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = Spacing.lg, end = Spacing.lg, bottom = Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                items(items, key = { it.item.id }) { item ->
                    val displayName = item.productGenericName ?: item.item.normalizedDescription
                    AppCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = item.item.productId?.let { pid -> { onOpenProduct(pid) } },
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(displayName, style = MaterialTheme.typography.bodyLarge)
                                if (item.productBrand != null) {
                                    Text(
                                        item.productBrand,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                if (item.establishmentName != null) {
                                    Text(
                                        item.establishmentName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                            Text(
                                CurrencyFormatter.formatCents(item.item.unitPriceCents),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MarcasTab(
    brandGroups: List<BrandGroup>,
    onOpenProduct: (Long) -> Unit,
    onEditBrand: (ProductEntity) -> Unit,
) {
    var expandedBrand by remember { mutableStateOf<String?>(null) }

    if (brandGroups.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().padding(Spacing.lg),
            contentAlignment = Alignment.Center,
        ) {
            EmptyState(
                icon = Icons.Filled.Sell,
                title = "Nenhuma marca ainda",
                subtitle = "Marcas aparecem aqui conforme suas notas fiscais são importadas.",
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            items(brandGroups, key = { it.brand }) { group ->
                val expanded = expandedBrand == group.brand
                val isNoBrand = group.brand == "Sem marca"
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedBrand = if (expanded) null else group.brand },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(group.brand, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "${group.products.size} produto${if (group.products.size == 1) "" else "s"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            modifier = Modifier.rotate(if (expanded) 90f else 0f),
                        )
                    }
                    if (expanded) {
                        Column(modifier = Modifier.padding(top = Spacing.sm)) {
                            group.products.forEach { product ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onOpenProduct(product.id) }
                                        .padding(vertical = Spacing.xs),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        product.genericName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f),
                                    )
                                    if (isNoBrand) {
                                        IconButton(
                                            onClick = { onEditBrand(product) },
                                            modifier = Modifier.size(32.dp),
                                        ) {
                                            Icon(
                                                Icons.Filled.Edit,
                                                contentDescription = "Atribuir marca",
                                                modifier = Modifier.size(16.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EditBrandDialog(
    product: ProductEntity,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var brandText by remember { mutableStateOf(product.brand ?: "") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Atribuir marca") },
        text = {
            Column {
                Text(
                    product.genericName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = Spacing.md),
                )
                OutlinedTextField(
                    value = brandText,
                    onValueChange = { brandText = it },
                    label = { Text("Marca") },
                    singleLine = true,
                    colors = appTextFieldColors(),
                    shape = MaterialTheme.shapes.medium,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(brandText) }) { Text("Salvar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    )
}
