package com.robson.financas.ui.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.robson.financas.data.local.entity.CategoryEntity
import com.robson.financas.data.local.entity.CategoryType
import com.robson.financas.data.local.relation.GoalProgress
import com.robson.financas.data.repository.CategoryRepository
import com.robson.financas.data.repository.GoalRepository
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

private fun YearMonth.toDbKey(): Int = year * 100 + monthValue

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val goalRepository: GoalRepository,
    categoryRepository: CategoryRepository,
) : ViewModel() {

    private val _yearMonth = MutableStateFlow(YearMonth.now())
    val yearMonth: StateFlow<YearMonth> = _yearMonth

    val rows: StateFlow<List<GoalProgress>> = _yearMonth
        .flatMapLatest { ym -> goalRepository.observeProgressForMonth(ym.toDbKey(), ym.atDay(1), ym.atEndOfMonth()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenseCategories: StateFlow<List<CategoryEntity>> = categoryRepository
        .observeByType(CategoryType.EXPENSE)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val hasAnyGoalThisMonth: StateFlow<Boolean> = rows
        .map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val previousMonthHasGoals: StateFlow<Boolean> = _yearMonth
        .flatMapLatest { ym ->
            val prev = ym.minusMonths(1)
            goalRepository.observeProgressForMonth(prev.toDbKey(), prev.atDay(1), prev.atEndOfMonth())
                .map { it.isNotEmpty() }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /** Só usada para criar uma meta nova a partir do FAB — editar uma existente é na tela de detalhe. */
    private val _editingGoal = MutableStateFlow<EditingGoal?>(null)
    val editingGoal: StateFlow<EditingGoal?> = _editingGoal

    fun prevMonth() = _yearMonth.update { it.minusMonths(1) }
    fun nextMonth() = _yearMonth.update { it.plusMonths(1) }

    fun openNewGoal() {
        _editingGoal.value = EditingGoal()
    }

    fun closeEditingGoal() {
        _editingGoal.value = null
    }

    fun updateEditingName(name: String) = _editingGoal.update { it?.copy(name = name) }

    fun updateEditingAmount(cents: Long) = _editingGoal.update { it?.copy(amountCents = cents) }

    fun toggleEditingCategory(categoryId: Long) = _editingGoal.update {
        it?.let { goal ->
            val allocations = if (categoryId in goal.categoryAllocations) {
                goal.categoryAllocations - categoryId
            } else {
                goal.categoryAllocations + (categoryId to 0L)
            }
            goal.copy(categoryAllocations = allocations)
        }
    }

    fun updateEditingCategoryAllocation(categoryId: Long, allocatedCents: Long) = _editingGoal.update {
        it?.let { goal ->
            if (categoryId !in goal.categoryAllocations) return@let goal
            goal.copy(categoryAllocations = goal.categoryAllocations + (categoryId to allocatedCents))
        }
    }

    fun saveEditingGoal() {
        val goal = _editingGoal.value ?: return
        if (!goal.isValid) return
        viewModelScope.launch {
            goalRepository.createGoal(_yearMonth.value.toDbKey(), goal.name.trim(), goal.amountCents, goal.categoryAllocations)
            _editingGoal.value = null
        }
    }

    fun importFromPreviousMonth() {
        val current = _yearMonth.value
        viewModelScope.launch {
            goalRepository.importFromPreviousMonth(current.minusMonths(1).toDbKey(), current.toDbKey())
        }
    }
}
