package com.robson.financas.notifications.parser

object PixTextUtils {
    private val amountRegex = Regex("""R\$\s?(\d{1,3}(?:\.\d{3})*,\d{2}|\d+,\d{2})""")

    fun extractAmountCents(text: String): Long? {
        val match = amountRegex.find(text) ?: return null
        val normalized = match.groupValues[1]
            .replace(".", "")
            .replace(",", ".")
        val reais = normalized.toDoubleOrNull() ?: return null
        return Math.round(reais * 100)
    }

    fun extractNameAfter(text: String, vararg markers: String): String? {
        for (marker in markers) {
            val idx = text.indexOf(marker, ignoreCase = true)
            if (idx == -1) continue
            val rest = text.substring(idx + marker.length).trim()
            val name = rest.takeWhile { it != '.' && it != ',' }.trim()
            if (name.isNotBlank()) return name
        }
        return null
    }
}
