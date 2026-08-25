package com.robson.financas.domain.fiscal.budget

import org.junit.Assert.assertEquals
import org.junit.Test

class BudgetCalculatorTest {

    @Test
    fun `under half spent has no alert`() {
        val status = BudgetCalculator.evaluate(limitCents = 10000, spentCents = 3000, dayOfMonth = 10, daysInMonth = 30)
        assertEquals(BudgetAlertLevel.NONE, status.alertLevel)
        assertEquals(7000L, status.remainingCents)
    }

    @Test
    fun `crossing 50 percent triggers HALF`() {
        val status = BudgetCalculator.evaluate(limitCents = 10000, spentCents = 5000, dayOfMonth = 15, daysInMonth = 30)
        assertEquals(BudgetAlertLevel.HALF, status.alertLevel)
        assertEquals(50f, status.percentConsumed, 0.01f)
    }

    @Test
    fun `crossing 80 percent late in the month triggers EIGHTY, not a projection alert`() {
        // Dia 27 de 30: no ritmo atual (315/dia), a projeção fecha em R$94,20 — abaixo do limite,
        // então o alerta certo é "80% consumido", não "tendência de estouro".
        val status = BudgetCalculator.evaluate(limitCents = 10000, spentCents = 8500, dayOfMonth = 27, daysInMonth = 30)
        assertEquals(BudgetAlertLevel.EIGHTY, status.alertLevel)
    }

    @Test
    fun `exactly at limit is REACHED, above it is EXCEEDED`() {
        val reached = BudgetCalculator.evaluate(limitCents = 10000, spentCents = 10000, dayOfMonth = 28, daysInMonth = 30)
        assertEquals(BudgetAlertLevel.REACHED, reached.alertLevel)

        val exceeded = BudgetCalculator.evaluate(limitCents = 10000, spentCents = 10500, dayOfMonth = 28, daysInMonth = 30)
        assertEquals(BudgetAlertLevel.EXCEEDED, exceeded.alertLevel)
        assertEquals(-500L, exceeded.remainingCents)
    }

    @Test
    fun `pace on day 5 that would overshoot by month end is flagged even under 50 percent`() {
        // R$50 gasto em 5 dias de um limite de R$100 = média R$10/dia; projeção pro mes (30 dias) = R$300.
        val status = BudgetCalculator.evaluate(limitCents = 10000, spentCents = 5000, dayOfMonth = 5, daysInMonth = 30)
        assertEquals(BudgetAlertLevel.PROJECTED_OVERSHOOT, status.alertLevel)
        assertEquals(30000L, status.projectedEndOfMonthCents)
    }
}
