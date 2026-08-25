package com.robson.financas.ui.creditcards

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.robson.financas.data.local.entity.CategoryEntity
import com.robson.financas.ui.common.CurrencyInputField
import com.robson.financas.ui.designsystem.AppPrimaryButton
import com.robson.financas.ui.designsystem.AppTextField
import com.robson.financas.ui.designsystem.appTextFieldColors
import com.robson.financas.ui.theme.Spacing
import com.robson.financas.util.DateFormatter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPurchaseScreen(
    onBack: () -> Unit,
    viewModel: AddPurchaseViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val categories by viewModel.categories.collectAsState()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nova compra") },
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
            CurrencyInputField(
                amountCents = uiState.amountCents,
                onAmountChange = viewModel::updateAmount,
                label = "Valor total",
                modifier = Modifier.fillMaxWidth(),
            )

            CategoryDropdown(
                options = categories,
                selectedId = uiState.categoryId,
                onSelected = viewModel::updateCategory,
                modifier = Modifier.padding(top = Spacing.lg),
            )

            PurchaseDateField(
                date = uiState.date,
                onDateSelected = viewModel::updateDate,
                modifier = Modifier.padding(top = Spacing.lg),
            )

            AppTextField(
                value = uiState.description,
                onValueChange = viewModel::updateDescription,
                label = "Descrição (opcional)",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.lg),
            )

            AppTextField(
                value = uiState.installments.toString(),
                onValueChange = { input ->
                    val count = input.filter { it.isDigit() }.toIntOrNull() ?: 1
                    viewModel.updateInstallments(count)
                },
                label = "Número de parcelas",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.lg),
            )

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
            colors = appTextFieldColors(),
            shape = MaterialTheme.shapes.medium,
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
private fun PurchaseDateField(
    date: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDialog by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = DateFormatter.formatShort(date),
        onValueChange = {},
        readOnly = true,
        label = { Text("Data da compra") },
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
