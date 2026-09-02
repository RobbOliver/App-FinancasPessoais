package com.robson.financas.ui.transactions

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.robson.financas.data.local.entity.AccountEntity
import com.robson.financas.data.local.entity.CategoryEntity
import com.robson.financas.data.local.entity.TransactionType
import com.robson.financas.ui.common.ColorCatalog
import com.robson.financas.ui.common.CurrencyInputField
import com.robson.financas.ui.common.label
import com.robson.financas.ui.designsystem.AppOutlinedButton
import com.robson.financas.ui.designsystem.AppPrimaryButton
import com.robson.financas.ui.designsystem.AppTextField
import com.robson.financas.ui.designsystem.appTextFieldColors
import com.robson.financas.ui.theme.HudCyan
import com.robson.financas.ui.theme.Spacing
import com.robson.financas.util.AttachmentStorage
import com.robson.financas.util.DateFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionScreen(
    onBack: () -> Unit,
    viewModel: AddEditTransactionViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val allTags by viewModel.allTags.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch(Dispatchers.IO) {
                val path = AttachmentStorage.copyToInternalStorage(context, uri)
                if (path != null) {
                    withContext(Dispatchers.Main) { viewModel.updateAttachmentPath(path) }
                }
            }
        }
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onBack()
    }

    LaunchedEffect(accounts, uiState.accountId) {
        if (uiState.accountId == null && accounts.isNotEmpty()) {
            viewModel.updateAccount(accounts.first().id)
        }
    }

    val categoryOptions = remember(categories, uiState.type) {
        val targetType = viewModel.categoryTypeFor(uiState.type)
        if (targetType == null) emptyList() else categories.filter { it.type == targetType }
    }

    val transferAccountOptions = remember(accounts, uiState.accountId) {
        accounts.filter { it.id != uiState.accountId }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditing) "Editar transação" else "Nova transação") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::toggleFavorite) {
                        Icon(
                            imageVector = if (uiState.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favoritar",
                        )
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
                TransactionType.entries.forEachIndexed { index, type ->
                    SegmentedButton(
                        selected = uiState.type == type,
                        onClick = { viewModel.updateType(type) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = TransactionType.entries.size),
                    ) {
                        Text(type.label())
                    }
                }
            }

            CurrencyInputField(
                amountCents = uiState.amountCents,
                onAmountChange = viewModel::updateAmount,
                label = "Valor",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.lg),
            )

            AccountDropdown(
                label = if (uiState.type == TransactionType.TRANSFER) "Conta de origem" else "Conta",
                options = accounts,
                selectedId = uiState.accountId,
                onSelected = viewModel::updateAccount,
                modifier = Modifier.padding(top = Spacing.lg),
            )

            if (uiState.type == TransactionType.TRANSFER) {
                AccountDropdown(
                    label = "Conta de destino",
                    options = transferAccountOptions,
                    selectedId = uiState.transferToAccountId,
                    onSelected = viewModel::updateTransferToAccount,
                    modifier = Modifier.padding(top = Spacing.lg),
                )
            } else {
                CategoryDropdown(
                    options = categoryOptions,
                    selectedId = uiState.categoryId,
                    onSelected = viewModel::updateCategory,
                    modifier = Modifier.padding(top = Spacing.lg),
                )
            }

            DateField(
                date = uiState.date,
                onDateSelected = viewModel::updateDate,
                modifier = Modifier.padding(top = Spacing.lg),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.lg),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(if (uiState.isPaid) "Pago" else "Agendado (não pago)")
                Switch(checked = uiState.isPaid, onCheckedChange = viewModel::updateIsPaid)
            }

            if (uiState.type != TransactionType.TRANSFER) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Spacing.sm),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Ignorar despesa")
                    Switch(checked = uiState.isIgnored, onCheckedChange = viewModel::updateIsIgnored)
                }
            }

            AppTextField(
                value = uiState.description,
                onValueChange = viewModel::updateDescription,
                label = "Descrição (opcional)",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.lg),
            )

            if (allTags.isNotEmpty()) {
                Text(
                    "Tags",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = Spacing.lg),
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Spacing.sm),
                ) {
                    allTags.forEach { tag ->
                        FilterChip(
                            selected = tag.id in uiState.selectedTagIds,
                            onClick = { viewModel.toggleTag(tag.id) },
                            label = { Text(tag.name) },
                        )
                    }
                }
            }

            Text(
                "Anexo",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(top = Spacing.lg),
            )
            if (uiState.attachmentPath != null) {
                val bitmap = remember(uiState.attachmentPath) {
                    BitmapFactory.decodeFile(uiState.attachmentPath)?.asImageBitmap()
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    bitmap?.let {
                        Image(
                            bitmap = it,
                            contentDescription = "Comprovante anexado",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(8.dp)),
                        )
                    }
                    IconButton(onClick = { viewModel.updateAttachmentPath(null) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Remover anexo")
                    }
                }
            } else {
                AppOutlinedButton(
                    text = "Anexar comprovante",
                    onClick = { pickImageLauncher.launch("image/*") },
                    icon = { Icon(Icons.Filled.AttachFile, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.padding(top = Spacing.sm),
                )
            }

            AppPrimaryButton(
                text = "Salvar",
                onClick = viewModel::save,
                enabled = uiState.isValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.xl),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountDropdown(
    label: String,
    options: List<AccountEntity>,
    selectedId: Long?,
    onSelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.find { it.id == selectedId }?.name ?: "Selecione"

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = appTextFieldColors(),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { account ->
                DropdownMenuItem(
                    text = { Text(account.name) },
                    onClick = {
                        onSelected(account.id)
                        expanded = false
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(
    options: List<CategoryEntity>,
    selectedId: Long?,
    onSelected: (Long?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val byId = remember(options) { options.associateBy { it.id } }
    val selected = byId[selectedId]
    val selectedLabel = selected?.let { category ->
        val parentName = category.parentCategoryId?.let { byId[it]?.name }
        if (parentName != null) "$parentName › ${category.name}" else category.name
    } ?: "Selecione uma categoria"

    val groups = remember(options) {
        val childrenByParentId = options.filter { it.parentCategoryId != null }.groupBy { it.parentCategoryId }
        options.filter { it.parentCategoryId == null }
            .sortedBy { it.name }
            .map { parent -> parent to childrenByParentId[parent.id].orEmpty().sortedBy { it.name } }
    }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text("Categoria") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = appTextFieldColors(),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            groups.forEach { (parent, children) ->
                CategoryDropdownItem(
                    category = parent,
                    indented = false,
                    onClick = {
                        onSelected(parent.id)
                        expanded = false
                    },
                )
                children.forEach { child ->
                    CategoryDropdownItem(
                        category = child,
                        indented = true,
                        onClick = {
                            onSelected(child.id)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryDropdownItem(
    category: CategoryEntity,
    indented: Boolean,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(ColorCatalog.toColor(category.colorHex)),
                )
                Text(
                    category.name,
                    style = if (indented) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = Spacing.sm),
                )
                if (category.isAiTaxonomy) {
                    Box(
                        modifier = Modifier
                            .padding(start = Spacing.sm)
                            .clip(RoundedCornerShape(4.dp))
                            .background(HudCyan.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 1.dp),
                    ) {
                        Text("IA", style = MaterialTheme.typography.labelSmall, color = HudCyan)
                    }
                }
            }
        },
        onClick = onClick,
        modifier = Modifier.padding(start = if (indented) 24.dp else 0.dp),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateField(
    date: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDialog by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = DateFormatter.formatShort(date),
        onValueChange = {},
        readOnly = true,
        label = { Text("Data") },
        trailingIcon = {
            TextButton(onClick = { showDialog = true }) { Text("Alterar") }
        },
        colors = appTextFieldColors(),
        shape = MaterialTheme.shapes.medium,
        modifier = modifier.fillMaxWidth(),
    )

    if (showDialog) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { millis ->
                        onDateSelected(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                    showDialog = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancelar") }
            },
        ) {
            DatePicker(state = state)
        }
    }
}
