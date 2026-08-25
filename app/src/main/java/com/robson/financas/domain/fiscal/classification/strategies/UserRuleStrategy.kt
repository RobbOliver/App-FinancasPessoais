package com.robson.financas.domain.fiscal.classification.strategies

import com.robson.financas.data.local.entity.fiscal.ClassificationSource
import com.robson.financas.data.local.entity.fiscal.MatchType
import com.robson.financas.domain.fiscal.classification.ClassificationContext
import com.robson.financas.domain.fiscal.classification.ClassificationResult
import com.robson.financas.domain.fiscal.classification.ClassificationStrategy
import com.robson.financas.domain.fiscal.classification.UserRule
import com.robson.financas.data.local.seed.fiscal.parseJsonStringArray

/**
 * Prioridade 1 — uma regra pessoal confirmada sempre vence, mesmo contra GTIN exato (seção 10).
 * [UserRule.matchValue] é interpretado conforme [UserRule.matchType]:
 * - DESCRIPTION_CONTAINS: array JSON de termos, todos precisam estar na descrição.
 * - ESTABLISHMENT: "estabelecimentoId|TERMO1|TERMO2" — termos só valem naquele estabelecimento.
 * - EXACT_PRODUCT: usa [UserRule.productId] diretamente, matchValue não é usado.
 * - GTIN: o próprio GTIN como texto puro.
 */
class UserRuleStrategy : ClassificationStrategy {
    override fun classify(context: ClassificationContext): ClassificationResult? {
        val rule = context.userRules.firstOrNull { matches(it, context) } ?: return null
        return ClassificationResult(
            categoryId = rule.categoryId,
            subcategoryId = rule.subcategoryId,
            microcategoryId = rule.microcategoryId,
            confidence = 1.0f,
            source = ClassificationSource.USER_RULE,
            reason = "Aplicado pela sua regra pessoal.",
        )
    }

    private fun matches(rule: UserRule, context: ClassificationContext): Boolean = when (rule.matchType) {
        MatchType.DESCRIPTION_CONTAINS ->
            rule.matchValue.parseJsonStringArray().all { term -> context.normalizedDescription.contains(term) }

        MatchType.ESTABLISHMENT -> {
            val parts = rule.matchValue.split("|")
            val ruleEstablishmentId = parts.firstOrNull()?.toLongOrNull()
            val terms = parts.drop(1)
            ruleEstablishmentId != null && ruleEstablishmentId == context.establishmentId &&
                terms.all { context.normalizedDescription.contains(it) }
        }

        MatchType.EXACT_PRODUCT -> rule.productId != null && rule.productId == context.productId

        MatchType.GTIN -> context.gtin != null && rule.matchValue == context.gtin
    }
}
