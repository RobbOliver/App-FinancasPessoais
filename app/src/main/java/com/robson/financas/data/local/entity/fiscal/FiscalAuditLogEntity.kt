package com.robson.financas.data.local.entity.fiscal

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

/** Trilha de auditoria só para ações "importantes" (seção 20): confirmação, correção, regra, exclusão. */
@Entity(tableName = "fiscal_audit_logs")
data class FiscalAuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entityType: String,
    val entityId: Long,
    val action: String,
    val oldValue: String? = null,
    val newValue: String? = null,
    val createdAt: Instant = Instant.now(),
)
