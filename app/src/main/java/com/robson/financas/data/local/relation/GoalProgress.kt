package com.robson.financas.data.local.relation

data class GoalProgress(
    val goalId: Long,
    val name: String,
    val amountCents: Long,
    val categoryNames: String?,
    val spentCents: Long,
) {
    val remainingCents: Long get() = amountCents - spentCents
}
