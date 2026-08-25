package com.robson.financas.data.local.relation

data class GoalProgress(
    val categoryId: Long,
    val amountCents: Long,
    val categoryName: String,
    val categoryIcon: String,
    val categoryColorHex: String,
    val parentCategoryId: Long?,
    val spentCents: Long,
) {
    val remainingCents: Long get() = amountCents - spentCents
}
