package com.robson.financas.data.local.entity.fiscal

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "establishments",
    indices = [Index(value = ["cnpj"], unique = true)],
)
data class EstablishmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cnpj: String?,
    val name: String,
    val normalizedName: String,
    val type: String? = null,
    val city: String? = null,
    val state: String? = null,
    val createdAt: Instant = Instant.now(),
)
