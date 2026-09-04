package com.robson.financas.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.robson.financas.data.local.entity.TransactionRecurrence
import com.robson.financas.data.local.entity.TransactionType
import com.robson.financas.data.local.relation.TransactionWithDetails
import com.robson.financas.data.repository.AccountRepository
import com.robson.financas.data.repository.CategoryRepository
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
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    private val _selectedMonth = MutableStateFlow(YearMonth.now())

    private val currentBalance = accountRepository.observeActiveAccountsWithBalance()
        .map { accounts -> accounts.sumOf { it.balanceCents } }

    private val goalsAndCrossRefs = combine(
        goalRepository.observeAll(),
        goalRepository.observeAllCategoryCrossRefs(),
    ) { goals, crossRefs -> goals to crossRefs }

    val uiState: StateFlow<ReportsUiState> = combine(
        _selectedMonth,
        currentBalance,
        transactionRepository.observeAllRecurring(),
        goalsAndCrossRefs,
        categoryRepository.observeAll(),
    ) { selectedMonth, balance, recurring, goalsWithCrossRefs, allCategories ->
        val (allGoals, allCrossRefs) = goalsWithCrossRefs

        // categoryId -> parentCategoryId, usado pra "cascatear": se a meta selecionou a
        // categoria-pai (ex.: Moradia) mas o agendamento está numa subcategoria (ex.: Aluguel),
        // o agendamento ainda precisa ser comparado contra a fatia da meta da categoria-pai.
        val parentOf: Map<Long, Long?> = allCategories.associate { it.id to it.parentCategoryId }

        // Um mês pode ter várias metas separadas (uma por categoria, por ex.) — soma a alocação
        // de todas elas por categoria em vez de pegar só uma meta.
        val goalIdToMonth: Map<Long, Int> = allGoals.associate { it.id to it.yearMonth }
        val allocationByMonth: MutableMap<Int, MutableMap<Long, Long>> = mutableMapOf()
        allCrossRefs.forEach { crossRef ->
            val month = goalIdToMonth[crossRef.goalId] ?: return@forEach
            val catMap = allocationByMonth.getOrPut(month) { mutableMapOf() }
            catMap[crossRef.categoryId] = (catMap[crossRef.categoryId] ?: 0L) + crossRef.allocatedCents
        }
        val lastGoalMonthKey = allocationByMonth.keys.maxOrNull()

        val currentMonth = YearMonth.now()

        // 12-month projection list (always from current month)
        var cumBalance = balance
        val projections = (0..11).map { offset ->
            val month = currentMonth.plusMonths(offset.toLong())
            val (income, expense, status) = computeMonthTotals(month, recurring, allocationByMonth, parentOf, lastGoalMonthKey)
            cumBalance += income - expense
            MonthProjection(month, income, expense, cumBalance, status)
        }

        // Chart data for selected month
        val chartDays = computeChartDays(selectedMonth, recurring, allocationByMonth, parentOf, lastGoalMonthKey)

        ReportsUiState(
            selectedMonth = selectedMonth,
            chartDays = chartDays,
            projections = projections,
            currentBalanceCents = balance,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportsUiState())

    fun previousMonth() = _selectedMonth.update { it.minusMonths(1) }
    fun nextMonth() = _selectedMonth.update { it.plusMonths(1) }

    /**
     * Atribui uma transação recorrente à categoria da meta que ela representa: por categoria
     * exata primeiro, senão pela categoria-pai (cascata de 1 nível, mesmo padrão usado no
     * cálculo de gasto real das metas). Retorna null se a categoria não está coberta por
     * nenhuma meta — nesse caso a transação conta à parte, fora do cálculo de metas.
     */
    private fun attributeToGoalCategory(categoryId: Long?, allocation: Map<Long, Long>, parentOf: Map<Long, Long?>): Long? {
        if (categoryId == null) return null
        if (allocation.containsKey(categoryId)) return categoryId
        val parentId = parentOf[categoryId] ?: return null
        return if (allocation.containsKey(parentId)) parentId else null
    }

    private fun computeMonthTotals(
        month: YearMonth,
        recurring: List<TransactionWithDetails>,
        allocationByMonth: Map<Int, Map<Long, Long>>,
        parentOf: Map<Long, Long?>,
        lastGoalMonthKey: Int?,
    ): Triple<Long, Long, GoalStatus> {
        val monthStart = month.atDay(1)
        val monthEnd = month.atEndOfMonth()
        val monthKey = month.year * 100 + month.monthValue

        val (allocation, status) = when {
            allocationByMonth[monthKey] != null -> Pair(allocationByMonth.getValue(monthKey), GoalStatus.REAL)
            lastGoalMonthKey != null -> Pair(allocationByMonth[lastGoalMonthKey].orEmpty(), GoalStatus.SIMULATED)
            else -> Pair(emptyMap(), GoalStatus.NONE)
        }

        var projIncome = 0L
        var nonGoalExpense = 0L
        val scheduledByCategory = mutableMapOf<Long, Long>()

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
            if (dates.isEmpty()) return@forEach

            val total = dates.size.toLong() * t.amountCents
            when (t.type) {
                TransactionType.INCOME -> projIncome += total
                TransactionType.EXPENSE -> {
                    val attributedTo = attributeToGoalCategory(t.categoryId, allocation, parentOf)
                    if (attributedTo != null) {
                        scheduledByCategory[attributedTo] = (scheduledByCategory[attributedTo] ?: 0L) + total
                    } else {
                        nonGoalExpense += total
                    }
                }
                else -> {}
            }
        }

        // Por categoria da meta, o que prevalece é o maior entre o valor planejado e o que já
        // está agendado de fato — um agendamento maior que a meta não pode ser "anulado" por
        // ela (o compromisso real vai acontecer de qualquer forma), e uma meta maior que o
        // agendamento mantém a folga para o restante da categoria.
        val goalDrivenExpense = allocation.entries.sumOf { (categoryId, allocatedCents) ->
            maxOf(allocatedCents, scheduledByCategory[categoryId] ?: 0L)
        }

        return Triple(projIncome, goalDrivenExpense + nonGoalExpense, status)
    }

    private fun computeChartDays(
        selectedMonth: YearMonth,
        recurring: List<TransactionWithDetails>,
        allocationByMonth: Map<Int, Map<Long, Long>>,
        parentOf: Map<Long, Long?>,
        lastGoalMonthKey: Int?,
    ): List<DailyPoint> {
        val monthStart = selectedMonth.atDay(1)
        val monthEnd = selectedMonth.atEndOfMonth()
        val daysInMonth = monthEnd.dayOfMonth
        val monthKey = selectedMonth.year * 100 + selectedMonth.monthValue

        val allocation: Map<Long, Long> = when {
            allocationByMonth[monthKey] != null -> allocationByMonth.getValue(monthKey)
            lastGoalMonthKey != null -> allocationByMonth[lastGoalMonthKey].orEmpty()
            else -> emptyMap()
        }

        val dailyIncome = LongArray(daysInMonth + 1)
        val dailyExpense = LongArray(daysInMonth + 1)
        val scheduledByCategory = mutableMapOf<Long, Long>()

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
                    TransactionType.EXPENSE -> {
                        // Todo agendamento real aparece no dia dele, esteja ou não coberto por meta.
                        dailyExpense[day] += t.amountCents
                        val attributedTo = attributeToGoalCategory(t.categoryId, allocation, parentOf)
                        if (attributedTo != null) {
                            scheduledByCategory[attributedTo] = (scheduledByCategory[attributedTo] ?: 0L) + t.amountCents
                        }
                    }
                    else -> {}
                }
            }
        }

        // Folga de cada categoria da meta (o que passa do agendado) — distribuída igualmente
        // pelos dias do mês, já que não tem uma data específica ainda.
        val totalSlack = allocation.entries.sumOf { (categoryId, allocatedCents) ->
            (allocatedCents - (scheduledByCategory[categoryId] ?: 0L)).coerceAtLeast(0L)
        }
        val dailySlack = totalSlack / daysInMonth
        val slackRemainder = totalSlack % daysInMonth

        var cumIncome = 0L
        var cumExpense = 0L
        return (1..daysInMonth).map { day ->
            cumIncome += dailyIncome[day]
            cumExpense += dailyExpense[day] + dailySlack + (if (day == 1) slackRemainder else 0L)
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
