package com.robson.financas.data.local.relation

import androidx.room.Embedded
import com.robson.financas.data.local.entity.TransactionEntity

data class TransactionWithDetails(
    @Embedded val transaction: TransactionEntity,
    val accountName: String,
    val transferToAccountName: String?,
    val categoryName: String?,
    val categoryIcon: String?,
    val categoryColorHex: String?,
)
