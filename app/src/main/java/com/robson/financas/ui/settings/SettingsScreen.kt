package com.robson.financas.ui.settings

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.robson.financas.data.local.entity.AccountEntity
import com.robson.financas.data.preferences.DEFAULT_OPENROUTER_MODEL
import com.robson.financas.ui.designsystem.AppCard
import com.robson.financas.ui.designsystem.AppOutlinedButton
import com.robson.financas.ui.designsystem.AppPrimaryButton
import com.robson.financas.ui.designsystem.AppTextField
import com.robson.financas.ui.designsystem.appTextFieldColors
import com.robson.financas.ui.theme.GreenIncome
import com.robson.financas.ui.theme.RedExpense
import com.robson.financas.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurações") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding),
            contentPadding = PaddingValues(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            item {
                Text(
                    "Contas no Resumo",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    "Escolha quais contas aparecem no card de saldo do Resumo — o saldo total continua somando todas.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = Spacing.xs, bottom = Spacing.sm),
                )
            }
            items(uiState.accounts, key = { it.id }) { account ->
                AccountVisibilityCard(
                    account = account,
                    onShowOnDashboardChanged = { show -> viewModel.setAccountDashboardVisibility(account.id, show) },
                )
            }
            item {
                Text(
                    "Captura automática de Pix e pagamentos",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = Spacing.lg),
                )
                Text(
                    "Escolha para qual conta lançar as transações detectadas em cada app.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = Spacing.xs, bottom = Spacing.sm),
                )
            }
            item {
                AppPrimaryButton(
                    text = "Permitir acesso a notificações",
                    onClick = {
                        context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            items(uiState.apps, key = { it.packageName }) { app ->
                MonitoredAppCard(
                    displayName = app.displayName,
                    accounts = uiState.accounts,
                    selectedAccountId = app.mapping?.accountId,
                    enabled = app.mapping?.enabled ?: false,
                    onAccountSelected = { accountId -> viewModel.selectAccount(app.packageName, accountId) },
                    onEnabledChanged = { enabled ->
                        app.mapping?.accountId?.let { accountId ->
                            viewModel.setEnabled(app.packageName, accountId, enabled)
                        }
                    },
                )
            }
            item {
                Text(
                    "Leitura de notas por IA (OpenRouter)",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = Spacing.lg),
                )
                Text(
                    "Ao escanear o QR Code de uma nota, o app busca a página pública da Sefaz e " +
                        "envia esse conteúdo para o modelo de IA escolhido, que devolve os itens já " +
                        "estruturados. Isso é a única etapa do app que sai do aparelho — o restante " +
                        "continua 100% local.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = Spacing.xs, bottom = Spacing.sm),
                )
            }
            item {
                AiSettingsCard(
                    apiKey = uiState.aiApiKey,
                    model = uiState.aiModel,
                    testState = uiState.apiKeyTestState,
                    onApiKeyChange = viewModel::updateApiKey,
                    onModelChange = viewModel::updateModel,
                    onTestApiKey = viewModel::testApiKey,
                )
            }
        }
    }
}

@Composable
private fun AccountVisibilityCard(
    account: AccountEntity,
    onShowOnDashboardChanged: (Boolean) -> Unit,
) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(account.name, style = MaterialTheme.typography.bodyLarge)
            Switch(checked = account.showOnDashboard, onCheckedChange = onShowOnDashboardChanged)
        }
    }
}

@Composable
private fun AiSettingsCard(
    apiKey: String,
    model: String,
    testState: ApiKeyTestState,
    onApiKeyChange: (String) -> Unit,
    onModelChange: (String) -> Unit,
    onTestApiKey: () -> Unit,
) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        AppTextField(
            value = apiKey,
            onValueChange = onApiKeyChange,
            label = "Chave de API da OpenRouter",
            placeholder = "sk-or-...",
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
        )
        AppTextField(
            value = model,
            onValueChange = onModelChange,
            label = "Modelo",
            placeholder = DEFAULT_OPENROUTER_MODEL,
            modifier = Modifier.fillMaxWidth().padding(top = Spacing.sm),
        )
        AppOutlinedButton(
            text = when (testState) {
                ApiKeyTestState.TESTING -> "Testando..."
                else -> "Testar chave"
            },
            onClick = onTestApiKey,
            enabled = testState != ApiKeyTestState.TESTING && apiKey.isNotBlank(),
            modifier = Modifier.fillMaxWidth().padding(top = Spacing.sm),
        )
        when (testState) {
            ApiKeyTestState.VALID -> Text(
                "Chave válida.",
                style = MaterialTheme.typography.bodySmall,
                color = GreenIncome,
                modifier = Modifier.padding(top = Spacing.xs),
            )
            ApiKeyTestState.INVALID -> Text(
                "Não conseguimos validar essa chave.",
                style = MaterialTheme.typography.bodySmall,
                color = RedExpense,
                modifier = Modifier.padding(top = Spacing.xs),
            )
            else -> Unit
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MonitoredAppCard(
    displayName: String,
    accounts: List<AccountEntity>,
    selectedAccountId: Long?,
    enabled: Boolean,
    onAccountSelected: (Long) -> Unit,
    onEnabledChanged: (Boolean) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedAccountName = accounts.find { it.id == selectedAccountId }?.name ?: "Nenhuma conta"

    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(displayName, style = MaterialTheme.typography.titleSmall)
            Switch(
                checked = enabled && selectedAccountId != null,
                onCheckedChange = onEnabledChanged,
                enabled = selectedAccountId != null,
            )
        }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier.padding(top = Spacing.sm),
        ) {
            OutlinedTextField(
                value = selectedAccountName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Conta") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = appTextFieldColors(),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                accounts.forEach { account ->
                    DropdownMenuItem(
                        text = { Text(account.name) },
                        onClick = {
                            onAccountSelected(account.id)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}
