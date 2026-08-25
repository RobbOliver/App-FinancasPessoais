package com.robson.financas.util

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateFormatter {
    private val ptBr: Locale = Locale.Builder().setLanguage("pt").setRegion("BR").build()
    private val shortDate = DateTimeFormatter.ofPattern("dd/MM/yyyy", ptBr)
    private val dayMonth = DateTimeFormatter.ofPattern("dd 'de' MMMM", ptBr)
    private val monthYear = DateTimeFormatter.ofPattern("MMMM 'de' yyyy", ptBr)
    private val monthAbbrev = DateTimeFormatter.ofPattern("MMM", ptBr)

    fun formatShort(date: LocalDate): String = date.format(shortDate)

    fun formatDayMonth(date: LocalDate): String = date.format(dayMonth)

    fun formatMonthYear(yearMonth: YearMonth): String =
        yearMonth.format(monthYear).replaceFirstChar { it.uppercase() }

    fun formatMonthAbbrev(yearMonth: YearMonth): String =
        yearMonth.format(monthAbbrev).replaceFirstChar { it.uppercase() }
}
