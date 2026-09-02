package com.robson.financas.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.robson.financas.data.local.entity.AccountEntity
import com.robson.financas.data.local.entity.NotificationAppMappingEntity
import com.robson.financas.data.preferences.AiSettingsRepository
import com.robson.financas.data.repository.AccountRepository
import com.robson.financas.data.repository.NotificationAppMappingRepository
import com.robson.financas.domain.fiscal.ai.OpenRouterExtractionClient
import com.robson.financas.notifications.MonitoredApps
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MonitoredAppUiState(
    val packageName: String,
    val displayName: String,
    val mapping: NotificationAppMappingEntity?,
)

enum class ApiKeyTestState { IDLE, TESTING, VALID, INVALID }

data class SettingsUiState(
    val accounts: List<AccountEntity> = emptyList(),
    val apps: List<MonitoredAppUiState> = emptyList(),
    val aiApiKey: String = "",
    val aiModel: String = "",
    val apiKeyTestState: ApiKeyTestState = ApiKeyTestState.IDLE,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val mappingRepository: NotificationAppMappingRepository,
    private val accountRepository: AccountRepository,
    private val aiSettingsRepository: AiSettingsRepository,
    private val openRouterExtractionClient: OpenRouterExtractionClient,
) : ViewModel() {

    private val apiKeyTestState = MutableStateFlow(ApiKeyTestState.IDLE)

    val uiState: StateFlow<SettingsUiState> = combine(
        accountRepository.observeActiveAccountsWithBalance(),
        mappingRepository.observeAll(),
        aiSettingsRepository.apiKey,
        aiSettingsRepository.model,
        apiKeyTestState,
    ) { accountsWithBalance, mappings, apiKey, model, testState ->
        val accounts = accountsWithBalance.map { it.account }
        val mappingByPackage = mappings.associateBy { it.packageName }
        SettingsUiState(
            accounts = accounts,
            apps = MonitoredApps.packageNames.sorted().map { pkg ->
                MonitoredAppUiState(
                    packageName = pkg,
                    displayName = MonitoredApps.displayNames[pkg] ?: pkg,
                    mapping = mappingByPackage[pkg],
                )
            },
            aiApiKey = apiKey.orEmpty(),
            aiModel = model,
            apiKeyTestState = testState,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun setAccountDashboardVisibility(accountId: Long, show: Boolean) {
        viewModelScope.launch { accountRepository.setShowOnDashboard(accountId, show) }
    }

    fun selectAccount(packageName: String, accountId: Long) {
        viewModelScope.launch { mappingRepository.setAccount(packageName, accountId) }
    }

    fun setEnabled(packageName: String, accountId: Long, enabled: Boolean) {
        viewModelScope.launch { mappingRepository.setEnabled(packageName, accountId, enabled) }
    }

    fun updateApiKey(value: String) {
        aiSettingsRepository.setApiKey(value)
        apiKeyTestState.value = ApiKeyTestState.IDLE
    }

    fun updateModel(value: String) {
        aiSettingsRepository.setModel(value)
    }

    fun testApiKey() {
        val key = aiSettingsRepository.apiKey.value
        if (key.isNullOrBlank()) {
            apiKeyTestState.value = ApiKeyTestState.INVALID
            return
        }
        apiKeyTestState.value = ApiKeyTestState.TESTING
        viewModelScope.launch {
            val valid = openRouterExtractionClient.validateApiKey(key)
            apiKeyTestState.value = if (valid) ApiKeyTestState.VALID else ApiKeyTestState.INVALID
        }
    }
}
