package com.robson.financas.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    private val ptBr: Locale = Locale.Builder().setLanguage("pt").setRegion("BR").build()
    private val brFormat: NumberFormat = NumberFormat.getCurrencyInstance(ptBr)

    fun formatCents(cents: Long): String = brFormat.format(cents / 100.0)
}
