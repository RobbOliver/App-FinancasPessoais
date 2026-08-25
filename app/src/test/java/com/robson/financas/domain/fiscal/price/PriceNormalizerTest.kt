package com.robson.financas.domain.fiscal.price

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PriceNormalizerTest {

    @Test
    fun `1kg for R8 vs 5kg for R35 — the 5kg pack is cheaper per kilogram`() {
        val a = PriceNormalizer.normalize(unit = "KG", quantity = 1.0, totalCents = 800)
        val b = PriceNormalizer.normalize(unit = "KG", quantity = 5.0, totalCents = 3500)

        assertEquals(800L, a.normalizedPriceCents)
        assertEquals(700L, b.normalizedPriceCents)
        assertTrue(b.normalizedPriceCents < a.normalizedPriceCents)
    }

    @Test
    fun `grams convert to price per kilogram`() {
        val result = PriceNormalizer.normalize(unit = "G", quantity = 500.0, totalCents = 1000)
        assertEquals("kg", result.normalizedUnit)
        assertEquals(2000L, result.normalizedPriceCents) // 500g por R$10 = R$20/kg
    }

    @Test
    fun `milliliters convert to price per liter`() {
        val result = PriceNormalizer.normalize(unit = "ML", quantity = 500.0, totalCents = 500)
        assertEquals("l", result.normalizedUnit)
        assertEquals(1000L, result.normalizedPriceCents)
    }

    @Test
    fun `variation percent matches the documented formula`() {
        assertEquals(25.0, PriceNormalizer.variationPercent(current = 1000, previous = 800), 0.001)
        assertEquals(-20.0, PriceNormalizer.variationPercent(current = 800, previous = 1000), 0.001)
    }
}
