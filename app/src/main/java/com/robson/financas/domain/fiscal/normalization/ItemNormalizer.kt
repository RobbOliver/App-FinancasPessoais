package com.robson.financas.domain.fiscal.normalization

import com.robson.financas.domain.fiscal.model.NormalizedProduct
import java.math.BigDecimal
import java.util.Locale

/**
 * Normalização determinística (seção 5) — sem ML. Separa marca, peso/volume e nome genérico da
 * descrição bruta do cupom, sem nunca usar isso como identidade da microcategoria (isso é
 * responsabilidade do [com.robson.financas.domain.fiscal.classification.ClassificationEngine]).
 */
object ItemNormalizer {

    // Marcas comuns de supermercado brasileiro — semente inicial; cresce com o uso (seção 5, passo 3).
    private val knownBrands = listOf(
        "OMO", "ARIEL", "BRILHANTE", "TIXAN", "YPE", "MINUANO",
        "NESTLE", "NESCAU", "NESCAFE", "TODDY", "TODDYNHO",
        "COCA COLA", "COCA-COLA", "PEPSI", "GUARANA ANTARCTICA", "GUARANA",
        "DANONE", "ACTIVIA", "VIGOR", "ITAMBE", "PARMALAT", "PIRACANJUBA",
        "SADIA", "PERDIGAO", "SEARA", "AURORA", "FRIBOI",
        "COLGATE", "ORAL B", "CLOSE UP", "REXONA", "DOVE", "NIVEA",
        "HEINZ", "HELLMANNS", "QUERO", "FUGINI",
        "PILAO", "MELITTA", "TRES CORACOES", "3 CORACOES",
        "CAMIL", "TIO JOAO", "PRATO FINO",
    ).sortedByDescending { it.length } // termos mais longos primeiro evita casar "3" antes de "3 CORACOES"

    private val weightVolumeRegex = Regex(
        """(\d+(?:[.,]\d+)?)\s*(KG|G|ML|L|LT)\b""",
        RegexOption.IGNORE_CASE,
    )

    fun normalize(rawDescription: String): NormalizedProduct {
        val upper = rawDescription.uppercase(Locale.ROOT).trim()

        val weightMatch = weightVolumeRegex.findAll(upper).lastOrNull()
        var weightGrams: Int? = null
        var volumeMl: Int? = null
        var withoutUnit = upper
        weightMatch?.let { match ->
            val amount = BigDecimal(match.groupValues[1].replace(',', '.'))
            when (match.groupValues[2].uppercase(Locale.ROOT)) {
                "KG" -> weightGrams = amount.multiply(BigDecimal(1000)).toInt()
                "G" -> weightGrams = amount.toInt()
                "L", "LT" -> volumeMl = amount.multiply(BigDecimal(1000)).toInt()
                "ML" -> volumeMl = amount.toInt()
            }
            withoutUnit = upper.removeRange(match.range).trim()
        }

        val brand = knownBrands.firstOrNull { withoutUnit.contains(it) }
        val withoutBrand = brand?.let { withoutUnit.replace(it, "").trim() } ?: withoutUnit

        val genericName = withoutBrand
            .replace(Regex("""\s+"""), " ")
            .trim()
            .ifBlank { withoutUnit.trim() }
            .toTitleCase()

        val normalizedName = listOfNotNull(genericName.takeIf { it.isNotBlank() }, brand?.toTitleCase())
            .joinToString(" ")
            .trim()
            .ifBlank { rawDescription.trim() }

        return NormalizedProduct(
            normalizedName = normalizedName,
            genericName = genericName,
            brand = brand?.toTitleCase(),
            weightGrams = weightGrams,
            volumeMl = volumeMl,
        )
    }

    private fun String.toTitleCase(): String =
        lowercase(Locale.ROOT).split(" ").joinToString(" ") { word ->
            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
        }
}
