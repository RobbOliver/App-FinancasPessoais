package com.robson.financas.ui.fiscal.importing

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.robson.financas.ui.designsystem.AppCard
import com.robson.financas.ui.designsystem.AppOutlinedButton
import com.robson.financas.ui.designsystem.AppPrimaryButton
import com.robson.financas.ui.designsystem.EmptyState
import com.robson.financas.ui.theme.RedExpense
import com.robson.financas.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportScreen(
    onBack: () -> Unit,
    onImported: (documentId: Long) -> Unit,
    onScanQrCode: () -> Unit,
    xmlViewModel: ImportViewModel = hiltViewModel(),
    photoViewModel: PhotoImportViewModel = hiltViewModel(),
) {
    val xmlState by xmlViewModel.uiState.collectAsState()
    val photoState by photoViewModel.uiState.collectAsState()

    val pickXmlLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let(xmlViewModel::importXmlFile)
    }
    val pickPhotoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let(photoViewModel::importPhoto)
    }

    LaunchedEffect(xmlState) {
        if (xmlState is ImportUiState.Success) {
            onImported((xmlState as ImportUiState.Success).documentId)
            xmlViewModel.consumeResult()
        }
    }
    LaunchedEffect(photoState) {
        if (photoState is ImportUiState.Success) {
            onImported((photoState as ImportUiState.Success).documentId)
            photoViewModel.consumeResult()
        }
    }

    val busy = xmlState is ImportUiState.Loading || photoState is ImportUiState.Loading

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Importar nota fiscal") },
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
                .fillMaxSize()
                .padding(innerPadding)
                .padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg),
        ) {
            EmptyState(
                icon = Icons.Filled.Description,
                title = "Importar XML de NF-e ou NFC-e",
                subtitle = "Selecione o arquivo XML da nota — o app extrai estabelecimento, itens e valores automaticamente. É o caminho mais confiável.",
                actionLabel = if (busy) null else "Escolher arquivo XML",
                onAction = if (busy) null else { { pickXmlLauncher.launch("*/*") } },
            )

            AppOutlinedButton(
                text = "Ler QR Code da nota",
                onClick = onScanQrCode,
                icon = { Icon(Icons.Filled.QrCodeScanner, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
            )

            AppOutlinedButton(
                text = "Importar foto da nota",
                onClick = { pickPhotoLauncher.launch("image/*") },
                icon = { Icon(Icons.Filled.PhotoCamera, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
            )

            ImportStatusCard(xmlState, onImported)
            ImportStatusCard(photoState, onImported)
        }
    }
}

@Composable
private fun ImportStatusCard(state: ImportUiState, onImported: (Long) -> Unit) {
    when (state) {
        is ImportUiState.Loading -> AppCard(modifier = Modifier.fillMaxWidth()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                CircularProgressIndicator()
                Text(
                    "Lendo e classificando itens…",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = Spacing.md),
                )
            }
        }
        is ImportUiState.Duplicate -> AppCard(modifier = Modifier.fillMaxWidth()) {
            Text("Esta nota já foi importada antes.", style = MaterialTheme.typography.bodyMedium)
            AppPrimaryButton(
                text = "Ver nota importada",
                onClick = { onImported(state.existingDocumentId) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.md),
            )
        }
        is ImportUiState.Error -> AppCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                state.message,
                style = MaterialTheme.typography.bodyMedium,
                color = RedExpense,
            )
        }
        else -> Unit
    }
}
