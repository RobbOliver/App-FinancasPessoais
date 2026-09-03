package com.robson.financas.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.robson.financas.data.local.entity.AccountEntity
import com.robson.financas.data.local.entity.CategoryEntity
import com.robson.financas.data.local.entity.TagEntity
import com.robson.financas.data.local.entity.TransactionEntity
import com.robson.financas.data.local.relation.TransactionWithDetails
import com.robson.financas.data.repository.AccountRepository
import com.robson.financas.data.repository.CategoryRepository
import com.robson.financas.data.repository.TagRepository
import com.robson.financas.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth
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

    val scheduledTransactions: StateFlow<List<TransactionWithDetails>> = _filter
        .flatMapLatest { f ->
            transactionRepository.observeScheduled().map { list ->
                list.filter {
                    it.transaction.date.year == f.selectedMonth.year &&
                        it.transaction.date.month == f.selectedMonth.month
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
