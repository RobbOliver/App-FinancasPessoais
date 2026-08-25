package com.robson.financas.domain.fiscal.qrcode

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NfceQrCodeParserTest {

    @Test
    fun `extracts key from querystring-style URL`() {
        val qr = "https://www.sefaz.sp.gov.br/nfce/qrcode?p=35260812345678000199650010000001231000000017|2|1|abc123"
        assertEquals("35260812345678000199650010000001231000000017", NfceQrCodeParser.extractAccessKey(qr))
    }

    @Test
    fun `extracts key from path-style URL`() {
        val qr = "https://nfce.sefaz.rs.gov.br/consulta/35260812345678000199650010000001231000000017"
        assertEquals("35260812345678000199650010000001231000000017", NfceQrCodeParser.extractAccessKey(qr))
    }

    @Test
    fun `returns null when there is no 44-digit sequence`() {
        assertNull(NfceQrCodeParser.extractAccessKey("https://example.com/not-a-fiscal-document"))
        assertNull(NfceQrCodeParser.extractAccessKey("12345"))
    }

    @Test
    fun `looksLikeNfceQrCode mirrors extraction`() {
        assertTrue(NfceQrCodeParser.looksLikeNfceQrCode("p=" + "0".repeat(44)))
        assertFalse(NfceQrCodeParser.looksLikeNfceQrCode("random text"))
    }
}
