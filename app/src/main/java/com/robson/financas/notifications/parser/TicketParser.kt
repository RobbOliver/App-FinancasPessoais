package com.robson.financas.notifications.parser

import com.robson.financas.data.local.entity.TransactionType

/**
 * Best-effort — see NubankParser's kdoc. Ticket (Edenred) purchase notifications are
 * always an outgoing vale-alimentação/refeição/car spend, so this always returns EXPENSE.
 */
object TicketParser : NotificationParser {
    override fun parse(title: String?, text: String?): ParsedPayment? {
        val full = "${title.orEmpty()} ${text.orEmpty()}"
        val amountCents = PixTextUtils.extractAmountCents(full) ?: return null

        return ParsedPayment(
            type = TransactionType.EXPENSE,
            amountCents = amountCents,
            counterpartyName = PixTextUtils.extractNameAfter(full, "em "),
        )
    }
}
