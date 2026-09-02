package com.robson.financas.ui.goals

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.robson.financas.data.local.entity.CategoryEntity
import com.robson.financas.data.local.entity.CategoryType
import com.robson.financas.data.local.entity.GoalEntity
import com.robson.financas.data.local.relation.GoalCategoryDetail
import com.robson.financas.data.repository.CategoryRepository
import com.robson.financas.data.repository.GoalRepository
import com.robson.financas.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

data class GoalDetailUiState(
    val goal: GoalEntity? = null,
    val categoryDetails: List<GoalCategoryDetail> = emptyList(),
    val deleted: Boolean = false,
) {
    val spentCents: Long get() = categoryDetails.sumOf { it.spentCents }
    val allocatedCents: Long get() = categoryDetails.sumOf { it.allocatedCents }
}

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class GoalDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val goalRepository: GoalRepository,
    categoryRepository: CategoryRepository,
) : ViewModel() {

    private val goalId: Long = checkNotNull(savedStateHandle[Screen.GoalDetail.ARG_GOAL_ID])

    private val _deleted = MutableStateFlow(false)

    private val goalFlow = goalRepository.observeGoal(goalId)

    val uiState: StateFlow<GoalDetailUiState> = combine(
        goalFlow,
        goalFlow.flatMapLatest { goal ->
            if (goal == null) {
                flowOf(emptyList())
            } else {
                val ym = goal.yearMonth.toYearMonth()
                goalRepository.observeCategoryDetailsForGoal(goalId, ym.atDay(1), ym.atEndOfMonth())
            }
        },
        _deleted,
    ) { goal, categoryDetails, deleted ->
        GoalDetailUiState(goal = goal, categoryDetails = categoryDetails, deleted = deleted)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GoalDetailUiState())

    val expenseCategories: StateFlow<List<CategoryEntity>> = categoryRepository
        .observeByType(CategoryType.EXPENSE)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _editingGoal = MutableStateFlow<EditingGoal?>(null)
    val editingGoal: StateFlow<EditingGoal?> = _editingGoal

    fun openEditGoal() {
        val goal = uiState.value.goal ?: return
        viewModelScope.launch {
            val allocations = goalRepository.getCategoryAllocationsForGoal(goalId)
            _editingGoal.value = EditingGoal(
                id = goalId,
                name = goal.name,
                amountCents = goal.amountCents,
                categoryAllocations = allocations,
            )
        }
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
        val id = goal.id ?: return
        viewModelScope.launch {
            goalRepository.updateGoal(id, goal.name.trim(), goal.amountCents, goal.categoryAllocations)
            _editingGoal.value = null
        }
    }

    fun deleteGoal() {
        viewModelScope.launch {
            goalRepository.deleteGoal(goalId)
            _deleted.value = true
        }
    }
}

private fun Int.toYearMonth(): YearMonth = YearMonth.of(this / 100, this % 100)
