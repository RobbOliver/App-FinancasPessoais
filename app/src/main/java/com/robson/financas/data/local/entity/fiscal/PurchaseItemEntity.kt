package com.robson.financas.data.local.entity.fiscal

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.robson.financas.data.local.entity.CategoryEntity
import java.time.Instant

/** Fonte da decisão de classificação — ordem de prioridade do motor híbrido (seção 8 do plano). */
enum class ClassificationSource {
    USER_RULE, GTIN, KNOWN_PRODUCT, ESTABLISHMENT_CODE, EXACT_MATCH,
    KEYWORD_RULE, BRAND, ESTABLISHMENT_CONTEXT, TEXT_SIMILARITY, LLM, NEEDS_REVIEW,
    /** O usuário corrigiu manualmente na tela de revisão — distinto de USER_RULE (regra automática). */
    USER_CORRECTION,
}

/** "confirmado" / "automático" / "sugerido" / "a revisar" — nunca inferido pela UI, sempre gravado. */
enum class ClassificationStatus { AUTOMATIC, SUGGESTED, NEEDS_CONFIRMATION, NEEDS_REVIEW, CONFIRMED }

@Entity(
    tableName = "purchase_items",
    foreignKeys = [
        ForeignKey(
            entity = FiscalDocumentEntity::class,
            parentColumns = ["id"],
            childColumns = ["fiscalDocumentId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = EstablishmentEntity::class,
            parentColumns = ["id"],
            childColumns = ["establishmentId"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["subcategoryId"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = MicrocategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["microcategoryId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index("fiscalDocumentId"),
        Index("establishmentId"),
        Index("productId"),
        Index("categoryId"),
        Index("subcategoryId"),
        Index("microcategoryId"),
    ],
)
data class PurchaseItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fiscalDocumentId: Long,
    val establishmentId: Long? = null,
    val productId: Long? = null,
    val originalDescription: String,
    val normalizedDescription: String,
    val quantity: Double,
    val unit: String,
    val unitPriceCents: Long,
    val totalPriceCents: Long,
    val discountCents: Long = 0,
    val weightGrams: Int? = null,
    val volumeMl: Int? = null,
    val categoryId: Long? = null,
    val subcategoryId: Long? = null,
    val microcategoryId: Long? = null,
    val classificationConfidence: Float? = null,
    val classificationSource: ClassificationSource? = null,
    val classificationStatus: ClassificationStatus = ClassificationStatus.NEEDS_REVIEW,
    val classificationReason: String? = null,
    val createdAt: Instant = Instant.now(),
)
