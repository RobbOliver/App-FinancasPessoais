package com.robson.financas.data.repository

import com.robson.financas.data.local.dao.TransactionDao
import com.robson.financas.data.local.entity.TransactionEntity
import com.robson.financas.data.local.relation.MonthSummary
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
    ): Flow<List<TransactionWithDetails>> =
        transactionDao.observeFiltered(accountId, categoryId, startDate, endDate, onlyNeedsReview)

    fun observeRecent(limit: Int): Flow<List<TransactionWithDetails>> = transactionDao.observeRecent(limit)

    fun observeMonthSummary(start: LocalDate, end: LocalDate): Flow<MonthSummary> =
        transactionDao.observeMonthSummary(start, end)
}
