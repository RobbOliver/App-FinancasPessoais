package com.robson.financas.ui.fiscal.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.robson.financas.data.repository.fiscal.FiscalTaxonomyRepository
import com.robson.financas.data.repository.fiscal.MicrocategoryBudgetRepository
import com.robson.financas.domain.fiscal.budget.BudgetStatus
import com.robson.financas.domain.fiscal.model.MicrocategoryOption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

data class BudgetRow(val option: MicrocategoryOption, val status: BudgetStatus?)

data class FiscalBudgetUiState(
    val rows: List<BudgetRow> = emptyList(),
    val editingOption: MicrocategoryOption? = null,
)

@HiltViewModel
class FiscalBudgetViewModel @Inject constructor(
    private val budgetRepository: MicrocategoryBudgetRepository,
    private val taxonomyRepository: FiscalTaxonomyRepository,
) : ViewModel() {

    private val yearMonth = YearMonth.from(LocalDate.now())
    private val yearMonthKey = yearMonth.year * 100 + yearMonth.monthValue

    private val options = MutableStateFlow<List<MicrocategoryOption>>(emptyList())
    private val editingOption = MutableStateFlow<MicrocategoryOption?>(null)

    val uiState: StateFlow<FiscalBudgetUiState> = combine(
        options,
        budgetRepository.observeStatuses(yearMonthKey),
        editingOption,
    ) { opts, statuses, editing ->
        val statusByMicrocategory = statuses.toMap()
        FiscalBudgetUiState(
            rows = opts.map { option -> BudgetRow(option, statusByMicrocategory[option.microcategoryId]) },
            editingOption = editing,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FiscalBudgetUiState())

    init {
        viewModelScope.launch { options.value = taxonomyRepository.getMicrocategoryOptions() }
    }

    fun startEditing(option: MicrocategoryOption) = editingOption.update { option }

    fun dismissEditing() = editingOption.update { null }

    fun saveBudget(limitCents: Long) {
        val option = editingOption.value ?: return
        viewModelScope.launch {
            budgetRepository.setBudget(option.microcategoryId, yearMonthKey, limitCents)
            editingOption.update { null }
        }
    }
}
