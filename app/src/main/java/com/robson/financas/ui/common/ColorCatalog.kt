package com.robson.financas.ui.common

import androidx.compose.ui.graphics.Color

object ColorCatalog {
    val hexValues: List<String> = listOf(
        "#EF6C00", "#F9A825", "#2E7D32", "#00838F",
        "#1565C0", "#5E35B1", "#8E24AA", "#D81B60",
        "#C62828", "#6D4C41", "#455A64", "#757575",
    )

    fun toColor(hex: String): Color = try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: IllegalArgumentException) {
        Color(android.graphics.Color.parseColor(hexValues.first()))
    }
}
