package com.robson.financas.ui.objectives

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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.robson.financas.ui.common.ColorPicker
import com.robson.financas.ui.common.CurrencyInputField
import com.robson.financas.ui.common.IconPicker
import com.robson.financas.util.DateFormatter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditObjectiveScreen(
    onBack: () -> Unit,
    viewModel: AddEditObjectiveViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditing) "Editar objetivo" else "Novo objetivo") },
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
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::updateName,
                label = { Text("Nome do objetivo") },
                modifier = Modifier.fillMaxWidth(),
            )

            CurrencyInputField(
                amountCents = uiState.targetCents,
                onAmountChange = viewModel::updateTargetCents,
                label = "Valor alvo",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
            )

            OptionalDateField(
                date = uiState.targetDate,
                onDateSelected = viewModel::updateTargetDate,
                modifier = Modifier.padding(top = 16.dp),
            )

            Text(
                "Cor",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = 20.dp, bottom = 8.dp),
            )
            ColorPicker(selectedHex = uiState.colorHex, onColorSelected = viewModel::updateColor)

            Text(
                "Ícone",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = 20.dp, bottom = 8.dp),
            )
            IconPicker(selectedKey = uiState.icon, onIconSelected = viewModel::updateIcon)

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
private fun OptionalDateField(
    date: LocalDate?,
    onDateSelected: (LocalDate?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDialog by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = date?.let { DateFormatter.formatShort(it) } ?: "",
        onValueChange = {},
        readOnly = true,
        label = { Text("Data alvo (opcional)") },
        trailingIcon = {
            TextButton(onClick = { showDialog = true }) { Text("Alterar") }
        },
        modifier = modifier.fillMaxWidth(),
    )

    if (showDialog) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = (date ?: LocalDate.now()).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
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
