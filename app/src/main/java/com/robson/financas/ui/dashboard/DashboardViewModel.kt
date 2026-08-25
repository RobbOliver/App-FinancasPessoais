package com.robson.financas.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.robson.financas.data.local.relation.AccountWithBalance
import com.robson.financas.data.local.relation.CategoryExpenseSlice
import com.robson.financas.data.local.relation.CreditCardSummary
import com.robson.financas.data.local.relation.MonthSummary
import com.robson.financas.data.local.relation.TransactionWithDetails
import com.robson.financas.data.repository.AccountRepository
import com.robson.financas.data.repository.CreditCardRepository
import com.robson.financas.data.repository.TransactionRepository
import com.robson.financas.ui.common.MonthBarData
import com.robson.financas.util.DateFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

data class DashboardUiState(
    val accounts: List<AccountWithBalance> = emptyList(),
    val monthSummary: MonthSummary = MonthSummary(0L, 0L),
    val recentTransactions: List<TransactionWithDetails> = emptyList(),
    val expenseByCategory: List<CategoryExpenseSlice> = emptyList(),
    val monthlyHistory: List<MonthBarData> = emptyList(),
    val pendingIncomeCents: Long = 0L,
    val pendingExpenseCents: Long = 0L,
    val creditCards: List<CreditCardSummary> = emptyList(),
) {
    val totalBalanceCents: Long get() = accounts.sumOf { it.balanceCents }
    val hasPending: Boolean get() = pendingIncomeCents > 0 || pendingExpenseCents > 0
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    accountRepository: AccountRepository,
    transactionRepository: TransactionRepository,
    creditCardRepository: CreditCardRepository,
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = run {
        val today = LocalDate.now()
        val start = today.withDayOfMonth(1)
        val end = today.withDayOfMonth(today.lengthOfMonth())
        val thisMonth = YearMonth.from(today)

        val monthlyHistoryFlow = combine(
            (5 downTo 0).map { offset ->
                val ym = thisMonth.minusMonths(offset.toLong())
                transactionRepository.observeMonthSummary(ym.atDay(1), ym.atEndOfMonth())
                    .map { summary -> MonthBarData(DateFormatter.formatMonthAbbrev(ym), summary.incomeCents, summary.expenseCents) }
            },
        ) { it.toList() }

        val baseFlow = combine(
            accountRepository.observeActiveAccountsWithBalance(),
            transactionRepository.observeMonthSummary(start, end),
            transactionRepository.observeRecent(5),
            transactionRepository.observeExpenseByCategoryForMonth(start, end),
            monthlyHistoryFlow,
        ) { accounts, summary, recent, expenseByCategory, monthlyHistory ->
            DashboardUiState(
                accounts = accounts,
                monthSummary = summary,
                recentTransactions = recent,
                expenseByCategory = expenseByCategory,
                monthlyHistory = monthlyHistory,
            )
        }

        combine(
            baseFlow,
            transactionRepository.observePendingSummary(start, end),
            creditCardRepository.observeCardsWithInvoiceSummary(thisMonth.year * 100 + thisMonth.monthValue),
        ) { base, pending, creditCards ->
            base.copy(
                pendingIncomeCents = pending.pendingIncomeCents,
                pendingExpenseCents = pending.pendingExpenseCents,
                creditCards = creditCards,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())
    }
}
