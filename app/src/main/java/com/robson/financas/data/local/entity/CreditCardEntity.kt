package com.robson.financas.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "credit_cards",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["paymentAccountId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("paymentAccountId")],
)
data class CreditCardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorHex: String,
    val icon: String,
    val closingDay: Int,
    val dueDay: Int,
    val limitCents: Long,
    val paymentAccountId: Long,
    val isArchived: Boolean = false,
)
