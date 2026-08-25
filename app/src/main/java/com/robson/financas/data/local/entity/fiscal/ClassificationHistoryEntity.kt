package com.robson.financas.data.local.entity.fiscal

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/** Toda mudança de classificação de um item gera uma linha aqui — base do "desfazer". */
@Entity(
    tableName = "classification_history",
    foreignKeys = [
        ForeignKey(
            entity = PurchaseItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["purchaseItemId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("purchaseItemId")],
)
data class ClassificationHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val purchaseItemId: Long,
    val previousCategoryId: Long?,
    val previousSubcategoryId: Long?,
    val previousMicrocategoryId: Long?,
    val newCategoryId: Long?,
    val newSubcategoryId: Long?,
    val newMicrocategoryId: Long?,
    val source: ClassificationSource,
    val confidence: Float?,
    val changedByUser: Boolean,
    val createdAt: Instant = Instant.now(),
)
