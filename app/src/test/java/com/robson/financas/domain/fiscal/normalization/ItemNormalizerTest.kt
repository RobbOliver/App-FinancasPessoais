package com.robson.financas.domain.fiscal.normalization

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ItemNormalizerTest {

    @Test
    fun `extracts brand and weight in kg, converts to grams`() {
        val result = ItemNormalizer.normalize("OMO LAV ROUPAS PO 2.2KG")

        assertEquals("Omo", result.brand)
        assertEquals(2200, result.weightGrams)
        assertNull(result.volumeMl)
        assertEquals("Lav Roupas Po Omo", result.normalizedName)
    }

    @Test
    fun `item sold by weight with no number in description keeps unit token, no brand found`() {
        val result = ItemNormalizer.normalize("QUEIJO MUSS KG")

        assertNull(result.brand)
        assertNull(result.weightGrams) // sem número antes de KG — o peso real vem de qCom, não da descrição
        assertEquals("Queijo Muss Kg", result.genericName)
    }

    @Test
    fun `extracts volume in liters, converts to milliliters`() {
        val result = ItemNormalizer.normalize("COCA COLA 2L")

        assertEquals("Coca Cola", result.brand)
        assertEquals(2000, result.volumeMl)
        assertNull(result.weightGrams)
    }

    @Test
    fun `no brand and no unit falls back to title-cased original`() {
        val result = ItemNormalizer.normalize("BANANA PRATA")

        assertNull(result.brand)
        assertNull(result.weightGrams)
        assertEquals("Banana Prata", result.genericName)
        assertEquals("Banana Prata", result.normalizedName)
    }
}
