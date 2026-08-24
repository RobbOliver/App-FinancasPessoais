package com.robson.financas.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateFormatter {
    private val ptBr: Locale = Locale.Builder().setLanguage("pt").setRegion("BR").build()
    private val shortDate = DateTimeFormatter.ofPattern("dd/MM/yyyy", ptBr)
    private val dayMonth = DateTimeFormatter.ofPattern("dd 'de' MMMM", ptBr)

    fun formatShort(date: LocalDate): String = date.format(shortDate)

    fun formatDayMonth(date: LocalDate): String = date.format(dayMonth)
}
