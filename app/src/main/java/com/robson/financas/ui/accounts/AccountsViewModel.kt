package com.robson.financas.ui.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.robson.financas.data.local.entity.AccountEntity
import com.robson.financas.data.local.relation.AccountWithBalance
import com.robson.financas.data.repository.AccountRepository
import com.robson.financas.data.repository.DeletionBlockedException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountsViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
) : ViewModel() {

    val accounts: StateFlow<List<AccountWithBalance>> = accountRepository
        .observeActiveAccountsWithBalance()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _deletionError = MutableStateFlow<String?>(null)
    val deletionError: StateFlow<String?> = _deletionError

    fun deleteAccount(account: AccountEntity, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            try {
                accountRepository.delete(account)
                onResult(true)
            } catch (e: DeletionBlockedException) {
                _deletionError.value = e.message
                onResult(false)
            }
        }
    }

    fun clearDeletionError() {
        _deletionError.value = null
    }
}
