package com.robson.financas.ui.tags

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Label
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.robson.financas.data.local.entity.TagEntity
import com.robson.financas.ui.common.ColorCatalog
import com.robson.financas.ui.common.ColorPicker
import com.robson.financas.ui.common.ConfirmDeleteDialog
import com.robson.financas.ui.designsystem.AppCard
import com.robson.financas.ui.designsystem.AppFab
import com.robson.financas.ui.designsystem.AppTextField
import com.robson.financas.ui.designsystem.EmptyState
import com.robson.financas.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagsScreen(
    onBack: () -> Unit,
    viewModel: TagsViewModel = hiltViewModel(),
) {
    val tags by viewModel.tags.collectAsState()
    var editingTag by remember { mutableStateOf<TagEntity?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<TagEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tags") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )
        },
        floatingActionButton = {
            AppFab(onClick = { showCreateDialog = true }, contentDescription = "Nova tag", icon = Icons.Filled.Add)
        },
    ) { innerPadding ->
        if (tags.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(Spacing.lg),
                contentAlignment = Alignment.Center,
            ) {
                EmptyState(
                    icon = Icons.Filled.Label,
                    title = "Nenhuma tag cadastrada",
                    subtitle = "Toque em + para criar a primeira.",
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                items(tags, key = { it.id }) { tag ->
                    AppCard(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(0.dp),
                        onClick = { editingTag = tag },
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Spacing.lg, vertical = Spacing.md),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(ColorCatalog.toColor(tag.colorHex)),
                                )
                                Text(tag.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = Spacing.md))
                            }
                            IconButton(onClick = { pendingDelete = tag }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Excluir")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        AddEditTagDialog(
            initialName = "",
            initialColorHex = ColorCatalog.hexValues.first(),
            onDismiss = { showCreateDialog = false },
            onSave = { name, colorHex ->
                viewModel.saveTag(null, name, colorHex)
                showCreateDialog = false
            },
        )
    }

    editingTag?.let { tag ->
        AddEditTagDialog(
            initialName = tag.name,
            initialColorHex = tag.colorHex,
            onDismiss = { editingTag = null },
            onSave = { name, colorHex ->
                viewModel.saveTag(tag.id, name, colorHex)
                editingTag = null
            },
        )
    }

    pendingDelete?.let { tag ->
        ConfirmDeleteDialog(
            title = "Excluir tag",
            message = "Tem certeza que deseja excluir \"${tag.name}\"?",
            onConfirm = {
                viewModel.deleteTag(tag)
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null },
        )
    }
}

@Composable
private fun AddEditTagDialog(
    initialName: String,
    initialColorHex: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }
    var colorHex by remember { mutableStateOf(initialColorHex) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialName.isBlank()) "Nova tag" else "Editar tag") },
        text = {
            Column {
                AppTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Nome",
                    modifier = Modifier.fillMaxWidth(),
                )
                ColorPicker(
                    selectedHex = colorHex,
                    onColorSelected = { colorHex = it },
                    modifier = Modifier.padding(top = Spacing.lg),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(name.trim(), colorHex) }, enabled = name.isNotBlank()) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    )
}
