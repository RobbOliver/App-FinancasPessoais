package com.robson.financas.data.local.relation

import androidx.room.Embedded
import com.robson.financas.data.local.entity.CreditCardPurchaseEntity

data class CreditCardPurchaseWithCategory(
    @Embedded val purchase: CreditCardPurchaseEntity,
    val categoryName: String?,
    val categoryIcon: String?,
    val categoryColorHex: String?,
)
