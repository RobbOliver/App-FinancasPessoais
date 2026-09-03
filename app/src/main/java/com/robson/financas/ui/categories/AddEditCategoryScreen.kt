package com.robson.financas.ui.categories

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.robson.financas.data.local.entity.CategoryType
import com.robson.financas.ui.common.ColorPicker
import com.robson.financas.ui.common.IconPicker
import com.robson.financas.ui.common.label
import com.robson.financas.ui.designsystem.AppPrimaryButton
import com.robson.financas.ui.designsystem.AppTextField
import com.robson.financas.ui.designsystem.appTextFieldColors
import com.robson.financas.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCategoryScreen(
    onBack: () -> Unit,
    viewModel: AddEditCategoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val allCategories by viewModel.allCategories.collectAsState()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onBack()
    }

    val parentOptions = remember(allCategories, uiState.type, uiState.existingCategory) {
        allCategories.filter {
            it.type == uiState.type &&
                it.parentCategoryId == null &&
                it.id != uiState.existingCategory?.id
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                expandedHeight = 40.dp,
                title = { Text(if (uiState.isEditing) "Editar categoria" else "Nova categoria") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(Spacing.lg)
                .verticalScroll(rememberScrollState()),
        ) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                CategoryType.entries.forEachIndexed { index, type ->
                    SegmentedButton(
                        selected = uiState.type == type,
                        onClick = { viewModel.updateType(type) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = CategoryType.entries.size),
                    ) {
                        Text(type.label())
                    }
                }
            }

            AppTextField(
                value = uiState.name,
                onValueChange = viewModel::updateName,
                label = "Nome da categoria",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.lg),
            )

            ParentCategoryDropdown(
                options = parentOptions,
                selectedId = uiState.parentCategoryId,
                onSelected = viewModel::updateParent,
                modifier = Modifier.padding(top = Spacing.lg),
            )

            Text(
                "Cor",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = Spacing.xl, bottom = Spacing.sm),
            )
            ColorPicker(selectedHex = uiState.colorHex, onColorSelected = viewModel::updateColor)

            Text(
                "Ícone",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = Spacing.xl, bottom = Spacing.sm),
            )
            IconPicker(selectedKey = uiState.icon, onIconSelected = viewModel::updateIcon)

            AppPrimaryButton(
                text = "Salvar",
                onClick = viewModel::save,
                enabled = uiState.name.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.xl),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ParentCategoryDropdown(
    options: List<com.robson.financas.data.local.entity.CategoryEntity>,
    selectedId: Long?,
    onSelected: (Long?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.find { it.id == selectedId }?.name ?: "Nenhuma (categoria principal)"

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text("Categoria pai (opcional)") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = appTextFieldColors(),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("Nenhuma (categoria principal)") },
                onClick = {
                    onSelected(null)
                    expanded = false
                },
            )
            options.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.name) },
                    onClick = {
                        onSelected(category.id)
                        expanded = false
                    },
                )
            }
        }
    }
}
