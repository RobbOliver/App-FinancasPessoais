package com.robson.financas.data.repository

import com.robson.financas.data.local.dao.GoalDao
import com.robson.financas.data.local.entity.GoalEntity
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

    suspend fun setGoal(yearMonth: Int, categoryId: Long, amountCents: Long) =
        goalDao.upsert(GoalEntity(yearMonth = yearMonth, categoryId = categoryId, amountCents = amountCents))

    suspend fun removeGoal(yearMonth: Int, categoryId: Long) =
        goalDao.deleteByCategoryAndMonth(categoryId, yearMonth)

    suspend fun hasGoalsForMonth(yearMonth: Int): Boolean = goalDao.getForMonth(yearMonth).isNotEmpty()

    suspend fun importFromPreviousMonth(fromYearMonth: Int, toYearMonth: Int) {
        goalDao.getForMonth(fromYearMonth).forEach { goal ->
            goalDao.upsert(goal.copy(yearMonth = toYearMonth))
        }
    }
}
