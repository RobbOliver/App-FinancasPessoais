package com.robson.financas.ui.creditcards

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.robson.financas.data.local.entity.CategoryEntity
import com.robson.financas.data.local.entity.CategoryType
import com.robson.financas.data.repository.CategoryRepository
import com.robson.financas.data.repository.CreditCardRepository
import com.robson.financas.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class AddPurchaseUiState(
    val description: String = "",
    val amountCents: Long = 0L,
    val categoryId: Long? = null,
    val date: LocalDate = LocalDate.now(),
    val installments: Int = 1,
    val isSaved: Boolean = false,
) {
    val isValid: Boolean get() = amountCents > 0 && installments in 1..24
}

@HiltViewModel
class AddPurchaseViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val creditCardRepository: CreditCardRepository,
    categoryRepository: CategoryRepository,
) : ViewModel() {

    private val cardId: Long = checkNotNull(savedStateHandle[Screen.AddPurchase.ARG_CARD_ID])

    private val _uiState = MutableStateFlow(AddPurchaseUiState())
    val uiState: StateFlow<AddPurchaseUiState> = _uiState

    val categories: StateFlow<List<CategoryEntity>> = categoryRepository
        .observeByType(CategoryType.EXPENSE)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateDescription(description: String) = _uiState.update { it.copy(description = description) }
    fun updateAmount(cents: Long) = _uiState.update { it.copy(amountCents = cents) }
    fun updateCategory(categoryId: Long?) = _uiState.update { it.copy(categoryId = categoryId) }
    fun updateDate(date: LocalDate) = _uiState.update { it.copy(date = date) }
    fun updateInstallments(count: Int) = _uiState.update { it.copy(installments = count.coerceIn(1, 24)) }

    fun save() {
        val state = _uiState.value
        if (!state.isValid) return
        viewModelScope.launch {
            val card = creditCardRepository.getById(cardId) ?: return@launch
            creditCardRepository.addPurchase(
                creditCardId = cardId,
                categoryId = state.categoryId,
                description = state.description.trim(),
                totalAmountCents = state.amountCents,
                purchaseDate = state.date,
                closingDay = card.closingDay,
                installments = state.installments,
            )
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
