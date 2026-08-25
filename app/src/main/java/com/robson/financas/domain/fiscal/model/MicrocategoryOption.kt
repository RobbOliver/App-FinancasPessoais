package com.robson.financas.domain.fiscal.model

/** Uma microcategoria com seu caminho completo — o que o seletor de correção mostra ao usuário. */
data class MicrocategoryOption(
    val microcategoryId: Long,
    val name: String,
    val subcategoryId: Long,
    val subcategoryName: String,
    val categoryId: Long,
    val categoryName: String,
)
