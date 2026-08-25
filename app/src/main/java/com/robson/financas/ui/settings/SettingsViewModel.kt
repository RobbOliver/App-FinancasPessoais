package com.robson.financas.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.robson.financas.data.local.entity.AccountEntity
import com.robson.financas.data.local.entity.NotificationAppMappingEntity
import com.robson.financas.data.repository.AccountRepository
import com.robson.financas.data.repository.NotificationAppMappingRepository
import com.robson.financas.notifications.MonitoredApps
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MonitoredAppUiState(
    val packageName: String,
    val displayName: String,
    val mapping: NotificationAppMappingEntity?,
)

data class SettingsUiState(
    val accounts: List<AccountEntity> = emptyList(),
    val apps: List<MonitoredAppUiState> = emptyList(),
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val mappingRepository: NotificationAppMappingRepository,
    accountRepository: AccountRepository,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        accountRepository.observeActiveAccountsWithBalance(),
        mappingRepository.observeAll(),
    ) { accountsWithBalance, mappings ->
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
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun selectAccount(packageName: String, accountId: Long) {
        viewModelScope.launch { mappingRepository.setAccount(packageName, accountId) }
    }

    fun setEnabled(packageName: String, accountId: Long, enabled: Boolean) {
        viewModelScope.launch { mappingRepository.setEnabled(packageName, accountId, enabled) }
    }
}
