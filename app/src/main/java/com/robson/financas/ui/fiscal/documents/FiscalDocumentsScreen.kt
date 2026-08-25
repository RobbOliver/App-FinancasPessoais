package com.robson.financas.ui.fiscal.documents

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.robson.financas.data.local.entity.fiscal.FiscalDocumentEntity
import com.robson.financas.ui.designsystem.AppCard
import com.robson.financas.ui.designsystem.AppFab
import com.robson.financas.ui.designsystem.EmptyState
import com.robson.financas.ui.theme.RedExpense
import com.robson.financas.ui.theme.Spacing
import com.robson.financas.util.CurrencyFormatter
import com.robson.financas.util.DateFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiscalDocumentsScreen(
    onBack: () -> Unit,
    onAddDocument: () -> Unit,
    onOpenDocument: (Long) -> Unit,
    viewModel: FiscalDocumentsViewModel = hiltViewModel(),
) {
    val documents by viewModel.documents.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notas fiscais") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )
        },
        floatingActionButton = {
            AppFab(onClick = onAddDocument, contentDescription = "Importar nota", icon = Icons.Filled.Add)
        },
    ) { innerPadding ->
        if (documents.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(Spacing.lg),
                contentAlignment = Alignment.Center,
            ) {
                EmptyState(
                    icon = Icons.Filled.Description,
                    title = "Nenhuma nota importada ainda",
                    subtitle = "Toque em + para importar o XML de uma NF-e ou NFC-e.",
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                items(documents, key = { it.id }) { document ->
                    DocumentRow(document = document, onClick = { onOpenDocument(document.id) })
                }
            }
        }
    }
}

@Composable
private fun DocumentRow(document: FiscalDocumentEntity, onClick: () -> Unit) {
    AppCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(DateFormatter.formatShort(document.issuedAt), style = MaterialTheme.typography.bodyLarge)
                Text(
                    document.issuerCnpj?.let { "CNPJ $it" } ?: "Estabelecimento não identificado",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(CurrencyFormatter.formatCents(document.totalCents), style = MaterialTheme.typography.bodyLarge)
                if (document.needsAttention) {
                    Icon(
                        Icons.Filled.ReportProblem,
                        contentDescription = "Precisa de atenção",
                        tint = RedExpense,
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(16.dp),
                    )
                }
            }
        }
    }
}
