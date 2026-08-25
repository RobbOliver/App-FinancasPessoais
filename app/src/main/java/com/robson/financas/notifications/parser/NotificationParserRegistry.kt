package com.robson.financas.notifications.parser

import com.robson.financas.notifications.MonitoredApps

object NotificationParserRegistry {
    private val parsers: Map<String, NotificationParser> = mapOf(
        MonitoredApps.NUBANK to NubankParser,
        MonitoredApps.PICPAY to PicPayParser,
        MonitoredApps.MERCADO_PAGO to MercadoPagoParser,
        MonitoredApps.GOOGLE_WALLET to GoogleWalletParser,
        MonitoredApps.TICKET to TicketParser,
    )

    fun parserFor(packageName: String): NotificationParser? = parsers[packageName]
}
