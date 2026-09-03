package com.robson.financas.ui.creditcards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.robson.financas.data.local.entity.AccountEntity
import com.robson.financas.ui.common.ColorPicker
import com.robson.financas.ui.common.CurrencyInputField
import com.robson.financas.ui.common.IconPicker
import com.robson.financas.ui.designsystem.AppPrimaryButton
import com.robson.financas.ui.designsystem.AppTextField
import com.robson.financas.ui.designsystem.appTextFieldColors
import com.robson.financas.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCreditCardScreen(
    onBack: () -> Unit,
    viewModel: AddEditCreditCardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val accounts by viewModel.accounts.collectAsState()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onBack()
    }

    LaunchedEffect(accounts, uiState.paymentAccountId) {
        if (uiState.paymentAccountId == null && accounts.isNotEmpty()) {
            viewModel.updatePaymentAccount(accounts.first().id)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                expandedHeight = 40.dp,
                title = { Text(if (uiState.isEditing) "Editar cartão" else "Novo cartão") },
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
            AppTextField(
                value = uiState.name,
                onValueChange = viewModel::updateName,
                label = "Nome do cartão",
                modifier = Modifier.fillMaxWidth(),
            )

            CurrencyInputField(
                amountCents = uiState.limitCents,
                onAmountChange = viewModel::updateLimit,
                label = "Limite",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.lg),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.lg),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                DayField(
                    label = "Dia de fechamento",
                    value = uiState.closingDay,
                    onValueChange = viewModel::updateClosingDay,
                    modifier = Modifier.weight(1f),
                )
                DayField(
                    label = "Dia de vencimento",
                    value = uiState.dueDay,
                    onValueChange = viewModel::updateDueDay,
                    modifier = Modifier.weight(1f),
                )
            }

            AccountDropdown(
                label = "Conta de pagamento",
                options = accounts,
                selectedId = uiState.paymentAccountId,
                onSelected = viewModel::updatePaymentAccount,
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
                enabled = uiState.isValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.xl),
            )
        }
    }
}

@Composable
private fun DayField(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppTextField(
        value = value.toString(),
        onValueChange = { input ->
            val day = input.filter { it.isDigit() }.toIntOrNull() ?: 1
            onValueChange(day.coerceIn(1, 31))
        },
        label = label,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier,
    )
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
