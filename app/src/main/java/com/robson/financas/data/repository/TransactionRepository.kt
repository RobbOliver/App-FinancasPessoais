package com.robson.financas.data.repository

import com.robson.financas.data.local.dao.TransactionDao
import com.robson.financas.data.local.entity.TransactionEntity
import com.robson.financas.data.local.relation.CategoryExpenseSlice
import com.robson.financas.data.local.relation.MonthSummary
import com.robson.financas.data.local.relation.PendingSummary
import com.robson.financas.data.local.relation.TransactionWithDetails
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao,
) {
    suspend fun getById(id: Long): TransactionEntity? = transactionDao.getById(id)

    suspend fun create(transaction: TransactionEntity): Long = transactionDao.insert(transaction)

    suspend fun update(transaction: TransactionEntity) = transactionDao.update(transaction)

    suspend fun delete(transaction: TransactionEntity) = transactionDao.delete(transaction)

    fun observeFiltered(
        accountId: Long? = null,
        categoryId: Long? = null,
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
        onlyNeedsReview: Boolean = false,
        onlyScheduled: Boolean = false,
        onlyFavorite: Boolean = false,
        tagId: Long? = null,
    ): Flow<List<TransactionWithDetails>> =
        transactionDao.observeFiltered(
            accountId,
            categoryId,
            startDate,
            endDate,
            onlyNeedsReview,
            onlyScheduled,
            onlyFavorite,
            tagId,
        )

    fun observeRecent(limit: Int): Flow<List<TransactionWithDetails>> = transactionDao.observeRecent(limit)

    fun observeByIdWithDetails(id: Long): Flow<TransactionWithDetails?> = transactionDao.observeByIdWithDetails(id)

    fun observeMonthSummary(start: LocalDate, end: LocalDate): Flow<MonthSummary> =
        transactionDao.observeMonthSummary(start, end)

    fun observeExpenseByCategoryForMonth(start: LocalDate, end: LocalDate): Flow<List<CategoryExpenseSlice>> =
        transactionDao.observeExpenseByCategoryForMonth(start, end)

    fun observePendingSummary(start: LocalDate, end: LocalDate): Flow<PendingSummary> =
        transactionDao.observePendingSummary(start, end)
}
