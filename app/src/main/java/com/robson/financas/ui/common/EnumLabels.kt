package com.robson.financas.ui.common

import com.robson.financas.data.local.entity.AccountType
import com.robson.financas.data.local.entity.CategoryType
import com.robson.financas.data.local.entity.TransactionType

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

fun TransactionType.label(): String = when (this) {
    TransactionType.INCOME -> "Receita"
    TransactionType.EXPENSE -> "Despesa"
    TransactionType.TRANSFER -> "Transferência"
}
