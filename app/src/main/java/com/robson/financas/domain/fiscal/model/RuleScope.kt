package com.robson.financas.domain.fiscal.model

/** Alcance da correção de classificação escolhida pelo usuário na tela de revisão. */
enum class RuleScope {
    /** Corrige só este item — não cria regra, próximas compras seguem passando pelo motor normal. */
    NONE,

    /** Cria uma regra pessoal ligada à identidade do produto — próximas compras do mesmo produto (em qualquer loja/nota) vêm com esta classificação. */
    THIS_PRODUCT,
}
