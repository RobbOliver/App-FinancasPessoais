package com.robson.financas.domain.fiscal.classification.strategies

import com.robson.financas.data.local.entity.fiscal.ClassificationSource
import com.robson.financas.domain.fiscal.classification.ClassificationContext
import com.robson.financas.domain.fiscal.classification.ClassificationResult
import com.robson.financas.domain.fiscal.classification.ClassificationStrategy
import com.robson.financas.domain.fiscal.classification.MicrocategoryTaxonomy

/** Confiança mínima para keyword específica o bastante (>= 8 chars) render automático (>= 0.90). */
private const val SPECIFIC_KEYWORD_MIN_LENGTH = 8
private const val SPECIFIC_KEYWORD_CONFIDENCE = 0.92f
private const val GENERIC_KEYWORD_CONFIDENCE = 0.80f

/**
 * Prioridade 6 — casa a descrição contra as palavras-chave da taxonomia (seedadas em
 * [com.robson.financas.data.local.seed.fiscal.FiscalTaxonomySeeder]). Prefere o termo mais
 * longo/específico encontrado, não o primeiro — evita que um match genérico vença um específico.
 */
class KeywordRuleStrategy : ClassificationStrategy {
    override fun classify(context: ClassificationContext): ClassificationResult? {
        var best: Pair<MicrocategoryTaxonomy, String>? = null

        for (micro in context.microcategories) {
            for (keyword in micro.keywords) {
                if (keyword.isBlank() || !context.normalizedDescription.contains(keyword)) continue
                if (best == null || keyword.length > best!!.second.length) {
                    best = micro to keyword
                }
            }
        }

        val (micro, keyword) = best ?: return null
        val confidence = if (keyword.trim().length >= SPECIFIC_KEYWORD_MIN_LENGTH) {
            SPECIFIC_KEYWORD_CONFIDENCE
        } else {
            GENERIC_KEYWORD_CONFIDENCE
        }

        return ClassificationResult(
            categoryId = micro.categoryId,
            subcategoryId = micro.subcategoryId,
            microcategoryId = micro.microcategoryId,
            confidence = confidence,
            source = ClassificationSource.KEYWORD_RULE,
            reason = "A descrição contém o termo \"${keyword.trim()}\".",
        )
    }
}
