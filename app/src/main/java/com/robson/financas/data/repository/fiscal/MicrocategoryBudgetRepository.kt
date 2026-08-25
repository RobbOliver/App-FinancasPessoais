package com.robson.financas.data.repository.fiscal

import com.robson.financas.data.local.dao.fiscal.MicrocategoryBudgetDao
import com.robson.financas.data.local.entity.fiscal.MicrocategoryBudgetEntity
import com.robson.financas.domain.fiscal.budget.BudgetCalculator
import com.robson.financas.domain.fiscal.budget.BudgetStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MicrocategoryBudgetRepository @Inject constructor(
    private val budgetDao: MicrocategoryBudgetDao,
) {
    suspend fun setBudget(microcategoryId: Long, yearMonth: Int, limitCents: Long) {
        budgetDao.upsert(MicrocategoryBudgetEntity(microcategoryId = microcategoryId, yearMonth = yearMonth, limitCents = limitCents))
    }

    /** Um par (microcategoryId -> status) por microcategoria orçada no mês. */
    fun observeStatuses(yearMonth: Int): Flow<List<Pair<Long, BudgetStatus>>> {
        val today = LocalDate.now()
        val monthStart = today.withDayOfMonth(1)
        val monthEnd = today.withDayOfMonth(today.lengthOfMonth())

        return budgetDao.observeForMonth(yearMonth).map { budgets ->
            budgets.map { budget ->
                val spent = budgetDao.observeSpentBetween(budget.microcategoryId, monthStart, monthEnd).first()
                budget.microcategoryId to BudgetCalculator.evaluate(
                    limitCents = budget.limitCents,
                    spentCents = spent,
                    dayOfMonth = today.dayOfMonth,
                    daysInMonth = today.lengthOfMonth(),
                )
            }
        }
    }
}
