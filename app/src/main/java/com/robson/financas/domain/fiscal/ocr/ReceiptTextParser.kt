package com.robson.financas.domain.fiscal.ocr

import com.robson.financas.data.local.entity.fiscal.DocumentStatus
import com.robson.financas.data.local.entity.fiscal.FiscalDocumentSource
import com.robson.financas.domain.fiscal.model.ParsedFiscalDocument
import com.robson.financas.domain.fiscal.model.ParsedItem
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

/**
 * Heurística sobre texto já reconhecido por OCR (ML Kit Text Recognition) — não é o caminho
 * de maior confiança estrutural (por isso vem por último no plano, seção 24/20): sem colunas
 * reais, cada linha terminada em um valor monetário vira um item de quantidade 1, sem GTIN.
 * O motor de classificação já rebaixa naturalmente a confiança desses itens (menos sinais
 * determinísticos que XML/GTIN), então mais deles caem em revisão — isso é esperado, não um bug.
 *
 * Nunca inventa o que não conseguiu ler: sem uma linha "TOTAL", o total vira a soma dos itens
 * encontrados (valor derivado, não inventado); sem uma data reconhecível, assume hoje — a
 * suposição mais razoável para uma foto tirada logo após a compra, nunca escondida do usuário
 * (o documento nasce com `status = UNKNOWN`, sinalizando a origem de menor confiança).
 */
object ReceiptTextParser {
    private val priceLineRegex = Regex("""^(.+?)\s+(\d{1,3}(?:\.\d{3})*,\d{2})\s*$""")
    private val totalLineRegex = Regex("""TOTAL[^\d]*?(\d{1,3}(?:\.\d{3})*,\d{2})""", RegexOption.IGNORE_CASE)
    private val dateRegex = Regex("""(\d{2})/(\d{2})/(\d{4})""")

    fun parse(rawText: String, establishmentNameGuess: String? = null): ParsedFiscalDocument {
        val lines = rawText.lines().map { it.trim() }.filter { it.isNotBlank() }

        var totalCents: Long? = null
        val items = mutableListOf<ParsedItem>()

        for (line in lines) {
            val totalMatch = totalLineRegex.find(line)
            if (totalMatch != null) {
                totalCents = parseBrMoneyToCents(totalMatch.groupValues[1])
                continue
            }
            val itemMatch = priceLineRegex.find(line) ?: continue
            val description = itemMatch.groupValues[1].trim()
            if (description.length < 2) continue
            val priceCents = parseBrMoneyToCents(itemMatch.groupValues[2])
            if (priceCents <= 0) continue
            items += ParsedItem(
                originalDescription = description,
                gtin = null,
                quantity = 1.0,
                unit = "UN",
                unitPriceCents = priceCents,
                totalPriceCents = priceCents,
            )
        }

        val issuedAt = lines.firstNotNullOfOrNull { line -> extractDate(line) } ?: LocalDate.now()
        val resolvedTotal = totalCents ?: items.sumOf { it.totalPriceCents }

        return ParsedFiscalDocument(
            accessKey = null,
            source = FiscalDocumentSource.PHOTO_OCR,
            issuerCnpj = null,
            issuerName = establishmentNameGuess,
            issuerCity = null,
            issuerState = null,
            issuedAt = issuedAt,
            totalCents = resolvedTotal,
            status = DocumentStatus.UNKNOWN,
            items = items,
            rawData = rawText,
        )
    }

    private fun extractDate(line: String): LocalDate? {
        val match = dateRegex.find(line) ?: return null
        return runCatching {
            LocalDate.of(match.groupValues[3].toInt(), match.groupValues[2].toInt(), match.groupValues[1].toInt())
        }.getOrNull()
    }

    /** "1.234,56" (formato brasileiro) -> 123456 centavos. Nunca float. */
    private fun parseBrMoneyToCents(text: String): Long {
        val normalized = text.replace(".", "").replace(",", ".")
        return BigDecimal(normalized).movePointRight(2).setScale(0, RoundingMode.HALF_UP).toLong()
    }
}
