package com.robson.financas.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.robson.financas.data.local.relation.AccountWithBalance
import com.robson.financas.data.local.relation.MonthSummary
import com.robson.financas.data.local.relation.TransactionWithDetails
import com.robson.financas.data.repository.AccountRepository
import com.robson.financas.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

data class DashboardUiState(
    val accounts: List<AccountWithBalance> = emptyList(),
    val monthSummary: MonthSummary = MonthSummary(0L, 0L),
    val recentTransactions: List<TransactionWithDetails> = emptyList(),
) {
    val totalBalanceCents: Long get() = accounts.sumOf { it.balanceCents }
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    accountRepository: AccountRepository,
    transactionRepository: TransactionRepository,
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = run {
        val today = LocalDate.now()
        val start = today.withDayOfMonth(1)
        val end = today.withDayOfMonth(today.lengthOfMonth())

        combine(
            accountRepository.observeActiveAccountsWithBalance(),
            transactionRepository.observeMonthSummary(start, end),
            transactionRepository.observeRecent(5),
        ) { accounts, summary, recent ->
            DashboardUiState(
                accounts = accounts,
                monthSummary = summary,
                recentTransactions = recent,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())
    }
}
