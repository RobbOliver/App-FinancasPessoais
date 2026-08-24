package com.robson.financas.ui.common

import com.robson.financas.data.local.entity.AccountType
import com.robson.financas.data.local.entity.CategoryType

fun AccountType.label(): String = when (this) {
    AccountType.CHECKING -> "Conta corrente"
    AccountType.SAVINGS -> "Poupança"
    AccountType.CASH -> "Dinheiro"
    AccountType.INVESTMENT -> "Investimento"
}

fun CategoryType.label(): String = when (this) {
    CategoryType.INCOME -> "Receita"
    CategoryType.EXPENSE -> "Despesa"
}
