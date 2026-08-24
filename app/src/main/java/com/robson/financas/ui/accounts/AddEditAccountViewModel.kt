package com.robson.financas.ui.accounts

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.robson.financas.data.local.entity.AccountEntity
import com.robson.financas.data.local.entity.AccountType
import com.robson.financas.data.repository.AccountRepository
import com.robson.financas.ui.common.ColorCatalog
import com.robson.financas.ui.common.IconCatalog
import com.robson.financas.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

data class AddEditAccountUiState(
    val name: String = "",
    val type: AccountType = AccountType.CHECKING,
    val initialBalanceCents: Long = 0L,
    val colorHex: String = ColorCatalog.hexValues.first(),
    val icon: String = IconCatalog.defaultKey,
    val existingAccount: AccountEntity? = null,
    val isSaved: Boolean = false,
) {
    val isEditing: Boolean get() = existingAccount != null
}

@HiltViewModel
class AddEditAccountViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val accountRepository: AccountRepository,
) : ViewModel() {

    private val accountId: Long? =
        savedStateHandle.get<Long>(Screen.AddEditAccount.ARG_ACCOUNT_ID)?.takeIf { it >= 0 }

    private val _uiState = MutableStateFlow(AddEditAccountUiState())
    val uiState: StateFlow<AddEditAccountUiState> = _uiState

    init {
        accountId?.let { id ->
            viewModelScope.launch {
                accountRepository.getById(id)?.let { account ->
                    _uiState.update {
                        it.copy(
                            name = account.name,
                            type = account.type,
                            initialBalanceCents = account.initialBalanceCents,
                            colorHex = account.colorHex,
                            icon = account.icon,
                            existingAccount = account,
                        )
                    }
                }
            }
        }
    }

    fun updateName(name: String) = _uiState.update { it.copy(name = name) }
    fun updateType(type: AccountType) = _uiState.update { it.copy(type = type) }
    fun updateInitialBalance(cents: Long) = _uiState.update { it.copy(initialBalanceCents = cents) }
    fun updateColor(hex: String) = _uiState.update { it.copy(colorHex = hex) }
    fun updateIcon(icon: String) = _uiState.update { it.copy(icon = icon) }

    fun save() {
        val state = _uiState.value
        if (state.name.isBlank()) return
        viewModelScope.launch {
            val entity = AccountEntity(
                id = state.existingAccount?.id ?: 0,
                name = state.name.trim(),
                type = state.type,
                initialBalanceCents = state.initialBalanceCents,
                colorHex = state.colorHex,
                icon = state.icon,
                isArchived = state.existingAccount?.isArchived ?: false,
                createdAt = state.existingAccount?.createdAt ?: Instant.now(),
            )
            if (state.existingAccount != null) {
                accountRepository.update(entity)
            } else {
                accountRepository.create(entity)
            }
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
