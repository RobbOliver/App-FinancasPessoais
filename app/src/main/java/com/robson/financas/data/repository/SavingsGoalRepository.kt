package com.robson.financas.data.repository

import com.robson.financas.data.local.dao.SavingsGoalDao
import com.robson.financas.data.local.entity.SavingsGoalContributionEntity
import com.robson.financas.data.local.entity.SavingsGoalEntity
import com.robson.financas.data.local.relation.SavingsGoalProgress
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SavingsGoalRepository @Inject constructor(
    private val savingsGoalDao: SavingsGoalDao,
) {
    fun observeAllWithProgress(): Flow<List<SavingsGoalProgress>> = savingsGoalDao.observeAllWithProgress()

    suspend fun getById(id: Long): SavingsGoalEntity? = savingsGoalDao.getById(id)

    suspend fun create(goal: SavingsGoalEntity): Long = savingsGoalDao.insert(goal)

    suspend fun update(goal: SavingsGoalEntity) = savingsGoalDao.update(goal)

    suspend fun delete(goal: SavingsGoalEntity) = savingsGoalDao.delete(goal)

    fun observeContributions(goalId: Long): Flow<List<SavingsGoalContributionEntity>> =
        savingsGoalDao.observeContributions(goalId)

    suspend fun addContribution(contribution: SavingsGoalContributionEntity) =
        savingsGoalDao.insertContribution(contribution)

    suspend fun deleteContribution(contribution: SavingsGoalContributionEntity) =
        savingsGoalDao.deleteContribution(contribution)
}
