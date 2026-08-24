package com.robson.financas.ui.common

import com.robson.financas.data.local.entity.AccountType

fun AccountType.label(): String = when (this) {
    AccountType.CHECKING -> "Conta corrente"
    AccountType.SAVINGS -> "Poupança"
    AccountType.CASH -> "Dinheiro"
    AccountType.INVESTMENT -> "Investimento"
}
