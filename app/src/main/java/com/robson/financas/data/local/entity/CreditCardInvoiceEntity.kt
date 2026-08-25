package com.robson.financas.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "credit_card_invoices",
    primaryKeys = ["creditCardId", "yearMonth"],
    foreignKeys = [
        ForeignKey(
            entity = CreditCardEntity::class,
            parentColumns = ["id"],
            childColumns = ["creditCardId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = TransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["paidTransactionId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("creditCardId"), Index("paidTransactionId")],
)
data class CreditCardInvoiceEntity(
    val creditCardId: Long,
    val yearMonth: Int,
    val isPaid: Boolean = false,
    val paidTransactionId: Long? = null,
)
