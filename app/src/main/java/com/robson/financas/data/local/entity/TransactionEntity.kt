package com.robson.financas.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.RESTRICT,
        ),
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["transferToAccountId"],
            onDelete = ForeignKey.RESTRICT,
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index("accountId"),
        Index("transferToAccountId"),
        Index("categoryId"),
    ],
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: TransactionType,
    val amountCents: Long,
    val accountId: Long,
    val transferToAccountId: Long? = null,
    val categoryId: Long? = null,
    val date: LocalDate,
    val description: String = "",
    val createdAt: Instant = Instant.now(),
    val source: TransactionSource = TransactionSource.MANUAL,
    val needsReview: Boolean = false,
    val counterpartyName: String? = null,
    val rawNotificationText: String? = null,
    val isPaid: Boolean = true,
    val isIgnored: Boolean = false,
    val isFavorite: Boolean = false,
    val attachmentPath: String? = null,
    val isRecurring: Boolean = false,
    val recurrenceFrequency: TransactionRecurrence? = null,
)
