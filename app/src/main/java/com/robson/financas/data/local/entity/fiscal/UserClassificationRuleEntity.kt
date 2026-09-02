package com.robson.financas.data.local.entity.fiscal

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.robson.financas.data.local.entity.CategoryEntity
import java.time.Instant

/** Como [matchValue] deve ser interpretado ao decidir se a regra se aplica a um item. */
enum class MatchType { DESCRIPTION_CONTAINS, EXACT_PRODUCT, ESTABLISHMENT, GTIN }

/**
 * Correção confirmada pelo usuário e promovida a regra (seção 10) — sempre vence contra
 * qualquer outra estratégia do motor. Nunca reclassifica itens antigos sozinha.
 */
@Entity(
    tableName = "user_classification_rules",
    foreignKeys = [
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
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["subcategoryId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = MicrocategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["microcategoryId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("productId"), Index("categoryId"), Index("subcategoryId"), Index("microcategoryId"),
    ],
)
data class UserClassificationRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val matchType: MatchType,
    val matchValue: String,
    val productId: Long? = null,
    val categoryId: Long,
    /** Nulo quando a regra aponta pra uma categoria do usuário, fora da taxonomia IA (sem microcategoria). */
    val subcategoryId: Long? = null,
    val microcategoryId: Long? = null,
    val priority: Int = 100,
    val active: Boolean = true,
    val timesApplied: Int = 0,
    val createdAt: Instant = Instant.now(),
)
