package com.robson.financas.ui.categories

import androidx.compose.foundation.background
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.robson.financas.data.local.entity.CategoryEntity
import com.robson.financas.data.local.entity.CategoryType
import com.robson.financas.ui.common.ColorCatalog
import com.robson.financas.ui.common.ConfirmDeleteDialog
import com.robson.financas.ui.common.IconCatalog
import com.robson.financas.ui.common.label
import com.robson.financas.ui.designsystem.AppCard
import com.robson.financas.ui.designsystem.EmptyState
import com.robson.financas.ui.designsystem.SectionHeader
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
    val deletionError by viewModel.deletionError.collectAsState()
    var pendingDelete by remember { mutableStateOf<CategoryEntity?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val grouped = remember(categories) {
        CategoryType.entries.associateWith { type ->
            val ofType = categories.filter { it.type == type }
            val parents = ofType.filter { it.parentCategoryId == null }
            parents.map { parent -> parent to ofType.filter { it.parentCategoryId == parent.id } }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
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
            FloatingActionButton(onClick = onAddCategory) {
                Icon(Icons.Filled.Add, contentDescription = "Nova categoria")
            }
        },
    ) { innerPadding ->
        if (categories.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(Spacing.lg),
                contentAlignment = Alignment.Center,
            ) {
                EmptyState(
                    icon = Icons.Filled.Category,
                    title = "Nenhuma categoria cadastrada",
                    subtitle = "Toque em + para criar a primeira.",
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                CategoryType.entries.forEach { type ->
                    val parentGroups = grouped[type].orEmpty()
                    if (parentGroups.isNotEmpty()) {
                        item {
                            SectionHeader(type.label())
                        }
                        items(parentGroups, key = { it.first.id }) { (parent, children) ->
                            Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                                AppCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(0.dp),
                                    onClick = { onEditCategory(parent.id) },
                                ) {
                                    CategoryRow(
                                        category = parent,
                                        indented = false,
                                        onDeleteClick = { pendingDelete = parent },
                                    )
                                }
                                children.forEach { child ->
                                    AppCard(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentPadding = PaddingValues(0.dp),
                                        onClick = { onEditCategory(child.id) },
                                    ) {
                                        CategoryRow(
                                            category = child,
                                            indented = true,
                                            onDeleteClick = { pendingDelete = child },
                                        )
                                    }
                                }
                            }
                        }
                        item { HorizontalDivider(color = BorderSubtle, modifier = Modifier.padding(vertical = Spacing.sm)) }
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
    indented: Boolean,
    onDeleteClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = if (indented) 48.dp else Spacing.lg,
                end = Spacing.lg,
                top = 10.dp,
                bottom = 10.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(ColorCatalog.toColor(category.colorHex)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = IconCatalog.resolve(category.icon),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp),
                )
            }
            Text(category.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = Spacing.md))
        }
        IconButton(onClick = onDeleteClick) {
            Icon(Icons.Filled.Delete, contentDescription = "Excluir")
        }
    }
}
