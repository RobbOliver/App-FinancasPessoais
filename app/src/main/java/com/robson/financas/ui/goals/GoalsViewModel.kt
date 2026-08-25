package com.robson.financas.ui.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.robson.financas.data.local.entity.CategoryType
import com.robson.financas.data.repository.CategoryRepository
import com.robson.financas.data.repository.GoalRepository
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
import java.time.YearMonth
import javax.inject.Inject

data class GoalRow(
    val categoryId: Long,
    val categoryName: String,
    val categoryIcon: String,
    val categoryColorHex: String,
    val parentCategoryId: Long?,
    val amountCents: Long?,
    val spentCents: Long,
) {
    val hasGoal: Boolean get() = amountCents != null
    val remainingCents: Long get() = (amountCents ?: 0L) - spentCents
}

private fun YearMonth.toDbKey(): Int = year * 100 + monthValue

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val goalRepository: GoalRepository,
    categoryRepository: CategoryRepository,
) : ViewModel() {

    private val _yearMonth = MutableStateFlow(YearMonth.now())
    val yearMonth: StateFlow<YearMonth> = _yearMonth

    val rows: StateFlow<List<GoalRow>> = combine(
        _yearMonth.flatMapLatest { ym ->
            goalRepository.observeProgressForMonth(ym.toDbKey(), ym.atDay(1), ym.atEndOfMonth())
        },
        categoryRepository.observeByType(CategoryType.EXPENSE),
    ) { progress, categories ->
        val progressByCategory = progress.associateBy { it.categoryId }
        categories.map { category ->
            val p = progressByCategory[category.id]
            GoalRow(
                categoryId = category.id,
                categoryName = category.name,
                categoryIcon = category.icon,
                categoryColorHex = category.colorHex,
                parentCategoryId = category.parentCategoryId,
                amountCents = p?.amountCents,
                spentCents = p?.spentCents ?: 0L,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val hasAnyGoalThisMonth: StateFlow<Boolean> = rows
        .map { list -> list.any { it.hasGoal } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val previousMonthHasGoals: StateFlow<Boolean> = _yearMonth
        .flatMapLatest { ym ->
            val prev = ym.minusMonths(1)
            goalRepository.observeProgressForMonth(prev.toDbKey(), prev.atDay(1), prev.atEndOfMonth())
                .map { it.isNotEmpty() }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun prevMonth() = _yearMonth.update { it.minusMonths(1) }
    fun nextMonth() = _yearMonth.update { it.plusMonths(1) }

    fun setGoal(categoryId: Long, amountCents: Long) {
        viewModelScope.launch { goalRepository.setGoal(_yearMonth.value.toDbKey(), categoryId, amountCents) }
    }

    fun removeGoal(categoryId: Long) {
        viewModelScope.launch { goalRepository.removeGoal(_yearMonth.value.toDbKey(), categoryId) }
    }

    fun importFromPreviousMonth() {
        val current = _yearMonth.value
        viewModelScope.launch {
            goalRepository.importFromPreviousMonth(current.minusMonths(1).toDbKey(), current.toDbKey())
        }
    }
}
