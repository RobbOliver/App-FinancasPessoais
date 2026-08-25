package com.robson.financas.domain.fiscal.ocr

import com.robson.financas.data.local.entity.fiscal.FiscalDocumentSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ReceiptTextParserTest {

    private val sampleReceipt = """
        MERCADO EXEMPLO LTDA
        25/08/2026 14:30
        OMO LAV ROUPAS PO 2.2KG          18,99
        QUEIJO MUSS KG                   20,43
        TOTAL                            39,42
    """.trimIndent()

    @Test
    fun `extracts items, skipping header and date lines`() {
        val result = ReceiptTextParser.parse(sampleReceipt)

        assertEquals(2, result.items.size)
        assertEquals("OMO LAV ROUPAS PO 2.2KG", result.items[0].originalDescription)
        assertEquals(1899L, result.items[0].totalPriceCents)
        assertEquals("QUEIJO MUSS KG", result.items[1].originalDescription)
        assertEquals(2043L, result.items[1].totalPriceCents)
    }

    @Test
    fun `reads the TOTAL line instead of summing when present`() {
        val result = ReceiptTextParser.parse(sampleReceipt)
        assertEquals(3942L, result.totalCents)
        assertEquals(FiscalDocumentSource.PHOTO_OCR, result.source)
    }

    @Test
    fun `extracts the printed date instead of defaulting to today`() {
        val result = ReceiptTextParser.parse(sampleReceipt)
        assertEquals(LocalDate.of(2026, 8, 25), result.issuedAt)
    }

    @Test
    fun `falls back to summing items when there is no TOTAL line`() {
        val noTotal = "AGUA MINERAL                     3,50\nPAO FRANCES                      7,20"
        val result = ReceiptTextParser.parse(noTotal)
        assertEquals(1070L, result.totalCents) // soma dos itens, nunca inventado
    }

    @Test
    fun `falls back to today when no date is printed`() {
        val noDate = "ITEM QUALQUER                    5,00"
        val result = ReceiptTextParser.parse(noDate)
        assertEquals(LocalDate.now(), result.issuedAt)
    }

    @Test
    fun `ignores lines with no trailing price`() {
        val noisy = "cupom fiscal eletronico\nobrigado pela preferencia\nAGUA MINERAL   3,50"
        val result = ReceiptTextParser.parse(noisy)
        assertEquals(1, result.items.size)
        assertTrue(result.items.none { it.originalDescription.contains("obrigado", ignoreCase = true) })
    }
}
