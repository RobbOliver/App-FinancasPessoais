package com.robson.financas.domain.fiscal

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AccessKeyValidatorTest {

    @Test
    fun `all-zero key is valid — trivial mod-11 case, remainder under 2 yields digit 0`() {
        val key = "0".repeat(44)
        assertTrue(AccessKeyValidator.isValid(key))
    }

    @Test
    fun `hand-computed case — single 1 at position 0 yields check digit 7`() {
        // Peso na posição 0 (mais à esquerda de 43 dígitos) = 2 + (42 mod 8) = 4; soma = 4;
        // resto = 4 % 11 = 4 (>= 2) => dígito = 11 - 4 = 7.
        val base43 = "1" + "0".repeat(42)
        assertEquals(7, AccessKeyValidator.calculateCheckDigit(base43))
        assertTrue(AccessKeyValidator.isValid(base43 + "7"))
        assertFalse(AccessKeyValidator.isValid(base43 + "8"))
    }

    @Test
    fun `wrong length is never valid`() {
        assertFalse(AccessKeyValidator.isValid("123"))
        assertFalse(AccessKeyValidator.isValid("0".repeat(43)))
        assertFalse(AccessKeyValidator.isValid("0".repeat(45)))
    }

    @Test
    fun `non-digit characters are stripped before length check`() {
        val key = "0".repeat(44)
        val withPunctuation = key.chunked(4).joinToString(".")
        assertTrue(AccessKeyValidator.isValid(withPunctuation))
    }
}
