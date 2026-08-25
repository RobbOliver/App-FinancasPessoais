package com.robson.financas.notifications.parser

import com.robson.financas.data.local.entity.TransactionType

/**
 * Best-effort — see NubankParser's kdoc. Google Wallet NFC tap-to-pay notifications are
 * always an outgoing card payment, so this always returns EXPENSE.
 */
object GoogleWalletParser : NotificationParser {
    override fun parse(title: String?, text: String?): ParsedPayment? {
        val full = "${title.orEmpty()} ${text.orEmpty()}"
        val amountCents = PixTextUtils.extractAmountCents(full) ?: return null

        return ParsedPayment(
            type = TransactionType.EXPENSE,
            amountCents = amountCents,
            counterpartyName = PixTextUtils.extractNameAfter(full, "em ", "at "),
        )
    }
}
