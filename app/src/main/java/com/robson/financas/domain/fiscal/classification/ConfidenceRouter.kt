package com.robson.financas.domain.fiscal.classification

import com.robson.financas.data.local.entity.fiscal.ClassificationStatus

/** Limiares de confiança (seção 8) — hoje constantes, prontos para virar configuráveis (DataStore). */
data class ConfidenceThresholds(
    val autoThreshold: Float = 0.90f,
    val suggestThreshold: Float = 0.75f,
    val confirmThreshold: Float = 0.50f,
)

object ConfidenceRouter {
    fun statusFor(confidence: Float, thresholds: ConfidenceThresholds = ConfidenceThresholds()): ClassificationStatus = when {
        confidence >= thresholds.autoThreshold -> ClassificationStatus.AUTOMATIC
        confidence >= thresholds.suggestThreshold -> ClassificationStatus.SUGGESTED
        confidence >= thresholds.confirmThreshold -> ClassificationStatus.NEEDS_CONFIRMATION
        else -> ClassificationStatus.NEEDS_REVIEW
    }
}
