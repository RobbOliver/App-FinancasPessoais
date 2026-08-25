package com.robson.financas.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.robson.financas.data.local.entity.CreditCardEntity
import com.robson.financas.data.local.entity.CreditCardInvoiceEntity
import com.robson.financas.data.local.entity.CreditCardPurchaseEntity
import com.robson.financas.data.local.relation.CreditCardPurchaseWithCategory
import com.robson.financas.data.local.relation.CreditCardSummary
import kotlinx.coroutines.flow.Flow

@Dao
interface CreditCardDao {
    @Insert
    suspend fun insert(card: CreditCardEntity): Long

    @Update
    suspend fun update(card: CreditCardEntity)

    @Delete
    suspend fun delete(card: CreditCardEntity)

    @Query("SELECT * FROM credit_cards WHERE id = :id")
    suspend fun getById(id: Long): CreditCardEntity?

    @Query("SELECT * FROM credit_cards WHERE isArchived = 0 ORDER BY id ASC")
    fun observeAll(): Flow<List<CreditCardEntity>>

    @Query(
        """
        SELECT cc.*, COALESCE(SUM(p.amountCents), 0) AS invoiceTotalCents,
            COALESCE(inv.isPaid, 0) AS invoicePaid
        FROM credit_cards cc
        LEFT JOIN credit_card_purchases p ON p.creditCardId = cc.id AND p.invoiceYearMonth = :yearMonth
        LEFT JOIN credit_card_invoices inv ON inv.creditCardId = cc.id AND inv.yearMonth = :yearMonth
        WHERE cc.isArchived = 0
        GROUP BY cc.id
        ORDER BY cc.id ASC
        """,
    )
    fun observeCardsWithInvoiceSummary(yearMonth: Int): Flow<List<CreditCardSummary>>

    @Insert
    suspend fun insertPurchases(purchases: List<CreditCardPurchaseEntity>)

    @Delete
    suspend fun deletePurchase(purchase: CreditCardPurchaseEntity)

    @Query(
        """
        SELECT p.*, c.name AS categoryName, c.icon AS categoryIcon, c.colorHex AS categoryColorHex
        FROM credit_card_purchases p
        LEFT JOIN categories c ON c.id = p.categoryId
        WHERE p.creditCardId = :creditCardId AND p.invoiceYearMonth = :yearMonth
        ORDER BY p.purchaseDate DESC
        """,
    )
    fun observePurchasesForInvoice(creditCardId: Long, yearMonth: Int): Flow<List<CreditCardPurchaseWithCategory>>

    @Upsert
    suspend fun upsertInvoice(invoice: CreditCardInvoiceEntity)

    @Query("SELECT * FROM credit_card_invoices WHERE creditCardId = :creditCardId AND yearMonth = :yearMonth")
    suspend fun getInvoice(creditCardId: Long, yearMonth: Int): CreditCardInvoiceEntity?
}
