package com.robson.financas.data.local.entity.fiscal

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.robson.financas.data.local.entity.CategoryEntity

/**
 * Classificação controlada pelo sistema — o usuário corrige o item, nunca edita esta taxonomia
 * diretamente. [subcategoryId] aponta para uma [CategoryEntity] filha (subcategoria editável).
 * [aliases]/[keywords] são arrays JSON usados pelo normalizador e pela regra de palavras-chave.
 */
@Entity(
    tableName = "microcategories",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["subcategoryId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("subcategoryId"),
        Index(value = ["systemKey"], unique = true),
    ],
)
data class MicrocategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val systemKey: String,
    val name: String,
    val subcategoryId: Long,
    val aliases: String = "[]",
    val keywords: String = "[]",
    val version: Int = 1,
    val active: Boolean = true,
)
