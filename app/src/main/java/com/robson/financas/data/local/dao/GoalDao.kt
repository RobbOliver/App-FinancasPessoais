package com.robson.financas.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.robson.financas.data.local.entity.GoalEntity
import com.robson.financas.data.local.relation.GoalProgress
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface GoalDao {
    @Upsert
    suspend fun upsert(goal: GoalEntity)

    @Query("DELETE FROM goals WHERE categoryId = :categoryId AND yearMonth = :yearMonth")
    suspend fun deleteByCategoryAndMonth(categoryId: Long, yearMonth: Int)

    @Query("SELECT * FROM goals WHERE yearMonth = :yearMonth")
    suspend fun getForMonth(yearMonth: Int): List<GoalEntity>

    @Query(
        """
        SELECT g.categoryId, g.amountCents,
            c.name AS categoryName, c.icon AS categoryIcon, c.colorHex AS categoryColorHex, c.parentCategoryId,
            COALESCE((SELECT SUM(t.amountCents) FROM transactions t
                      WHERE t.categoryId = g.categoryId AND t.type = 'EXPENSE'
                        AND t.date >= :startDate AND t.date <= :endDate
                        AND t.isPaid = 1 AND t.isIgnored = 0), 0) AS spentCents
        FROM goals g
        JOIN categories c ON c.id = g.categoryId
        WHERE g.yearMonth = :yearMonth
        ORDER BY c.parentCategoryId IS NOT NULL, c.name
        """,
    )
    fun observeProgressForMonth(yearMonth: Int, startDate: LocalDate, endDate: LocalDate): Flow<List<GoalProgress>>
}
