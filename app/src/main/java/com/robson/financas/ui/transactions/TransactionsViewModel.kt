package com.robson.financas.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.robson.financas.data.local.entity.AccountEntity
import com.robson.financas.data.local.entity.CategoryEntity
import com.robson.financas.data.local.entity.TagEntity
import com.robson.financas.data.local.entity.TransactionEntity
import com.robson.financas.data.local.entity.TransactionRecurrence
import com.robson.financas.data.local.relation.TransactionWithDetails
import com.robson.financas.data.repository.AccountRepository
import com.robson.financas.data.repository.CategoryRepository
import com.robson.financas.data.repository.TagRepository
import com.robson.financas.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class TransactionsFilterState(
    val accountId: Long? = null,
    val categoryId: Long? = null,
    val tagId: Long? = null,
    val selectedMonth: YearMonth = YearMonth.now(),
    val onlyNeedsReview: Boolean = false,
    val onlyScheduled: Boolean = false,
    val onlyFavorite: Boolean = false,
    val excludeScheduled: Boolean = true,
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    accountRepository: AccountRepository,
    categoryRepository: CategoryRepository,
    tagRepository: TagRepository,
) : ViewModel() {

    private val _filter = MutableStateFlow(TransactionsFilterState())
    val filter: StateFlow<TransactionsFilterState> = _filter

    val accounts: StateFlow<List<AccountEntity>> = accountRepository
        .observeActiveAccountsWithBalance()
        .map { list -> list.map { it.account } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<CategoryEntity>> = categoryRepository
        .observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tags: StateFlow<List<TagEntity>> = tagRepository
        .observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val scheduledTransactions: StateFlow<List<TransactionWithDetails>> = combine(
        _filter,
        transactionRepository.observeScheduled(),
        transactionRepository.observeAllRecurring(),
    ) { f, actual, recurring ->
        val month = f.selectedMonth
        val monthStart = month.atDay(1)
        val monthEnd = month.atEndOfMonth()

        val actualThisMonth = actual.filter {
            val d = it.transaction.date
            d.year == month.year && d.month == month.month
        }

        val projected = recurring.flatMap { item ->
            val t = item.transaction
            val originalMonth = YearMonth.from(t.date)
            val freq = t.recurrenceFrequency ?: return@flatMap emptyList()
            if (originalMonth == month || month.isBefore(originalMonth)) return@flatMap emptyList()
            val endDate = t.recurrenceEndDate
            projectOccurrences(t.date, freq, monthStart, monthEnd)
                .filter { date -> endDate == null || !date.isAfter(endDate) }
                .map { date -> item.copy(transaction = t.copy(date = date)) }
        }

        (actualThisMonth + projected).sortedBy { it.transaction.date }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<TransactionWithDetails>> = _filter
        .flatMapLatest { f ->
            transactionRepository.observeFiltered(
                f.accountId,
                f.categoryId,
                f.selectedMonth.atDay(1),
                f.selectedMonth.atEndOfMonth(),
                f.onlyNeedsReview,
                f.onlyScheduled,
                f.onlyFavorite,
                f.tagId,
                f.excludeScheduled,
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateAccountFilter(id: Long?) = _filter.update { it.copy(accountId = id) }
    fun updateCategoryFilter(id: Long?) = _filter.update { it.copy(categoryId = id) }
    fun updateTagFilter(id: Long?) = _filter.update { it.copy(tagId = id) }
    fun previousMonth() = _filter.update { it.copy(selectedMonth = it.selectedMonth.minusMonths(1)) }
    fun nextMonth() = _filter.update { it.copy(selectedMonth = it.selectedMonth.plusMonths(1)) }
    fun toggleNeedsReview(only: Boolean) = _filter.update { it.copy(onlyNeedsReview = only) }
    fun toggleScheduled(only: Boolean) = _filter.update { it.copy(onlyScheduled = only) }
    fun toggleFavorite(only: Boolean) = _filter.update { it.copy(onlyFavorite = only) }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch { transactionRepository.delete(transaction) }
    }

    fun togglePaid(transaction: TransactionEntity) {
        viewModelScope.launch { transactionRepository.update(transaction.copy(isPaid = !transaction.isPaid)) }
    }
}

private fun projectOccurrences(
    originalDate: LocalDate,
    freq: TransactionRecurrence,
    monthStart: LocalDate,
    monthEnd: LocalDate,
): List<LocalDate> {
    val originalMonth = YearMonth.from(originalDate)
    val selectedMonth = YearMonth.from(monthStart)
    val monthsBetween = originalMonth.until(selectedMonth, ChronoUnit.MONTHS)
    val cappedDay = minOf(originalDate.dayOfMonth, monthEnd.dayOfMonth)

    return when (freq) {
        TransactionRecurrence.MONTHLY ->
            listOf(monthStart.withDayOfMonth(cappedDay))

        TransactionRecurrence.BIMONTHLY ->
            if (monthsBetween % 2 == 0L) listOf(monthStart.withDayOfMonth(cappedDay)) else emptyList()

        TransactionRecurrence.QUARTERLY ->
            if (monthsBetween % 3 == 0L) listOf(monthStart.withDayOfMonth(cappedDay)) else emptyList()

        TransactionRecurrence.YEARLY ->
            if (selectedMonth.month == originalMonth.month) listOf(monthStart.withDayOfMonth(cappedDay)) else emptyList()

        TransactionRecurrence.WEEKLY -> buildList {
            var next = originalDate
            while (next.isBefore(monthStart)) next = next.plusWeeks(1)
            while (!next.isAfter(monthEnd)) { add(next); next = next.plusWeeks(1) }
        }

        TransactionRecurrence.BIWEEKLY -> buildList {
            var next = originalDate
            while (next.isBefore(monthStart)) next = next.plusWeeks(2)
            while (!next.isAfter(monthEnd)) { add(next); next = next.plusWeeks(2) }
        }

        TransactionRecurrence.DAILY -> buildList {
            var current = monthStart
            while (!current.isAfter(monthEnd)) { add(current); current = current.plusDays(1) }
        }
    }
}
