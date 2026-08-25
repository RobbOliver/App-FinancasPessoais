package com.robson.financas.notifications.parser

import com.robson.financas.data.local.entity.TransactionType

data class ParsedPayment(
    val type: TransactionType,
    val amountCents: Long,
    val counterpartyName: String?,
)

fun interface NotificationParser {
    fun parse(title: String?, text: String?): ParsedPayment?
}
