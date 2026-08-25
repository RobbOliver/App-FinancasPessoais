package com.robson.financas.domain.fiscal.parser

import com.robson.financas.data.local.entity.fiscal.DocumentStatus
import com.robson.financas.data.local.entity.fiscal.FiscalDocumentSource
import com.robson.financas.domain.fiscal.model.ParsedFiscalDocument
import com.robson.financas.domain.fiscal.model.ParsedItem
import org.w3c.dom.Element
import java.io.ByteArrayInputStream
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Lê o XML padrão de NF-e/NF-e-C (modelo 55/65) — schema nacional estável, documentado pela
 * Sefaz, igual em qualquer estado. Implementado em DOM puro (`javax.xml.parsers`, sem
 * `android.*`) para rodar em teste JVM sem emulador.
 *
 * Nunca inventa um campo ausente: um total/chave/item que não é encontrado no XML vira `null`
 * (para campos opcionais) ou lança [FiscalXmlParseException] (para o que é indispensável).
 */
class FiscalXmlParseException(message: String) : Exception(message)

object NfeXmlParser {

    private val dhEmiFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    fun parse(xml: String): ParsedFiscalDocument {
        val document = DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = false
        }.newDocumentBuilder().parse(ByteArrayInputStream(xml.toByteArray(Charsets.UTF_8)))

        val infNFe = document.getElementsByTagName("infNFe").item(0) as? Element
            ?: throw FiscalXmlParseException("XML não contém <infNFe> — não parece uma NF-e/NFC-e válida.")

        val accessKey = document.getElementsByTagName("chNFe").item(0)?.textContent
            ?: infNFe.getAttribute("Id").removePrefix("NFe").takeIf { it.length == 44 }

        val ide = infNFe.child("ide")
        val emit = infNFe.child("emit")
        val enderEmit = emit?.child("enderEmit")
        val total = infNFe.child("total")?.child("ICMSTot")

        val issuedAt = ide?.child("dhEmi")?.textContent?.let {
            LocalDateTime.parse(it, dhEmiFormatter).toLocalDate()
        } ?: LocalDate.now()

        val cStat = document.getElementsByTagName("cStat").item(0)?.textContent
        val status = when (cStat) {
            "100" -> DocumentStatus.AUTHORIZED
            "101", "151", "155" -> DocumentStatus.CANCELLED
            null -> DocumentStatus.UNKNOWN
            else -> DocumentStatus.UNKNOWN
        }

        val items = infNFe.children("det").map { det ->
            val prod = det.child("prod")
                ?: throw FiscalXmlParseException("Item <det> sem <prod> — XML fora do padrão.")
            val qtd = prod.child("qCom")?.textContent?.toDoubleOrNull()
                ?: throw FiscalXmlParseException("Item sem quantidade (qCom).")
            val unitPrice = prod.child("vUnCom")?.textContent?.toCents()
                ?: throw FiscalXmlParseException("Item sem preço unitário (vUnCom).")
            val totalPrice = prod.child("vProd")?.textContent?.toCents()
                ?: throw FiscalXmlParseException("Item sem valor total (vProd).")
            val discount = det.child("vDesc")?.textContent?.toCents() ?: 0L

            ParsedItem(
                originalDescription = prod.child("xProd")?.textContent.orEmpty(),
                gtin = prod.child("cEAN")?.textContent?.takeIf { it.isNotBlank() && it != "SEM GTIN" },
                quantity = qtd,
                unit = prod.child("uCom")?.textContent.orEmpty(),
                unitPriceCents = unitPrice,
                totalPriceCents = totalPrice,
                discountCents = discount,
            )
        }

        val totalCents = total?.child("vNF")?.textContent?.toCents()
            ?: throw FiscalXmlParseException("Documento sem total (vNF) — não é possível conciliar itens.")

        return ParsedFiscalDocument(
            accessKey = accessKey,
            // Posições 20-21 da chave de 44 dígitos carregam o modelo do documento: "55" = NF-e, "65" = NFC-e.
            source = if (accessKey?.getOrNull(20) == '6') FiscalDocumentSource.XML_NFCE else FiscalDocumentSource.XML_NFE,
            issuerCnpj = emit?.child("CNPJ")?.textContent,
            issuerName = emit?.child("xNome")?.textContent,
            issuerCity = enderEmit?.child("xMun")?.textContent,
            issuerState = enderEmit?.child("UF")?.textContent,
            issuedAt = issuedAt,
            totalCents = totalCents,
            status = status,
            items = items,
            rawData = xml,
        )
    }

    /** Converte "18.99" (decimal do XML) para centavos via BigDecimal — nunca float. */
    private fun String.toCents(): Long =
        BigDecimal(this.trim()).movePointRight(2).setScale(0, RoundingMode.HALF_UP).toLong()

    private fun Element.child(tag: String): Element? {
        val nodes = getElementsByTagName(tag)
        for (i in 0 until nodes.length) {
            val node = nodes.item(i) as Element
            if (node.parentNode == this) return node
        }
        return null
    }

    private fun Element.children(tag: String): List<Element> {
        val nodes = getElementsByTagName(tag)
        val result = mutableListOf<Element>()
        for (i in 0 until nodes.length) {
            val node = nodes.item(i) as Element
            if (node.parentNode == this) result += node
        }
        return result
    }
}
