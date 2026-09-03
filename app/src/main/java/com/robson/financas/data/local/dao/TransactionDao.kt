package com.robson.financas.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.robson.financas.data.local.entity.TransactionEntity
import com.robson.financas.data.local.relation.CategoryExpenseSlice
import com.robson.financas.data.local.relation.MonthSummary
import com.robson.financas.data.local.relation.PendingSummary
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
        SELECT categoryId FROM transactions
        WHERE categoryId IS NOT NULL
          AND needsReview = 0
          AND counterpartyName IS NOT NULL
          AND LOWER(counterpartyName) = LOWER(:counterpartyName)
        ORDER BY date DESC, createdAt DESC
        LIMIT 1
        """,
    )
    suspend fun findRecentCategoryIdForCounterparty(counterpartyName: String): Long?

    @Query(
        """
        SELECT t.*,
            a.name AS accountName,
            a2.name AS transferToAccountName,
            c.name AS categoryName,
            c.icon AS categoryIcon,
            c.colorHex AS categoryColorHex,
            fd.id AS fiscalDocumentId
        FROM transactions t
        JOIN accounts a ON a.id = t.accountId
        LEFT JOIN accounts a2 ON a2.id = t.transferToAccountId
        LEFT JOIN categories c ON c.id = t.categoryId
        LEFT JOIN fiscal_documents fd ON fd.linkedTransactionId = t.id
        WHERE (:accountId IS NULL OR t.accountId = :accountId OR t.transferToAccountId = :accountId)
          AND (:categoryId IS NULL OR t.categoryId = :categoryId)
          AND (:startDate IS NULL OR t.date >= :startDate)
          AND (:endDate IS NULL OR t.date <= :endDate)
          AND (:onlyNeedsReview = 0 OR t.needsReview = 1)
          AND (:onlyScheduled = 0 OR t.isPaid = 0)
          AND (:onlyFavorite = 0 OR t.isFavorite = 1)
          AND (:excludeScheduled = 0 OR t.isPaid = 1)
          AND (:tagId IS NULL OR EXISTS (
              SELECT 1 FROM transaction_tag_cross_refs x WHERE x.transactionId = t.id AND x.tagId = :tagId
          ))
        ORDER BY t.date DESC, t.createdAt DESC
        """,
    )
    fun observeFiltered(
        accountId: Long?,
        categoryId: Long?,
        startDate: LocalDate?,
        endDate: LocalDate?,
        onlyNeedsReview: Boolean = false,
        onlyScheduled: Boolean = false,
        onlyFavorite: Boolean = false,
        tagId: Long? = null,
        excludeScheduled: Boolean = false,
    ): Flow<List<TransactionWithDetails>>

    @Query(
        """
        SELECT t.*,
            a.name AS accountName,
            a2.name AS transferToAccountName,
            c.name AS categoryName,
            c.icon AS categoryIcon,
            c.colorHex AS categoryColorHex,
            fd.id AS fiscalDocumentId
        FROM transactions t
        JOIN accounts a ON a.id = t.accountId
        LEFT JOIN accounts a2 ON a2.id = t.transferToAccountId
        LEFT JOIN categories c ON c.id = t.categoryId
        LEFT JOIN fiscal_documents fd ON fd.linkedTransactionId = t.id
        WHERE t.isPaid = 0 AND t.isIgnored = 0
        ORDER BY t.date ASC, t.createdAt DESC
        """,
    )
    fun observeScheduled(): Flow<List<TransactionWithDetails>>

    @Query(
        """
        SELECT t.*,
            a.name AS accountName,
            a2.name AS transferToAccountName,
            c.name AS categoryName,
            c.icon AS categoryIcon,
            c.colorHex AS categoryColorHex,
            fd.id AS fiscalDocumentId
        FROM transactions t
        JOIN accounts a ON a.id = t.accountId
        LEFT JOIN accounts a2 ON a2.id = t.transferToAccountId
        LEFT JOIN categories c ON c.id = t.categoryId
        LEFT JOIN fiscal_documents fd ON fd.linkedTransactionId = t.id
        ORDER BY t.date DESC, t.createdAt DESC
        LIMIT :limit
        """,
    )
    fun observeRecent(limit: Int): Flow<List<TransactionWithDetails>>

    @Query(
        """
        SELECT t.*,
            a.name AS accountName,
            a2.name AS transferToAccountName,
            c.name AS categoryName,
            c.icon AS categoryIcon,
            c.colorHex AS categoryColorHex,
            fd.id AS fiscalDocumentId
        FROM transactions t
        JOIN accounts a ON a.id = t.accountId
        LEFT JOIN accounts a2 ON a2.id = t.transferToAccountId
        LEFT JOIN categories c ON c.id = t.categoryId
        LEFT JOIN fiscal_documents fd ON fd.linkedTransactionId = t.id
        WHERE t.id = :id
        """,
    )
    fun observeByIdWithDetails(id: Long): Flow<TransactionWithDetails?>

    @Query(
        """
        SELECT
            COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amountCents ELSE 0 END), 0) AS incomeCents,
            COALESCE(SUM(CASE WHEN type = 'EXPENSE' THEN amountCents ELSE 0 END), 0) AS expenseCents
        FROM transactions
        WHERE date >= :start AND date <= :end AND isPaid = 1 AND isIgnored = 0
        """,
    )
    fun observeMonthSummary(start: LocalDate, end: LocalDate): Flow<MonthSummary>

    @Query(
        """
        SELECT c.id AS categoryId, c.name AS categoryName, c.colorHex AS categoryColorHex,
            SUM(t.amountCents) AS totalCents
        FROM transactions t
        JOIN categories c ON c.id = t.categoryId
        WHERE t.type = 'EXPENSE' AND t.date >= :start AND t.date <= :end
          AND t.isPaid = 1 AND t.isIgnored = 0
        GROUP BY c.id
        ORDER BY totalCents DESC
        """,
    )
    fun observeExpenseByCategoryForMonth(start: LocalDate, end: LocalDate): Flow<List<CategoryExpenseSlice>>

    @Query(
        """
        SELECT
            COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amountCents ELSE 0 END), 0) AS pendingIncomeCents,
            COALESCE(SUM(CASE WHEN type = 'EXPENSE' THEN amountCents ELSE 0 END), 0) AS pendingExpenseCents
        FROM transactions
        WHERE date >= :start AND date <= :end AND isPaid = 0 AND isIgnored = 0
        """,
    )
    fun observePendingSummary(start: LocalDate, end: LocalDate): Flow<PendingSummary>
}
