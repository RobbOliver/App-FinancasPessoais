package com.robson.financas.data.local.relation

data class CategoryExpenseSlice(
    val categoryId: Long,
    val categoryName: String,
    val categoryColorHex: String,
    val totalCents: Long,
)
