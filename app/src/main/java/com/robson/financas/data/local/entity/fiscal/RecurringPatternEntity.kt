package com.robson.financas.data.local.entity.fiscal

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate

enum class RecurrenceFrequency { WEEKLY, BIWEEKLY, MONTHLY, BIMONTHLY, QUARTERLY, SEASONAL, IRREGULAR }

/** Recorrência detectada (seção 12) — exige ao menos 3 ocorrências com intervalo consistente. */
@Entity(
    tableName = "recurring_patterns",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = MicrocategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["microcategoryId"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = EstablishmentEntity::class,
            parentColumns = ["id"],
            childColumns = ["establishmentId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("productId"), Index("microcategoryId"), Index("establishmentId")],
)
data class RecurringPatternEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long? = null,
    val microcategoryId: Long? = null,
    val establishmentId: Long? = null,
    val frequency: RecurrenceFrequency,
    val averageIntervalDays: Int,
    val averageAmountCents: Long,
    val occurrenceCount: Int,
    val confidence: Float,
    val lastOccurrenceAt: LocalDate,
    val nextExpectedAt: LocalDate,
    val updatedAt: Instant = Instant.now(),
)
