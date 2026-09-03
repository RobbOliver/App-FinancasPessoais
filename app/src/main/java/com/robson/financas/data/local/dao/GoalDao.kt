package com.robson.financas.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.robson.financas.data.local.entity.GoalCategoryCrossRef
import com.robson.financas.data.local.entity.GoalEntity
import com.robson.financas.data.local.relation.GoalCategoryAllocation
import com.robson.financas.data.local.relation.GoalCategoryDetail
import com.robson.financas.data.local.relation.GoalProgress
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface GoalDao {
    @Insert
    suspend fun insert(goal: GoalEntity): Long

    @Update
    suspend fun update(goal: GoalEntity)

    @Delete
    suspend fun delete(goal: GoalEntity)

    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getById(id: Long): GoalEntity?

    @Query("SELECT * FROM goals WHERE id = :id")
    fun observeById(id: Long): Flow<GoalEntity?>

    @Query("SELECT * FROM goals WHERE yearMonth = :yearMonth")
    suspend fun getForMonth(yearMonth: Int): List<GoalEntity>

    @Query("SELECT categoryId FROM goal_categories WHERE goalId = :goalId")
    suspend fun getCategoryIdsForGoal(goalId: Long): List<Long>

    @Query("SELECT categoryId, allocatedCents FROM goal_categories WHERE goalId = :goalId")
    suspend fun getCategoryAllocationsForGoal(goalId: Long): List<GoalCategoryAllocation>

    @Query("DELETE FROM goal_categories WHERE goalId = :goalId")
    suspend fun clearCategoriesForGoal(goalId: Long)

    @Insert
    suspend fun insertCategoryCrossRefs(crossRefs: List<GoalCategoryCrossRef>)

    @Query(
        """
        SELECT g.id AS goalId, g.name AS name, g.amountCents AS amountCents,
            (SELECT GROUP_CONCAT(c.name, ', ') FROM categories c
             JOIN goal_categories gc ON gc.categoryId = c.id WHERE gc.goalId = g.id) AS categoryNames,
            COALESCE((SELECT SUM(t.amountCents) FROM transactions t
                      WHERE t.categoryId IN (SELECT categoryId FROM goal_categories WHERE goalId = g.id)
                        AND t.type = 'EXPENSE' AND t.date >= :startDate AND t.date <= :endDate
                        AND t.isPaid = 1 AND t.isIgnored = 0), 0) AS spentCents
        FROM goals g
        WHERE g.yearMonth = :yearMonth
        ORDER BY g.name
        """,
    )
    fun observeProgressForMonth(yearMonth: Int, startDate: LocalDate, endDate: LocalDate): Flow<List<GoalProgress>>

    @Query(
        """
        SELECT c.id AS categoryId, c.name AS categoryName, gc.allocatedCents AS allocatedCents,
            COALESCE((SELECT SUM(t.amountCents) FROM transactions t
                      WHERE t.categoryId = c.id AND t.type = 'EXPENSE' AND t.date >= :startDate AND t.date <= :endDate
                        AND t.isPaid = 1 AND t.isIgnored = 0), 0) AS spentCents
        FROM goal_categories gc
        JOIN categories c ON c.id = gc.categoryId
        WHERE gc.goalId = :goalId
        ORDER BY c.name
        """,
    )
    fun observeCategoryDetailsForGoal(
        goalId: Long,
        startDate: LocalDate,
        endDate: LocalDate,
    ): Flow<List<GoalCategoryDetail>>

    @Query("SELECT * FROM goals ORDER BY yearMonth DESC")
    fun observeAll(): Flow<List<GoalEntity>>
}
