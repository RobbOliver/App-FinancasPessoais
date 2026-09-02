package com.robson.financas.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: AccountType,
    val initialBalanceCents: Long,
    val colorHex: String,
    val icon: String,
    val isArchived: Boolean = false,
    val createdAt: Instant = Instant.now(),
    /** Se essa conta aparece no card de saldo por conta do Resumo (Dashboard). */
    val showOnDashboard: Boolean = true,
)
