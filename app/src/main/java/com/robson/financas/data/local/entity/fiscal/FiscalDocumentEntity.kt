package com.robson.financas.data.local.entity.fiscal

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.robson.financas.data.local.entity.TransactionEntity
import java.time.Instant
import java.time.LocalDate

enum class FiscalDocumentSource { QR_NFCE, XML_NFE, XML_NFCE, PHOTO_OCR, MANUAL }

enum class DocumentStatus { AUTHORIZED, CANCELLED, CONTINGENCY, UNKNOWN }

/**
 * Uma nota fiscal (NFC-e/NF-e) ou lançamento manual equivalente. [rawData] preserva o payload
 * original (XML/HTML de consulta/texto OCR) intocado — nunca sobrescrito por normalização.
 */
@Entity(
    tableName = "fiscal_documents",
    foreignKeys = [
        ForeignKey(
            entity = EstablishmentEntity::class,
            parentColumns = ["id"],
            childColumns = ["establishmentId"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = TransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["linkedTransactionId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index("establishmentId"),
        Index("linkedTransactionId"),
        Index(value = ["accessKey"], unique = true),
        Index(value = ["idempotencyHash"], unique = true),
    ],
)
data class FiscalDocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accessKey: String?,
    val source: FiscalDocumentSource,
    val issuerCnpj: String?,
    val establishmentId: Long? = null,
    val issuedAt: LocalDate,
    val totalCents: Long,
    val status: DocumentStatus = DocumentStatus.UNKNOWN,
    val rawData: String,
    val needsAttention: Boolean = false,
    val pendingValidation: Boolean = false,
    val idempotencyHash: String,
    val linkedTransactionId: Long? = null,
    val createdAt: Instant = Instant.now(),
)
