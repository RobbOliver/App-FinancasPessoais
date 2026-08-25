package com.robson.financas.ui.fiscal.scanning

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
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
import com.robson.financas.ui.designsystem.AppPrimaryButton
import com.robson.financas.ui.theme.GreenIncome
import com.robson.financas.ui.theme.RedExpense
import com.robson.financas.ui.theme.Spacing

/**
 * O QR Code da NFC-e não carrega os itens da compra — só a chave de acesso. Por isso esta tela
 * nunca cria uma nota "adivinhando" total/data/itens: só valida a chave e conduz para a
 * importação do XML, que é quem realmente tem os dados completos (limitação assumida
 * conscientemente, ver seção 2 do plano de arquitetura).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScanResultScreen(
    onBack: () -> Unit,
    onGoToXmlImport: () -> Unit,
    onOpenDocument: (Long) -> Unit,
    viewModel: QrScanResultViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("QR Code lido") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(Spacing.lg),
            contentAlignment = Alignment.Center,
        ) {
            when (val state = uiState) {
                is QrScanResultUiState.Loading -> Unit
                is QrScanResultUiState.Invalid -> ResultBody(
                    icon = Icons.Filled.Error,
                    iconTint = RedExpense,
                    title = "Chave de acesso inválida",
                    subtitle = "O dígito verificador não confere. Tente ler o QR Code novamente.",
                    actionLabel = "Tentar de novo",
                    onAction = onBack,
                )
                is QrScanResultUiState.AlreadyImported -> ResultBody(
                    icon = Icons.Filled.CheckCircle,
                    iconTint = GreenIncome,
                    title = "Esta nota já foi importada",
                    subtitle = "Você já tem os dados completos desta compra.",
                    actionLabel = "Ver nota",
                    onAction = { onOpenDocument(state.documentId) },
                )
                is QrScanResultUiState.ValidNotYetImported -> ResultBody(
                    icon = Icons.Filled.CheckCircle,
                    iconTint = GreenIncome,
                    title = "Chave validada",
                    subtitle = "O QR Code não traz os itens da compra — importe o XML desta nota " +
                        "para extrair estabelecimento, itens e valores automaticamente.",
                    actionLabel = "Importar XML",
                    onAction = onGoToXmlImport,
                )
            }
        }
    }
}

@Composable
private fun ResultBody(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    title: String,
    subtitle: String,
    actionLabel: String,
    onAction: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(48.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = Spacing.lg),
        )
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = Spacing.sm),
        )
        AppPrimaryButton(
            text = actionLabel,
            onClick = onAction,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Spacing.xl),
        )
    }
}
