package com.robson.financas.domain.fiscal.classification.strategies

import com.robson.financas.data.local.entity.fiscal.ClassificationSource
import com.robson.financas.domain.fiscal.classification.ClassificationContext
import com.robson.financas.domain.fiscal.classification.ClassificationResult
import com.robson.financas.domain.fiscal.classification.ClassificationStrategy

/**
 * Prioridade 5 — já vimos essa descrição exata antes e o usuário confirmou/o sistema já a
 * classificou automaticamente. [ClassificationContext.priorConfirmedMatch] é pré-buscado pela
 * camada de repositório com uma única consulta antes do motor rodar.
 */
class ExactNormalizedMatchStrategy : ClassificationStrategy {
    override fun classify(context: ClassificationContext): ClassificationResult? {
        val prior = context.priorConfirmedMatch ?: return null
        return ClassificationResult(
            categoryId = prior.categoryId,
            subcategoryId = prior.subcategoryId,
            microcategoryId = prior.microcategoryId,
            confidence = 0.95f,
            source = ClassificationSource.EXACT_MATCH,
            reason = "Mesma descrição já foi classificada assim antes.",
        )
    }
}
