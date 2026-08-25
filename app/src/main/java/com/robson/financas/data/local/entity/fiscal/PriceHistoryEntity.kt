package com.robson.financas.data.local.entity.fiscal

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * Um ponto de preço por compra. [normalizedPriceCents]/[normalizedUnit] permitem comparar
 * apresentações diferentes (ex.: 1kg vs 5kg) pelo preço por kg/L/unidade (seção 11).
 */
@Entity(
    tableName = "price_history",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = EstablishmentEntity::class,
            parentColumns = ["id"],
            childColumns = ["establishmentId"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = PurchaseItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["purchaseItemId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("productId"), Index("establishmentId"), Index("purchaseItemId")],
)
data class PriceHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val establishmentId: Long? = null,
    val purchaseItemId: Long,
    val priceTotalCents: Long,
    val normalizedPriceCents: Long,
    val normalizedUnit: String,
    val quantity: Double,
    val purchasedAt: LocalDate,
    val isPromotional: Boolean = false,
)
