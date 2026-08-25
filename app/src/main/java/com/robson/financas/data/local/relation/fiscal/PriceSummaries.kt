package com.robson.financas.data.local.relation.fiscal

/** Agregados de preço de um produto — seção 11 do plano de arquitetura. */
data class ProductPriceSummary(
    val productId: Long,
    val minNormalizedCents: Long,
    val maxNormalizedCents: Long,
    val avgNormalizedCents: Long,
    val purchaseCount: Int,
)

/** Um ponto de comparação entre estabelecimentos para o mesmo produto. */
data class EstablishmentPricePoint(
    val establishmentId: Long?,
    val establishmentName: String?,
    val normalizedPriceCents: Long,
    val purchasedAt: java.time.LocalDate,
)
