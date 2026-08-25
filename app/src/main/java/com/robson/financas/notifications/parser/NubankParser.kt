package com.robson.financas.notifications.parser

import com.robson.financas.data.local.entity.TransactionType

/**
 * Best-effort — written from publicly documented Nubank Pix notification wording,
 * not yet validated against a real captured notification. Adjust once real samples
 * arrive via PaymentNotificationListenerService's logcat output.
 */
object NubankParser : NotificationParser {
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
            "enviou" in lower || "enviado" in lower || "pagamento" in lower || "compra" in lower -> ParsedPayment(
                type = TransactionType.EXPENSE,
                amountCents = amountCents,
                counterpartyName = PixTextUtils.extractNameAfter(full, "para ", "em "),
            )
            else -> null
        }
    }
}
