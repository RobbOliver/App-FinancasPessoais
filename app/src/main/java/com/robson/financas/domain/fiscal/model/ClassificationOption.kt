package com.robson.financas.domain.fiscal.model

/**
 * Uma opção selecionável na folha de correção de classificação — ou uma microcategoria da
 * taxonomia IA, ou uma categoria "solta" criada pelo usuário (fora dessa taxonomia).
 */
sealed class ClassificationOption {
    abstract val displayName: String
    abstract val categoryName: String

    data class Microcategory(
        val microcategoryId: Long,
        val name: String,
        val subcategoryId: Long,
        val subcategoryName: String,
        val categoryId: Long,
        override val categoryName: String,
    ) : ClassificationOption() {
        override val displayName: String get() = name
    }

    data class PlainCategory(
        val categoryId: Long,
        override val categoryName: String,
    ) : ClassificationOption() {
        override val displayName: String get() = categoryName
    }
}
