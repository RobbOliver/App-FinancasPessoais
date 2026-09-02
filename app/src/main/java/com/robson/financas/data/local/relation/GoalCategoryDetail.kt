package com.robson.financas.data.local.relation

/** Fatia (em centavos) do valor total de uma meta que uma categoria pode gastar. */
data class GoalCategoryAllocation(
    val categoryId: Long,
    val allocatedCents: Long,
)

/** Progresso individual de uma categoria dentro de uma meta — usado na tela de detalhe. */
data class GoalCategoryDetail(
    val categoryId: Long,
    val categoryName: String,
    val allocatedCents: Long,
    val spentCents: Long,
) {
    val remainingCents: Long get() = allocatedCents - spentCents
    val isOverBudget: Boolean get() = allocatedCents > 0 && spentCents > allocatedCents
}
