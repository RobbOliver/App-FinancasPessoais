package com.robson.financas.data.local.relation.fiscal

import androidx.room.Embedded
import com.robson.financas.data.local.entity.fiscal.PurchaseItemEntity

data class PurchaseItemWithDetails(
    @Embedded val item: PurchaseItemEntity,
    val establishmentName: String?,
    val categoryName: String?,
    val subcategoryName: String?,
    val microcategoryName: String?,
    val productBrand: String?,
    val productGenericName: String?,
)
