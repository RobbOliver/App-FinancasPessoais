package com.robson.financas.domain.fiscal.model

data class NormalizedProduct(
    val normalizedName: String,
    val genericName: String,
    val brand: String?,
    val weightGrams: Int?,
    val volumeMl: Int?,
)
