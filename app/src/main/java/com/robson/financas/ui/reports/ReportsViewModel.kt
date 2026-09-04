package com.robson.financas.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.robson.financas.data.local.entity.TransactionRecurrence
import com.robson.financas.data.local.entity.TransactionType
import com.robson.financas.data.local.relation.TransactionWithDetails
import com.robson.financas.data.repository.AccountRepository
import com.robson.financas.data.repository.GoalRepository
import com.robson.financas.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import javax.inject.Inject

enum class GoalStatus { REAL, SIMULATED, NONE }

data class MonthProjection(
    val yearMonth: YearMonth,
    val projectedIncomeCents: Long,
    val projectedExpenseCents: Long,
    val cumulativeBalanceCents: Long,
    val goalStatus: GoalStatus,
) {
    val resultCents: Long get() = projectedIncomeCents - projectedExpenseCents
}

data class DailyPoint(
    val day: Int,
    val cumulativeIncome: Long,
    val cumulativeExpense: Long,
) {
    val balance: Long get() = cumulativeIncome - cumulativeExpense
}

data class ReportsUiState(
    val selectedMonth: YearMonth = YearMonth.now(),
    val chartDays: List<DailyPoint> = emptyList(),
    val projections: List<MonthProjection> = emptyList(),
    val currentBalanceCents: Long = 0L,
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val goalRepository: GoalRepository,
) : ViewModel() {

    private val _selectedMonth = MutableStateFlow(YearMonth.now())

    private val currentBalance = accountRepository.observeActiveAccountsWithBalance()
        .map { accounts -> accounts.sumOf { it.balanceCents } }

    val uiState: StateFlow<ReportsUiState> = combine(
        _selectedMonth,
        currentBalance,
        transactionRepository.observeAllRecurring(),
        goalRepository.observeAll(),
        goalRepository.observeAllCategoryCrossRefs(),
    ) { selectedMonth, balance, recurring, allGoals, allCrossRefs ->
        // Um mês pode ter várias metas separadas (uma por categoria, por ex.) — soma todas em
        // vez de pegar só uma, senão a despesa prevista fica muito abaixo do que foi planejado.
        val goalTotalsByMonth: Map<Int, Long> = allGoals
            .groupBy { it.yearMonth }
            .mapValues { (_, goals) -> goals.sumOf { it.amountCents } }

        // categoryId das metas de cada mês — usado pra não somar duas vezes uma transação
        // recorrente cuja categoria já está embutida no valor da meta (ex.: aluguel agendado +
        // meta "Essenciais" que também cobre a categoria Moradia).
        val goalIdsByMonth: Map<Int, List<Long>> = allGoals.groupBy({ it.yearMonth }, { it.id })
        val categoryIdsByGoalId: Map<Long, List<Long>> = allCrossRefs.groupBy({ it.goalId }, { it.categoryId })
        val goalCategoryIdsByMonth: Map<Int, Set<Long>> = goalIdsByMonth.mapValues { (_, goalIds) ->
            goalIds.flatMap { categoryIdsByGoalId[it].orEmpty() }.toSet()
        }

        val lastGoalMonthKey = goalTotalsByMonth.keys.maxOrNull()
        val lastGoalTotalCents = lastGoalMonthKey?.let { goalTotalsByMonth.getValue(it) } ?: 0L
        val hasLastGoal = lastGoalMonthKey != null
        val currentMonth = YearMonth.now()

        // 12-month projection list (always from current month)
        var cumBalance = balance
        val projections = (0..11).map { offset ->
            val month = currentMonth.plusMonths(offset.toLong())
            val (income, expense, status) = computeMonthTotals(
                month, recurring, goalTotalsByMonth, goalCategoryIdsByMonth,
                lastGoalMonthKey, lastGoalTotalCents, hasLastGoal,
            )
            cumBalance += income - expense
            MonthProjection(month, income, expense, cumBalance, status)
        }

        // Chart data for selected month
        val chartDays = computeChartDays(
            selectedMonth, recurring, goalTotalsByMonth, goalCategoryIdsByMonth,
            lastGoalMonthKey, lastGoalTotalCents, hasLastGoal,
        )

        ReportsUiState(
            selectedMonth = selectedMonth,
            chartDays = chartDays,
            projections = projections,
            currentBalanceCents = balance,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportsUiState())

    fun previousMonth() = _selectedMonth.update { it.minusMonths(1) }
    fun nextMonth() = _selectedMonth.update { it.plusMonths(1) }

    private fun computeMonthTotals(
        month: YearMonth,
        recurring: List<TransactionWithDetails>,
        goalTotalsByMonth: Map<Int, Long>,
        goalCategoryIdsByMonth: Map<Int, Set<Long>>,
        lastGoalMonthKey: Int?,
        lastGoalTotalCents: Long,
        hasLastGoal: Boolean,
    ): Triple<Long, Long, GoalStatus> {
        val monthStart = month.atDay(1)
        val monthEnd = month.atEndOfMonth()
        val monthKey = month.year * 100 + month.monthValue

        val (goalAmount, status, excludedCategoryIds) = when {
            goalTotalsByMonth[monthKey] != null ->
                Triple(goalTotalsByMonth.getValue(monthKey), GoalStatus.REAL, goalCategoryIdsByMonth[monthKey].orEmpty())
            hasLastGoal ->
                Triple(lastGoalTotalCents, GoalStatus.SIMULATED, goalCategoryIdsByMonth[lastGoalMonthKey].orEmpty())
            else ->
                Triple(0L, GoalStatus.NONE, emptySet())
        }

        var projIncome = 0L
        var projExpense = 0L

        recurring.forEach { item ->
            val t = item.transaction
            val freq = t.recurrenceFrequency ?: return@forEach
            val originalMonth = YearMonth.from(t.date)

            val dates = when {
                originalMonth == month -> listOf(t.date)
                month.isAfter(originalMonth) -> {
                    val endDate = t.recurrenceEndDate
                    projectOccurrences(t.date, freq, monthStart, monthEnd)
                        .filter { d -> endDate == null || !d.isAfter(endDate) }
                }
                else -> emptyList()
            }

            val total = dates.size.toLong() * t.amountCents
            when (t.type) {
                TransactionType.INCOME -> projIncome += total
                TransactionType.EXPENSE ->
                    if (t.categoryId == null || t.categoryId !in excludedCategoryIds) projExpense += total
                else -> {}
            }
        }

        projExpense += goalAmount

        return Triple(projIncome, projExpense, status)
    }

    private fun computeChartDays(
        selectedMonth: YearMonth,
        recurring: List<TransactionWithDetails>,
        goalTotalsByMonth: Map<Int, Long>,
        goalCategoryIdsByMonth: Map<Int, Set<Long>>,
        lastGoalMonthKey: Int?,
        lastGoalTotalCents: Long,
        hasLastGoal: Boolean,
    ): List<DailyPoint> {
        val monthStart = selectedMonth.atDay(1)
        val monthEnd = selectedMonth.atEndOfMonth()
        val daysInMonth = monthEnd.dayOfMonth
        val monthKey = selectedMonth.year * 100 + selectedMonth.monthValue

        val (goalAmount, excludedCategoryIds) = when {
            goalTotalsByMonth[monthKey] != null ->
                Pair(goalTotalsByMonth.getValue(monthKey), goalCategoryIdsByMonth[monthKey].orEmpty())
            hasLastGoal ->
                Pair(lastGoalTotalCents, goalCategoryIdsByMonth[lastGoalMonthKey].orEmpty())
            else ->
                Pair(0L, emptySet())
        }

        val dailyIncome = LongArray(daysInMonth + 1)
        val dailyExpense = LongArray(daysInMonth + 1)

        recurring.forEach { item ->
            val t = item.transaction
            val freq = t.recurrenceFrequency ?: return@forEach
            val originalMonth = YearMonth.from(t.date)

            val dates = when {
                originalMonth == selectedMonth -> listOf(t.date)
                selectedMonth.isAfter(originalMonth) -> {
                    val endDate = t.recurrenceEndDate
                    projectOccurrences(t.date, freq, monthStart, monthEnd)
                        .filter { d -> endDate == null || !d.isAfter(endDate) }
                }
                else -> emptyList()
            }

            dates.forEach { date ->
                val day = date.dayOfMonth
                when (t.type) {
                    TransactionType.INCOME -> dailyIncome[day] += t.amountCents
                    TransactionType.EXPENSE ->
                        if (t.categoryId == null || t.categoryId !in excludedCategoryIds) dailyExpense[day] += t.amountCents
                    else -> {}
                }
            }
        }

        // Spread meta expense evenly across days
        val dailyMeta = goalAmount / daysInMonth
        val metaRemainder = goalAmount % daysInMonth

        var cumIncome = 0L
        var cumExpense = 0L
        return (1..daysInMonth).map { day ->
            cumIncome += dailyIncome[day]
            cumExpense += dailyExpense[day] + dailyMeta + (if (day == 1) metaRemainder else 0L)
            DailyPoint(day, cumIncome, cumExpense)
        }
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
