package com.robson.financas.domain.fiscal.classification

import com.robson.financas.data.local.entity.fiscal.ClassificationSource
import com.robson.financas.data.local.entity.fiscal.MatchType

/** Resultado de uma estratégia — `null` ids representam "sem classificação" (fallback). */
data class ClassificationResult(
    val categoryId: Long?,
    val subcategoryId: Long?,
    val microcategoryId: Long?,
    val confidence: Float,
    val source: ClassificationSource,
    val reason: String,
)

/**
 * Uma regra pessoal já resolvida para o formato que as estratégias precisam (sem depender de Room).
 * [subcategoryId]/[microcategoryId] são nulos quando a regra aponta pra uma categoria "solta" do
 * usuário (fora da taxonomia IA), não uma microcategoria.
 */
data class UserRule(
    val id: Long,
    val matchType: MatchType,
    val matchValue: String,
    val productId: Long?,
    val categoryId: Long,
    val subcategoryId: Long?,
    val microcategoryId: Long?,
    val priority: Int,
)

/** Uma microcategoria e seu caminho completo na taxonomia, já resolvidos. */
data class MicrocategoryTaxonomy(
    val microcategoryId: Long,
    val subcategoryId: Long,
    val categoryId: Long,
    val keywords: List<String>,
)

data class PriorMatch(val categoryId: Long, val subcategoryId: Long, val microcategoryId: Long)

/**
 * Tudo que o motor de classificação precisa para decidir um item — pré-buscado pela camada de
 * repositório (que fala com o Room). O motor em si (`ClassificationEngine` + estratégias) é
 * Kotlin puro, sem I/O, para rodar em teste JVM sem emulador.
 */
data class ClassificationContext(
    val normalizedDescription: String, // descrição em maiúsculas, usada para casar termos
    val establishmentId: Long?,
    val productId: Long?,
    val gtin: String?,
    val userRules: List<UserRule>, // já ordenadas por priority DESC
    val microcategories: List<MicrocategoryTaxonomy>,
    val priorConfirmedMatch: PriorMatch?,
)

fun interface ClassificationStrategy {
    fun classify(context: ClassificationContext): ClassificationResult?
}
