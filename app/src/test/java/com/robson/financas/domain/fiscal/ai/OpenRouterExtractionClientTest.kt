package com.robson.financas.domain.fiscal.ai

import com.robson.financas.data.local.entity.fiscal.DocumentStatus
import com.robson.financas.data.local.entity.fiscal.FiscalDocumentSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class OpenRouterExtractionClientTest {

    private val client = OpenRouterExtractionClient()

    @Test
    fun `parses well formed json into ParsedFiscalDocument`() {
        val content = """
            {
              "estabelecimento": {"nome": "MERCADO EXEMPLO LTDA", "cnpj": "12345678000199", "cidade": "Sao Paulo", "uf": "SP"},
              "data_emissao": "2026-08-25",
              "status": "autorizada",
              "total": "39.42",
              "itens": [
                {"descricao": "OMO LAV ROUPAS PO 2.2KG", "quantidade": 1.0, "unidade": "UN", "valor_unitario": "18.99", "valor_total": "18.99", "desconto": "0.00"},
                {"descricao": "QUEIJO MUSS KG", "quantidade": 0.512, "unidade": "KG", "valor_unitario": "39.90", "valor_total": "20.43", "desconto": "0.00"}
              ]
            }
        """.trimIndent()

        val result = client.parseExtractedJson(content)

        assertEquals(FiscalDocumentSource.AI_QRCODE, result.source)
        assertEquals("MERCADO EXEMPLO LTDA", result.issuerName)
        assertEquals("12345678000199", result.issuerCnpj)
        assertEquals(DocumentStatus.AUTHORIZED, result.status)
        assertEquals(LocalDate.of(2026, 8, 25), result.issuedAt)
        assertEquals(3942L, result.totalCents)
        assertEquals(2, result.items.size)
        assertEquals("OMO LAV ROUPAS PO 2.2KG", result.items[0].originalDescription)
        assertEquals(1899L, result.items[0].unitPriceCents)
        assertEquals(2043L, result.items[1].totalPriceCents)
    }

    @Test
    fun `tolerates surrounding markdown fences around the json object`() {
        val content = """
            Aqui está o JSON extraído:
            ```json
            {"estabelecimento": null, "data_emissao": "2026-01-01", "status": "desconhecido", "total": "10.00", "itens": []}
            ```
        """.trimIndent()

        val result = client.parseExtractedJson(content)

        assertNull(result.issuerName)
        assertEquals(DocumentStatus.UNKNOWN, result.status)
        assertEquals(1000L, result.totalCents)
        assertEquals(0, result.items.size)
    }

    @Test
    fun `falls back to today when issue date is missing or malformed`() {
        val content = """{"data_emissao": "não é uma data", "status": "cancelada", "total": "0.00", "itens": []}"""

        val result = client.parseExtractedJson(content)

        assertEquals(DocumentStatus.CANCELLED, result.status)
        assertEquals(LocalDate.now(), result.issuedAt)
    }

    @Test
    fun `parses normalized product names keyed by the raw description`() {
        val content = """
            {
              "BE.REFRI.COCA COLA S.ACUCAR PET": {"nome_canonico": "Coca-Cola sem Açúcar", "marca": "Coca-Cola"},
              "SALG.ELMA CHIPS CHEETOS LUA": {"nome_canonico": "Cheetos Onda", "marca": "Elma Chips"}
            }
        """.trimIndent()

        val result = client.parseNormalizedNames(content)

        assertEquals(2, result.size)
        assertEquals(NormalizedProductName("Coca-Cola sem Açúcar", "Coca-Cola"), result["BE.REFRI.COCA COLA S.ACUCAR PET"])
        assertEquals(NormalizedProductName("Cheetos Onda", "Elma Chips"), result["SALG.ELMA CHIPS CHEETOS LUA"])
    }

    @Test
    fun `normalized product names treats null or blank brand as no brand`() {
        val content = """{"ARROZ TIPO 1 5KG": {"nome_canonico": "Arroz Tipo 1", "marca": null}}"""

        val result = client.parseNormalizedNames(content)

        assertEquals(NormalizedProductName("Arroz Tipo 1", null), result["ARROZ TIPO 1 5KG"])
    }
}
