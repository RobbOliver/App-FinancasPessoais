package com.robson.financas.domain.fiscal.budget

enum class BudgetAlertLevel { NONE, HALF, EIGHTY, REACHED, EXCEEDED, PROJECTED_OVERSHOOT }

data class BudgetStatus(
    val limitCents: Long,
    val spentCents: Long,
    val remainingCents: Long,
    val percentConsumed: Float,
    val dailyAverageCents: Long,
    val projectedEndOfMonthCents: Long,
    val alertLevel: BudgetAlertLevel,
)

/** Orçamento por microcategoria (seção 16) — pura, sem I/O; o "gasto" já vem calculado do banco. */
object BudgetCalculator {
    fun evaluate(limitCents: Long, spentCents: Long, dayOfMonth: Int, daysInMonth: Int): BudgetStatus {
        val remaining = limitCents - spentCents
        val percent = if (limitCents > 0) (spentCents.toFloat() / limitCents) * 100f else 0f
        val dailyAverage = if (dayOfMonth > 0) spentCents / dayOfMonth else 0L
        val projected = dailyAverage * daysInMonth

        val alert = when {
            spentCents > limitCents -> BudgetAlertLevel.EXCEEDED
            percent >= 100f -> BudgetAlertLevel.REACHED
            projected > limitCents -> BudgetAlertLevel.PROJECTED_OVERSHOOT
            percent >= 80f -> BudgetAlertLevel.EIGHTY
            percent >= 50f -> BudgetAlertLevel.HALF
            else -> BudgetAlertLevel.NONE
        }

        return BudgetStatus(
            limitCents = limitCents,
            spentCents = spentCents,
            remainingCents = remaining,
            percentConsumed = percent,
            dailyAverageCents = dailyAverage,
            projectedEndOfMonthCents = projected,
            alertLevel = alert,
        )
    }
}
