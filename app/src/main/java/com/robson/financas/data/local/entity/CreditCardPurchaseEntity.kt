package com.robson.financas.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "credit_card_purchases",
    foreignKeys = [
        ForeignKey(
            entity = CreditCardEntity::class,
            parentColumns = ["id"],
            childColumns = ["creditCardId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("creditCardId"), Index("categoryId")],
)
data class CreditCardPurchaseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val creditCardId: Long,
    val categoryId: Long? = null,
    val description: String,
    val amountCents: Long,
    val purchaseDate: LocalDate,
    val invoiceYearMonth: Int,
    val installmentGroupId: String,
    val installmentNumber: Int,
    val installmentTotal: Int,
)
