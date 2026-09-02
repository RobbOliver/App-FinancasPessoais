package com.robson.financas.ui.goals

/**
 * Meta em edição na folha (`SetGoalDialog`) — `id == null` significa criação de uma meta nova.
 * Cada categoria marcada carrega sua própria fatia do valor total (`categoryAllocations`), que
 * precisa somar exatamente `amountCents` pra poder salvar.
 */
data class EditingGoal(
    val id: Long? = null,
    val name: String = "",
    val amountCents: Long = 0L,
    val categoryAllocations: Map<Long, Long> = emptyMap(),
) {
    val selectedCategoryIds: Set<Long> get() = categoryAllocations.keys
    val allocatedTotalCents: Long get() = categoryAllocations.values.sum()
    val remainingToAllocateCents: Long get() = amountCents - allocatedTotalCents
    val isValid: Boolean
        get() = name.isNotBlank() && amountCents > 0 && categoryAllocations.isNotEmpty() && remainingToAllocateCents == 0L
}
