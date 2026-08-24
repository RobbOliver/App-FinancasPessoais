package com.robson.financas.data.local.entity

enum class AccountType {
    CHECKING,
    SAVINGS,
    CASH,
    INVESTMENT,
}

enum class CategoryType {
    INCOME,
    EXPENSE,
}

enum class TransactionType {
    INCOME,
    EXPENSE,
    TRANSFER,
}

enum class TransactionSource {
    MANUAL,
    NOTIFICATION,
}
