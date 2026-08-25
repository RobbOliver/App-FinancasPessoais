package com.robson.financas.notifications.parser

import com.robson.financas.data.local.entity.TransactionType

/** Best-effort — see NubankParser's kdoc; not yet validated against a real captured notification. */
object PicPayParser : NotificationParser {
    override fun parse(title: String?, text: String?): ParsedPayment? {
        val full = "${title.orEmpty()} ${text.orEmpty()}"
        val amountCents = PixTextUtils.extractAmountCents(full) ?: return null

        val lower = full.lowercase()
        return when {
            "recebeu" in lower || "recebido" in lower -> ParsedPayment(
                type = TransactionType.INCOME,
                amountCents = amountCents,
                counterpartyName = PixTextUtils.extractNameAfter(full, "de "),
            )
            "pagamento" in lower || "enviou" in lower || "compra" in lower -> ParsedPayment(
                type = TransactionType.EXPENSE,
                amountCents = amountCents,
                counterpartyName = PixTextUtils.extractNameAfter(full, "para ", "em "),
            )
            else -> null
        }
    }
}
