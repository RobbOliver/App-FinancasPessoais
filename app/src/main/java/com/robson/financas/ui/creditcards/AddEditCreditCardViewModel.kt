package com.robson.financas.ui.creditcards

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.robson.financas.data.local.entity.AccountEntity
import com.robson.financas.data.local.entity.CreditCardEntity
import com.robson.financas.data.repository.AccountRepository
import com.robson.financas.data.repository.CreditCardRepository
import com.robson.financas.ui.common.ColorCatalog
import com.robson.financas.ui.common.IconCatalog
import com.robson.financas.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddEditCreditCardUiState(
    val name: String = "",
    val colorHex: String = ColorCatalog.hexValues.first(),
    val icon: String = IconCatalog.defaultKey,
    val closingDay: Int = 1,
    val dueDay: Int = 10,
    val limitCents: Long = 0L,
    val paymentAccountId: Long? = null,
    val existingCard: CreditCardEntity? = null,
    val isSaved: Boolean = false,
) {
    val isEditing: Boolean get() = existingCard != null
    val isValid: Boolean get() = name.isNotBlank() && paymentAccountId != null &&
        closingDay in 1..31 && dueDay in 1..31
}

@HiltViewModel
class AddEditCreditCardViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val creditCardRepository: CreditCardRepository,
    accountRepository: AccountRepository,
) : ViewModel() {

    private val cardId: Long? =
        savedStateHandle.get<Long>(Screen.AddEditCreditCard.ARG_CARD_ID)?.takeIf { it >= 0 }

    private val _uiState = MutableStateFlow(AddEditCreditCardUiState())
    val uiState: StateFlow<AddEditCreditCardUiState> = _uiState

    val accounts: StateFlow<List<AccountEntity>> = accountRepository
        .observeActiveAccountsWithBalance()
        .map { list -> list.map { it.account } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        cardId?.let { id ->
            viewModelScope.launch {
                creditCardRepository.getById(id)?.let { card ->
                    _uiState.update {
                        it.copy(
                            name = card.name,
                            colorHex = card.colorHex,
                            icon = card.icon,
                            closingDay = card.closingDay,
                            dueDay = card.dueDay,
                            limitCents = card.limitCents,
                            paymentAccountId = card.paymentAccountId,
                            existingCard = card,
                        )
                    }
                }
            }
        }
    }

    fun updateName(name: String) = _uiState.update { it.copy(name = name) }
    fun updateColor(hex: String) = _uiState.update { it.copy(colorHex = hex) }
    fun updateIcon(icon: String) = _uiState.update { it.copy(icon = icon) }
    fun updateClosingDay(day: Int) = _uiState.update { it.copy(closingDay = day) }
    fun updateDueDay(day: Int) = _uiState.update { it.copy(dueDay = day) }
    fun updateLimit(cents: Long) = _uiState.update { it.copy(limitCents = cents) }
    fun updatePaymentAccount(accountId: Long) = _uiState.update { it.copy(paymentAccountId = accountId) }

    fun save() {
        val state = _uiState.value
        if (!state.isValid) return
        viewModelScope.launch {
            val entity = CreditCardEntity(
                id = state.existingCard?.id ?: 0,
                name = state.name.trim(),
                colorHex = state.colorHex,
                icon = state.icon,
                closingDay = state.closingDay,
                dueDay = state.dueDay,
                limitCents = state.limitCents,
                paymentAccountId = state.paymentAccountId!!,
                isArchived = state.existingCard?.isArchived ?: false,
            )
            if (state.existingCard != null) {
                creditCardRepository.update(entity)
            } else {
                creditCardRepository.create(entity)
            }
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
