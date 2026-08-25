package com.robson.financas.ui.creditcards

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.robson.financas.data.local.entity.CreditCardEntity
import com.robson.financas.data.local.relation.CreditCardPurchaseWithCategory
import com.robson.financas.data.repository.CreditCardRepository
import com.robson.financas.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

private fun YearMonth.toDbKey(): Int = year * 100 + monthValue

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class CreditCardDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val creditCardRepository: CreditCardRepository,
) : ViewModel() {

    private val cardId: Long = checkNotNull(savedStateHandle[Screen.CreditCardDetail.ARG_CARD_ID])

    private val _yearMonth = MutableStateFlow(YearMonth.now())
    val yearMonth: StateFlow<YearMonth> = _yearMonth

    private val _card = MutableStateFlow<CreditCardEntity?>(null)
    val card: StateFlow<CreditCardEntity?> = _card

    private val _isPaid = MutableStateFlow(false)
    val isPaid: StateFlow<Boolean> = _isPaid

    val purchases: StateFlow<List<CreditCardPurchaseWithCategory>> = _yearMonth
        .flatMapLatest { ym -> creditCardRepository.observePurchasesForInvoice(cardId, ym.toDbKey()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val invoiceTotalCents: StateFlow<Long> = purchases
        .map { list -> list.sumOf { it.purchase.amountCents } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    init {
        viewModelScope.launch {
            _card.value = creditCardRepository.getById(cardId)
        }
        viewModelScope.launch {
            _yearMonth.collect { ym ->
                _isPaid.value = creditCardRepository.getInvoice(cardId, ym.toDbKey())?.isPaid ?: false
            }
        }
    }

    fun prevMonth() = _yearMonth.update { it.minusMonths(1) }
    fun nextMonth() = _yearMonth.update { it.plusMonths(1) }

    fun payInvoice() {
        val currentCard = _card.value ?: return
        val total = invoiceTotalCents.value
        if (total <= 0) return
        viewModelScope.launch {
            creditCardRepository.payInvoice(currentCard, _yearMonth.value.toDbKey(), total, LocalDate.now())
            _isPaid.value = true
        }
    }
}
