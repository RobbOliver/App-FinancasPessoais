package com.robson.financas.ui.common

import com.robson.financas.data.local.entity.AccountType
import com.robson.financas.data.local.entity.CategoryType
import com.robson.financas.data.local.entity.TransactionRecurrence
import com.robson.financas.data.local.entity.TransactionType
import com.robson.financas.ui.categories.CategoryTab

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

fun CategoryTab.label(): String = when (this) {
    CategoryTab.RECEITAS -> "Receitas"
    CategoryTab.DESPESAS -> "Despesas"
    CategoryTab.IA -> "IA"
}

fun TransactionRecurrence.label(): String = when (this) {
    TransactionRecurrence.DAILY -> "Diário"
    TransactionRecurrence.WEEKLY -> "Semanal"
    TransactionRecurrence.BIWEEKLY -> "Quinzenal"
    TransactionRecurrence.MONTHLY -> "Mensal"
    TransactionRecurrence.BIMONTHLY -> "Bimestral"
    TransactionRecurrence.QUARTERLY -> "Trimestral"
    TransactionRecurrence.YEARLY -> "Anual"
}
