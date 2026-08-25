package com.robson.financas.data.repository

import com.robson.financas.data.local.dao.CreditCardDao
import com.robson.financas.data.local.entity.CreditCardEntity
import com.robson.financas.data.local.entity.CreditCardInvoiceEntity
import com.robson.financas.data.local.entity.CreditCardPurchaseEntity
import com.robson.financas.data.local.entity.TransactionEntity
import com.robson.financas.data.local.entity.TransactionSource
import com.robson.financas.data.local.entity.TransactionType
import com.robson.financas.data.local.relation.CreditCardPurchaseWithCategory
import com.robson.financas.data.local.relation.CreditCardSummary
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreditCardRepository @Inject constructor(
    private val creditCardDao: CreditCardDao,
    private val transactionRepository: TransactionRepository,
) {
    fun observeAll(): Flow<List<CreditCardEntity>> = creditCardDao.observeAll()

    suspend fun getById(id: Long): CreditCardEntity? = creditCardDao.getById(id)

    suspend fun create(card: CreditCardEntity): Long = creditCardDao.insert(card)

    suspend fun update(card: CreditCardEntity) = creditCardDao.update(card)

    suspend fun delete(card: CreditCardEntity) = creditCardDao.delete(card)

    fun observeCardsWithInvoiceSummary(yearMonth: Int): Flow<List<CreditCardSummary>> =
        creditCardDao.observeCardsWithInvoiceSummary(yearMonth)

    fun observePurchasesForInvoice(creditCardId: Long, yearMonth: Int): Flow<List<CreditCardPurchaseWithCategory>> =
        creditCardDao.observePurchasesForInvoice(creditCardId, yearMonth)

    suspend fun deletePurchase(purchase: CreditCardPurchaseEntity) = creditCardDao.deletePurchase(purchase)

    suspend fun addPurchase(
        creditCardId: Long,
        categoryId: Long?,
        description: String,
        totalAmountCents: Long,
        purchaseDate: LocalDate,
        closingDay: Int,
        installments: Int,
    ) {
        val groupId = java.util.UUID.randomUUID().toString()
        val baseYearMonth = invoiceYearMonthFor(purchaseDate, closingDay)
        val perInstallmentCents = totalAmountCents / installments
        val remainder = totalAmountCents - perInstallmentCents * installments

        val purchases = (0 until installments).map { index ->
            val ym = addMonths(baseYearMonth, index)
            CreditCardPurchaseEntity(
                creditCardId = creditCardId,
                categoryId = categoryId,
                description = description,
                amountCents = perInstallmentCents + if (index == installments - 1) remainder else 0,
                purchaseDate = purchaseDate,
                invoiceYearMonth = ym,
                installmentGroupId = groupId,
                installmentNumber = index + 1,
                installmentTotal = installments,
            )
        }
        creditCardDao.insertPurchases(purchases)
    }

    suspend fun payInvoice(card: CreditCardEntity, yearMonth: Int, totalCents: Long, date: LocalDate) {
        val transactionId = transactionRepository.create(
            TransactionEntity(
                type = TransactionType.EXPENSE,
                amountCents = totalCents,
                accountId = card.paymentAccountId,
                categoryId = null,
                date = date,
                description = "Fatura ${card.name}",
                source = TransactionSource.MANUAL,
            ),
        )
        creditCardDao.upsertInvoice(
            CreditCardInvoiceEntity(
                creditCardId = card.id,
                yearMonth = yearMonth,
                isPaid = true,
                paidTransactionId = transactionId,
            ),
        )
    }

    companion object {
        fun invoiceYearMonthFor(purchaseDate: LocalDate, closingDay: Int): Int {
            val base = purchaseDate.year * 100 + purchaseDate.monthValue
            return if (purchaseDate.dayOfMonth > closingDay) addMonths(base, 1) else base
        }

        fun addMonths(yearMonth: Int, months: Int): Int {
            val year = yearMonth / 100
            val month = yearMonth % 100
            val total = year * 12 + (month - 1) + months
            return (total / 12) * 100 + (total % 12 + 1)
        }
    }
}
