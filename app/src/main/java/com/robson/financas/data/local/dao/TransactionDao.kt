package com.robson.financas.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.robson.financas.data.local.entity.TransactionEntity
import com.robson.financas.data.local.relation.MonthSummary
import com.robson.financas.data.local.relation.TransactionWithDetails
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface TransactionDao {
    @Insert
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): TransactionEntity?

    @Query(
        """
        SELECT t.*,
            a.name AS accountName,
            a2.name AS transferToAccountName,
            c.name AS categoryName,
            c.icon AS categoryIcon,
            c.colorHex AS categoryColorHex
        FROM transactions t
        JOIN accounts a ON a.id = t.accountId
        LEFT JOIN accounts a2 ON a2.id = t.transferToAccountId
        LEFT JOIN categories c ON c.id = t.categoryId
        WHERE (:accountId IS NULL OR t.accountId = :accountId OR t.transferToAccountId = :accountId)
          AND (:categoryId IS NULL OR t.categoryId = :categoryId)
          AND (:startDate IS NULL OR t.date >= :startDate)
          AND (:endDate IS NULL OR t.date <= :endDate)
        ORDER BY t.date DESC, t.createdAt DESC
        """,
    )
    fun observeFiltered(
        accountId: Long?,
        categoryId: Long?,
        startDate: LocalDate?,
        endDate: LocalDate?,
    ): Flow<List<TransactionWithDetails>>

    @Query(
        """
        SELECT t.*,
            a.name AS accountName,
            a2.name AS transferToAccountName,
            c.name AS categoryName,
            c.icon AS categoryIcon,
            c.colorHex AS categoryColorHex
        FROM transactions t
        JOIN accounts a ON a.id = t.accountId
        LEFT JOIN accounts a2 ON a2.id = t.transferToAccountId
        LEFT JOIN categories c ON c.id = t.categoryId
        ORDER BY t.date DESC, t.createdAt DESC
        LIMIT :limit
        """,
    )
    fun observeRecent(limit: Int): Flow<List<TransactionWithDetails>>

    @Query(
        """
        SELECT
            COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amountCents ELSE 0 END), 0) AS incomeCents,
            COALESCE(SUM(CASE WHEN type = 'EXPENSE' THEN amountCents ELSE 0 END), 0) AS expenseCents
        FROM transactions
        WHERE date >= :start AND date <= :end
        """,
    )
    fun observeMonthSummary(start: LocalDate, end: LocalDate): Flow<MonthSummary>
}
