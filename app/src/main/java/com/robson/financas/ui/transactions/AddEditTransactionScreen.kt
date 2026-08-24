package com.robson.financas.ui.transactions

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
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
import com.robson.financas.data.local.entity.AccountEntity
import com.robson.financas.data.local.entity.CategoryEntity
import com.robson.financas.data.local.entity.TransactionType
import com.robson.financas.ui.common.CurrencyInputField
import com.robson.financas.ui.common.label
import com.robson.financas.util.DateFormatter
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
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
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
                    .padding(top = 16.dp),
            )

            AccountDropdown(
                label = if (uiState.type == TransactionType.TRANSFER) "Conta de origem" else "Conta",
                options = accounts,
                selectedId = uiState.accountId,
                onSelected = viewModel::updateAccount,
                modifier = Modifier.padding(top = 16.dp),
            )

            if (uiState.type == TransactionType.TRANSFER) {
                AccountDropdown(
                    label = "Conta de destino",
                    options = transferAccountOptions,
                    selectedId = uiState.transferToAccountId,
                    onSelected = viewModel::updateTransferToAccount,
                    modifier = Modifier.padding(top = 16.dp),
                )
            } else {
                CategoryDropdown(
                    options = categoryOptions,
                    selectedId = uiState.categoryId,
                    onSelected = viewModel::updateCategory,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }

            DateField(
                date = uiState.date,
                onDateSelected = viewModel::updateDate,
                modifier = Modifier.padding(top = 16.dp),
            )

            OutlinedTextField(
                value = uiState.description,
                onValueChange = viewModel::updateDescription,
                label = { Text("Descrição (opcional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
            )

            Button(
                onClick = viewModel::save,
                enabled = uiState.isValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
            ) {
                Text("Salvar")
            }
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
    val selectedLabel = options.find { it.id == selectedId }?.name ?: "Selecione uma categoria"

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text("Categoria") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
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
