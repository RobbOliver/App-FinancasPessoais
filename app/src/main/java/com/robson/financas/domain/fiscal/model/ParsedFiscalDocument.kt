package com.robson.financas.domain.fiscal.model

import com.robson.financas.data.local.entity.fiscal.DocumentStatus
import com.robson.financas.data.local.entity.fiscal.FiscalDocumentSource
import java.time.LocalDate

/**
 * Resultado intermediário e agnóstico de fonte (XML, QR, OCR) de ler um documento fiscal —
 * o que entra em [com.robson.financas.data.repository.fiscal.FiscalDocumentRepository] para
 * virar entidades Room. Nunca é a origem da verdade: o `rawData` bruto é preservado à parte.
 */
data class ParsedFiscalDocument(
    val accessKey: String?,
    val source: FiscalDocumentSource,
    val issuerCnpj: String?,
    val issuerName: String?,
    val issuerCity: String?,
    val issuerState: String?,
    val issuedAt: LocalDate,
    val totalCents: Long,
    val status: DocumentStatus,
    val items: List<ParsedItem>,
    val rawData: String,
)

data class ParsedItem(
    val originalDescription: String,
    val gtin: String?,
    val quantity: Double,
    val unit: String,
    val unitPriceCents: Long,
    val totalPriceCents: Long,
    val discountCents: Long = 0,
    /** Nome/marca canônicos já resolvidos (cache local ou IA) — só preenchido no caminho QR/IA. */
    val canonicalName: String? = null,
    val canonicalBrand: String? = null,
)
