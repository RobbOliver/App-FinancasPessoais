package com.robson.financas.domain.fiscal.classification

import com.robson.financas.domain.fiscal.classification.strategies.ExactNormalizedMatchStrategy
import com.robson.financas.domain.fiscal.classification.strategies.FallbackToReviewStrategy
import com.robson.financas.domain.fiscal.classification.strategies.KeywordRuleStrategy
import com.robson.financas.domain.fiscal.classification.strategies.UserRuleStrategy

/**
 * Cadeia de responsabilidade — a primeira estratégia que responder decide. Ordem = prioridade
 * exata da seção 8 do plano de arquitetura. Fase 1 implementa 4 das 11 estratégias (as que não
 * dependem de histórico de GTIN/produto ainda inexistente); as demais (GTIN, produto conhecido,
 * código do estabelecimento, marca, contexto do estabelecimento, similaridade textual, LLM)
 * entram nas Fases 2/3 conforme o plano — a cadeia já está pronta para recebê-las no meio.
 */
class ClassificationEngine(private val strategies: List<ClassificationStrategy>) {

    fun classify(context: ClassificationContext): ClassificationResult {
        for (strategy in strategies) {
            strategy.classify(context)?.let { return it }
        }
        error("A cadeia deve sempre terminar com FallbackToReviewStrategy, que nunca retorna null.")
    }

    companion object {
        fun default(): ClassificationEngine = ClassificationEngine(
            listOf(
                UserRuleStrategy(),
                ExactNormalizedMatchStrategy(),
                KeywordRuleStrategy(),
                FallbackToReviewStrategy(),
            ),
        )
    }
}
