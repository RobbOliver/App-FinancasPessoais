package com.robson.financas.domain.fiscal.model

/** Resultado de uma tentativa de importação — nunca lança exceção para o chamador de UI. */
sealed interface FiscalImportResult {
    data class Success(val documentId: Long, val itemCount: Int, val needsAttention: Boolean) : FiscalImportResult
    data class Duplicate(val existingDocumentId: Long) : FiscalImportResult
    data class Invalid(val reason: String) : FiscalImportResult
}
