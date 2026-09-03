package com.robson.financas.ui.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.robson.financas.data.local.entity.CategoryEntity
import com.robson.financas.ui.common.ColorCatalog
import com.robson.financas.ui.common.ConfirmDeleteDialog
import com.robson.financas.ui.common.IconCatalog
import com.robson.financas.ui.common.label
import com.robson.financas.ui.designsystem.AppCard
import com.robson.financas.ui.designsystem.AppFab
import com.robson.financas.ui.designsystem.EmptyState
import com.robson.financas.ui.designsystem.FabClearance
import com.robson.financas.ui.theme.BorderSubtle
import com.robson.financas.ui.theme.Spacing
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    onBack: () -> Unit,
    onAddCategory: () -> Unit,
    onEditCategory: (Long) -> Unit,
    viewModel: CategoriesViewModel = hiltViewModel(),
) {
    val categories by viewModel.categories.collectAsState()
    val allCategories by viewModel.allCategories.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val deletionError by viewModel.deletionError.collectAsState()
    var pendingDelete by remember { mutableStateOf<CategoryEntity?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Agrupa por pai "de verdade" (via allCategories) em vez de só pelos itens já filtrados pela aba:
    // uma subcategoria criada pelo usuário pode ficar visível nesta aba mesmo que o pai dela tenha
    // migrado pra outra aba (ex.: virou categoria IA por coincidência de nome) — sem isso ela some da tela.
    val parentGroups = remember(categories, allCategories) {
        val byId = allCategories.associateBy { it.id }
        val childrenByParentId = categories.filter { it.parentCategoryId != null }.groupBy { it.parentCategoryId }
        val topLevelIds = categories.filter { it.parentCategoryId == null }.map { it.id }
        val parentIds = (topLevelIds + childrenByParentId.keys.filterNotNull()).distinct()
        parentIds.mapNotNull { byId[it] }
            .sortedBy { it.name }
            .map { parent -> parent to childrenByParentId[parent.id].orEmpty().sortedBy { it.name } }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                expandedHeight = 40.dp,
                title = { Text("Categorias") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (selectedTab != CategoryTab.IA) {
                AppFab(onClick = onAddCategory, contentDescription = "Nova categoria", icon = Icons.Filled.Add)
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            TabRow(selectedTabIndex = selectedTab.ordinal) {
                CategoryTab.entries.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { viewModel.selectTab(tab) },
                        text = { Text(tab.label()) },
                    )
                }
            }

            if (categories.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(Spacing.lg),
                    contentAlignment = Alignment.Center,
                ) {
                    EmptyState(
                        icon = Icons.Filled.Category,
                        title = "Nenhuma categoria nesta aba",
                        subtitle = if (selectedTab == CategoryTab.IA) {
                            "As categorias de classificação por IA aparecem aqui conforme suas notas fiscais são importadas."
                        } else {
                            "Toque em + para criar a primeira."
                        },
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = Spacing.lg,
                        end = Spacing.lg,
                        top = Spacing.sm,
                        bottom = Spacing.sm + FabClearance,
                    ),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    items(parentGroups, key = { it.first.id }) { (parent, children) ->
                        AppCard(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(0.dp),
                        ) {
                            CategoryRow(
                                category = parent,
                                isParent = true,
                                onClick = { onEditCategory(parent.id) },
                                onDeleteClick = if (parent.isAiTaxonomy) null else { { pendingDelete = parent } },
                            )
                            if (children.isNotEmpty()) {
                                HorizontalDivider(color = BorderSubtle, modifier = Modifier.padding(start = Spacing.lg))
                                children.forEachIndexed { index, child ->
                                    CategoryRow(
                                        category = child,
                                        isParent = false,
                                        onClick = { onEditCategory(child.id) },
                                        onDeleteClick = if (child.isAiTaxonomy) null else { { pendingDelete = child } },
                                    )
                                    if (index < children.lastIndex) {
                                        HorizontalDivider(color = BorderSubtle, modifier = Modifier.padding(start = 64.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    pendingDelete?.let { category ->
        ConfirmDeleteDialog(
            title = "Excluir categoria",
            message = "Tem certeza que deseja excluir \"${category.name}\"?",
            onConfirm = {
                viewModel.deleteCategory(category) { success ->
                    if (success) {
                        coroutineScope.launch { snackbarHostState.showSnackbar("Categoria excluída") }
                    }
                }
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null },
        )
    }

    deletionError?.let { message ->
        ConfirmDeleteDialog(
            title = "Não foi possível excluir",
            message = message,
            onConfirm = { viewModel.clearDeletionError() },
            onDismiss = { viewModel.clearDeletionError() },
        )
    }
}

@Composable
private fun CategoryRow(
    category: CategoryEntity,
    isParent: Boolean,
    onClick: () -> Unit,
    onDeleteClick: (() -> Unit)?,
) {
    val iconSize = if (isParent) 32.dp else 24.dp
    val iconInnerSize = if (isParent) 18.dp else 14.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                start = if (isParent) Spacing.lg else 64.dp,
                end = Spacing.lg,
                top = if (isParent) 12.dp else 8.dp,
                bottom = if (isParent) 12.dp else 8.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            if (!isParent) {
                // Guia visual em L ligando esta linha ao card do pai, pra deixar clara a relação hierárquica.
                Box(
                    modifier = Modifier
                        .padding(end = Spacing.sm)
                        .size(width = 16.dp, height = 1.dp)
                        .background(BorderSubtle),
                )
            }
            Box(
                modifier = Modifier
                    .size(iconSize)
                    .clip(CircleShape)
                    .background(ColorCatalog.toColor(category.colorHex)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = IconCatalog.resolve(category.icon),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(iconInnerSize),
                )
            }
            Text(
                category.name,
                style = if (isParent) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
                color = if (isParent) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = Spacing.md),
            )
        }
        if (onDeleteClick != null) {
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Filled.Delete, contentDescription = "Excluir")
            }
        }
    }
}
