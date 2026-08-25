package com.robson.financas.domain.fiscal.classification.strategies

import com.robson.financas.data.local.entity.fiscal.ClassificationSource
import com.robson.financas.domain.fiscal.classification.ClassificationContext
import com.robson.financas.domain.fiscal.classification.ClassificationResult
import com.robson.financas.domain.fiscal.classification.ClassificationStrategy

/** Prioridade 11 — sempre responde. Nunca inventa uma categoria; item vai para "a revisar". */
class FallbackToReviewStrategy : ClassificationStrategy {
    override fun classify(context: ClassificationContext): ClassificationResult = ClassificationResult(
        categoryId = null,
        subcategoryId = null,
        microcategoryId = null,
        confidence = 0f,
        source = ClassificationSource.NEEDS_REVIEW,
        reason = "Nenhuma regra, produto conhecido ou palavra-chave reconhecida para este item.",
    )
}
