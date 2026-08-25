package com.robson.financas.domain.fiscal.price

import java.util.Locale
import kotlin.math.roundToLong

data class NormalizedPrice(val normalizedPriceCents: Long, val normalizedUnit: String)

/**
 * Nunca compara apresentações diferentes diretamente — normaliza tudo para preço por kg, L ou
 * unidade antes de qualquer comparação (seção 11). Ex.: 1kg/R$8,00 → R$8,00/kg;
 * 5kg/R$35,00 → R$7,00/kg — o segundo é mais barato por quilo mesmo custando mais no total.
 */
object PriceNormalizer {
    fun normalize(unit: String, quantity: Double, totalCents: Long): NormalizedPrice {
        if (quantity <= 0.0) return NormalizedPrice(totalCents, "un")
        return when (unit.trim().uppercase(Locale.ROOT)) {
            "KG" -> NormalizedPrice((totalCents / quantity).roundToLong(), "kg")
            "G", "GR" -> NormalizedPrice(((totalCents / quantity) * 1000).roundToLong(), "kg")
            "L", "LT" -> NormalizedPrice((totalCents / quantity).roundToLong(), "l")
            "ML" -> NormalizedPrice(((totalCents / quantity) * 1000).roundToLong(), "l")
            else -> NormalizedPrice((totalCents / quantity).roundToLong(), "un")
        }
    }

    fun variationPercent(current: Long, previous: Long): Double =
        if (previous == 0L) 0.0 else ((current - previous).toDouble() / previous) * 100
}
