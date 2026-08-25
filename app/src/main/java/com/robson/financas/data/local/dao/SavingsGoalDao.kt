package com.robson.financas.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.robson.financas.data.local.entity.SavingsGoalContributionEntity
import com.robson.financas.data.local.entity.SavingsGoalEntity
import com.robson.financas.data.local.relation.SavingsGoalProgress
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingsGoalDao {
    @Insert
    suspend fun insert(goal: SavingsGoalEntity): Long

    @Update
    suspend fun update(goal: SavingsGoalEntity)

    @Delete
    suspend fun delete(goal: SavingsGoalEntity)

    @Query("SELECT * FROM savings_goals WHERE id = :id")
    suspend fun getById(id: Long): SavingsGoalEntity?

    @Query(
        """
        SELECT g.*, COALESCE(SUM(c.amountCents), 0) AS savedCents
        FROM savings_goals g
        LEFT JOIN savings_goal_contributions c ON c.goalId = g.id
        WHERE g.isArchived = 0
        GROUP BY g.id
        ORDER BY g.id ASC
        """,
    )
    fun observeAllWithProgress(): Flow<List<SavingsGoalProgress>>

    @Insert
    suspend fun insertContribution(contribution: SavingsGoalContributionEntity)

    @Delete
    suspend fun deleteContribution(contribution: SavingsGoalContributionEntity)

    @Query("SELECT * FROM savings_goal_contributions WHERE goalId = :goalId ORDER BY date DESC, id DESC")
    fun observeContributions(goalId: Long): Flow<List<SavingsGoalContributionEntity>>
}
