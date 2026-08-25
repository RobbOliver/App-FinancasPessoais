package com.robson.financas.domain.fiscal.parser

import com.robson.financas.data.local.entity.fiscal.DocumentStatus
import com.robson.financas.data.local.entity.fiscal.FiscalDocumentSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NfeXmlParserTest {

    private val sampleAccessKey = "35260812345678000199650010000001231000000017"
        .let { it.substring(0, 44) } // sanity — mantém 44 dígitos legível no teste

    private val sampleXml = """
        <?xml version="1.0" encoding="UTF-8"?>
        <nfeProc>
          <NFe>
            <infNFe Id="NFe$sampleAccessKey">
              <ide>
                <cUF>35</cUF>
                <dhEmi>2026-08-25T14:30:00-03:00</dhEmi>
              </ide>
              <emit>
                <CNPJ>12345678000199</CNPJ>
                <xNome>MERCADO EXEMPLO LTDA</xNome>
                <enderEmit>
                  <xMun>Sao Paulo</xMun>
                  <UF>SP</UF>
                </enderEmit>
              </emit>
              <det nItem="1">
                <prod>
                  <cProd>001</cProd>
                  <cEAN>7891000100103</cEAN>
                  <xProd>OMO LAV ROUPAS PO 2.2KG</xProd>
                  <uCom>UN</uCom>
                  <qCom>1.0000</qCom>
                  <vUnCom>18.99</vUnCom>
                  <vProd>18.99</vProd>
                </prod>
              </det>
              <det nItem="2">
                <prod>
                  <cProd>002</cProd>
                  <cEAN>SEM GTIN</cEAN>
                  <xProd>QUEIJO MUSS KG</xProd>
                  <uCom>KG</uCom>
                  <qCom>0.5120</qCom>
                  <vUnCom>39.90</vUnCom>
                  <vProd>20.43</vProd>
                </prod>
              </det>
              <total>
                <ICMSTot>
                  <vNF>39.42</vNF>
                </ICMSTot>
              </total>
            </infNFe>
            <protNFe>
              <infProt>
                <chNFe>$sampleAccessKey</chNFe>
                <cStat>100</cStat>
              </infProt>
            </protNFe>
          </NFe>
        </nfeProc>
    """.trimIndent()

    @Test
    fun `parses header fields correctly`() {
        val result = NfeXmlParser.parse(sampleXml)

        assertEquals(sampleAccessKey, result.accessKey)
        assertEquals(FiscalDocumentSource.XML_NFCE, result.source) // modelo 65 na posição 20-21 da chave
        assertEquals("12345678000199", result.issuerCnpj)
        assertEquals("MERCADO EXEMPLO LTDA", result.issuerName)
        assertEquals("SP", result.issuerState)
        assertEquals(DocumentStatus.AUTHORIZED, result.status)
        assertEquals(3942L, result.totalCents)
    }

    @Test
    fun `parses items with correct decimal-to-cents conversion`() {
        val result = NfeXmlParser.parse(sampleXml)

        assertEquals(2, result.items.size)

        val soap = result.items[0]
        assertEquals("OMO LAV ROUPAS PO 2.2KG", soap.originalDescription)
        assertEquals("7891000100103", soap.gtin)
        assertEquals(1899L, soap.unitPriceCents)
        assertEquals(1899L, soap.totalPriceCents)
        assertEquals(1.0, soap.quantity, 0.0001)

        val cheese = result.items[1]
        assertEquals("QUEIJO MUSS KG", cheese.originalDescription)
        assertNull(cheese.gtin) // "SEM GTIN" é tratado como ausência de GTIN, não como valor literal
        assertEquals(2043L, cheese.totalPriceCents)
        assertEquals(0.512, cheese.quantity, 0.0001)
    }

    @Test(expected = FiscalXmlParseException::class)
    fun `throws on XML without infNFe`() {
        NfeXmlParser.parse("<root><foo>bar</foo></root>")
    }
}
