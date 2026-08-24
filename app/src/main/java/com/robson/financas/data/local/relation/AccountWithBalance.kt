package com.robson.financas.data.local.relation

import androidx.room.Embedded
import com.robson.financas.data.local.entity.AccountEntity

data class AccountWithBalance(
    @Embedded val account: AccountEntity,
    val balanceCents: Long,
)
