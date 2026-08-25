package com.robson.financas.ui.creditcards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.robson.financas.data.local.relation.CreditCardSummary
import com.robson.financas.data.repository.CreditCardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class CreditCardsViewModel @Inject constructor(
    creditCardRepository: CreditCardRepository,
) : ViewModel() {

    val cards: StateFlow<List<CreditCardSummary>> = run {
        val today = YearMonth.now()
        val yearMonth = today.year * 100 + today.monthValue
        creditCardRepository
            .observeCardsWithInvoiceSummary(yearMonth)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }
}
