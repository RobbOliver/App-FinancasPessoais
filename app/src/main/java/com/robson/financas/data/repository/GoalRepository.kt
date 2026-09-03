package com.robson.financas.data.repository

import com.robson.financas.data.local.dao.GoalDao
import com.robson.financas.data.local.entity.GoalCategoryCrossRef
import com.robson.financas.data.local.entity.GoalEntity
import com.robson.financas.data.local.relation.GoalCategoryDetail
import com.robson.financas.data.local.relation.GoalProgress
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoalRepository @Inject constructor(
    private val goalDao: GoalDao,
) {
    fun observeProgressForMonth(yearMonth: Int, startDate: LocalDate, endDate: LocalDate): Flow<List<GoalProgress>> =
        goalDao.observeProgressForMonth(yearMonth, startDate, endDate)

    fun observeGoal(goalId: Long): Flow<GoalEntity?> = goalDao.observeById(goalId)

    fun observeCategoryDetailsForGoal(
        goalId: Long,
        startDate: LocalDate,
        endDate: LocalDate,
    ): Flow<List<GoalCategoryDetail>> = goalDao.observeCategoryDetailsForGoal(goalId, startDate, endDate)

    suspend fun getCategoryIdsForGoal(goalId: Long): List<Long> = goalDao.getCategoryIdsForGoal(goalId)

    /** categoryId -> quanto (em centavos) dessa categoria está alocado no valor total da meta. */
    suspend fun getCategoryAllocationsForGoal(goalId: Long): Map<Long, Long> =
        goalDao.getCategoryAllocationsForGoal(goalId).associate { it.categoryId to it.allocatedCents }

    suspend fun createGoal(yearMonth: Int, name: String, amountCents: Long, categoryAllocations: Map<Long, Long>): Long {
        val goalId = goalDao.insert(GoalEntity(yearMonth = yearMonth, name = name, amountCents = amountCents))
        goalDao.insertCategoryCrossRefs(categoryAllocations.map { (categoryId, allocatedCents) ->
            GoalCategoryCrossRef(goalId, categoryId, allocatedCents)
        })
        return goalId
    }

    suspend fun updateGoal(goalId: Long, name: String, amountCents: Long, categoryAllocations: Map<Long, Long>) {
        val existing = goalDao.getById(goalId) ?: return
        goalDao.update(existing.copy(name = name, amountCents = amountCents))
        goalDao.clearCategoriesForGoal(goalId)
        goalDao.insertCategoryCrossRefs(categoryAllocations.map { (categoryId, allocatedCents) ->
            GoalCategoryCrossRef(goalId, categoryId, allocatedCents)
        })
    }

    suspend fun deleteGoal(goalId: Long) {
        val existing = goalDao.getById(goalId) ?: return
        goalDao.delete(existing)
    }

    fun observeAll(): Flow<List<GoalEntity>> = goalDao.observeAll()

    suspend fun hasGoalsForMonth(yearMonth: Int): Boolean = goalDao.getForMonth(yearMonth).isNotEmpty()

    suspend fun importFromPreviousMonth(fromYearMonth: Int, toYearMonth: Int) {
        goalDao.getForMonth(fromYearMonth).forEach { goal ->
            val allocations = goalDao.getCategoryAllocationsForGoal(goal.id)
            val newGoalId = goalDao.insert(goal.copy(id = 0, yearMonth = toYearMonth))
            goalDao.insertCategoryCrossRefs(
                allocations.map { GoalCategoryCrossRef(newGoalId, it.categoryId, it.allocatedCents) },
            )
        }
    }
}
