package com.robson.financas.data.local.entity.fiscal

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Cache permanente de normalização: descrição bruta de uma nota (abreviada, específica da
 * loja) → nome canônico + marca já resolvidos pela IA uma vez. Sem TTL — resolvido uma vez,
 * fica resolvido; a próxima nota com a mesma descrição bruta reaproveita direto daqui, sem
 * chamar a IA de novo (ver `AiExtractionViewModel`).
 */
@Entity(
    tableName = "product_aliases",
    indices = [Index(value = ["rawDescription"], unique = true)],
)
data class ProductAliasEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val rawDescription: String,
    val canonicalName: String,
    val brand: String?,
    val createdAt: Instant = Instant.now(),
)
