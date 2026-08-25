package com.robson.financas.data.local.entity.fiscal

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Produto normalizado — separado de [MicrocategoryEntity] de propósito (seção 6 do plano):
 * marca, peso e apresentação vivem aqui para permitir comparação de preço correta; a
 * microcategoria nunca carrega essa informação.
 */
@Entity(
    tableName = "products",
    foreignKeys = [
        ForeignKey(
            entity = MicrocategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["defaultMicrocategoryId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index("defaultMicrocategoryId"),
        Index(value = ["gtin"], unique = true),
    ],
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val normalizedName: String,
    val genericName: String,
    val brand: String?,
    val gtin: String?,
    val defaultMicrocategoryId: Long? = null,
    val createdAt: Instant = Instant.now(),
)
