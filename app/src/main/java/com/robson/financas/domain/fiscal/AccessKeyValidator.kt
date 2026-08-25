package com.robson.financas.domain.fiscal

/**
 * Valida a chave de acesso de 44 dígitos de uma NF-e/NFC-e pelo dígito verificador (módulo 11)
 * — checagem local, sem rede, antes de qualquer consulta ao portal da Sefaz (fluxo, passo 3).
 */
object AccessKeyValidator {

    fun isValid(accessKey: String): Boolean {
        val digits = accessKey.filter { it.isDigit() }
        if (digits.length != 44) return false
        val base = digits.substring(0, 43)
        val expectedCheckDigit = digits.last() - '0'
        return calculateCheckDigit(base) == expectedCheckDigit
    }

    /** [base43] deve ter exatamente 43 dígitos. Pesos 2..9 cíclicos, da direita para a esquerda. */
    fun calculateCheckDigit(base43: String): Int {
        require(base43.length == 43) { "Chave base deve ter 43 dígitos, tinha ${base43.length}" }
        var sum = 0
        var weight = 2
        for (i in base43.indices.reversed()) {
            sum += (base43[i] - '0') * weight
            weight = if (weight == 9) 2 else weight + 1
        }
        val remainder = sum % 11
        return if (remainder < 2) 0 else 11 - remainder
    }
}
