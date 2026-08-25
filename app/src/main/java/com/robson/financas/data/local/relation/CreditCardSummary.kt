package com.robson.financas.data.local.relation

import androidx.room.Embedded
import com.robson.financas.data.local.entity.CreditCardEntity

data class CreditCardSummary(
    @Embedded val card: CreditCardEntity,
    val invoiceTotalCents: Long,
    val invoicePaid: Boolean,
) {
    val limitUsageFraction: Float
        get() = if (card.limitCents > 0) (invoiceTotalCents.toFloat() / card.limitCents).coerceIn(0f, 1f) else 0f
}
