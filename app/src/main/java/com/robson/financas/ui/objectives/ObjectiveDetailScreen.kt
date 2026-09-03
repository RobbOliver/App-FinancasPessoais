package com.robson.financas.ui.objectives

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.robson.financas.ui.common.CurrencyInputField
import com.robson.financas.ui.common.GoalProgressBar
import com.robson.financas.ui.designsystem.AppCard
import com.robson.financas.ui.designsystem.AppFab
import com.robson.financas.ui.designsystem.AppTextField
import com.robson.financas.ui.theme.GreenIncome
import com.robson.financas.ui.theme.RedExpense
import com.robson.financas.ui.theme.Spacing
import com.robson.financas.util.CurrencyFormatter
import com.robson.financas.util.DateFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObjectiveDetailScreen(
    onBack: () -> Unit,
    viewModel: ObjectiveDetailViewModel = hiltViewModel(),
) {
    val progress by viewModel.progress.collectAsState()
    val contributions by viewModel.contributions.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                expandedHeight = 40.dp,
                title = { Text(progress?.goal?.name ?: "Objetivo") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )
        },
        floatingActionButton = {
            AppFab(onClick = { showAddDialog = true }, contentDescription = "Novo aporte", icon = Icons.Filled.Add)
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            progress?.let { p ->
                item {
                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        GoalProgressBar(
                            goalCents = p.goal.targetCents,
                            spentCents = p.goal.targetCents - p.savedCents,
                        )
                        Text(
                            "${CurrencyFormatter.formatCents(p.savedCents)} de ${CurrencyFormatter.formatCents(p.goal.targetCents)}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = Spacing.sm),
                        )
                        p.goal.targetDate?.let { date ->
                            Text(
                                "Meta para ${DateFormatter.formatShort(date)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
            item {
                Text("Aportes", style = MaterialTheme.typography.titleMedium)
            }
            if (contributions.isEmpty()) {
                item {
                    Text(
                        "Nenhum aporte registrado ainda.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                items(contributions, key = { it.id }) { contribution ->
                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(DateFormatter.formatShort(contribution.date), style = MaterialTheme.typography.bodyMedium)
                                if (contribution.note.isNotBlank()) {
                                    Text(
                                        contribution.note,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    CurrencyFormatter.formatCents(contribution.amountCents),
                                    color = if (contribution.amountCents >= 0) GreenIncome else RedExpense,
                                )
                                IconButton(onClick = { viewModel.deleteContribution(contribution) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Excluir")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddContributionDialog(
            onDismiss = { showAddDialog = false },
            onSave = { amount, note ->
                viewModel.addContribution(amount, note)
                showAddDialog = false
            },
        )
    }
}

@Composable
private fun AddContributionDialog(
    onDismiss: () -> Unit,
    onSave: (Long, String) -> Unit,
) {
    var amountCents by remember { mutableLongStateOf(0L) }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo aporte") },
        text = {
            Column {
                CurrencyInputField(amountCents = amountCents, onAmountChange = { amountCents = it })
                AppTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = "Nota (opcional)",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Spacing.md),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(amountCents, note) }, enabled = amountCents > 0) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    )
}
