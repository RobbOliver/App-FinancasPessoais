package com.robson.financas.notifications

object MonitoredApps {
    const val NUBANK = "com.nu.production"
    const val PICPAY = "com.picpay"
    const val MERCADO_PAGO = "com.mercadopago.wallet"
    const val GOOGLE_WALLET = "com.google.android.apps.walletnfcrel"
    const val TICKET = "br.com.mobile.ticket"

    val packageNames: Set<String> = setOf(NUBANK, PICPAY, MERCADO_PAGO, GOOGLE_WALLET, TICKET)

    val displayNames: Map<String, String> = mapOf(
        NUBANK to "Nubank",
        PICPAY to "PicPay",
        MERCADO_PAGO to "Mercado Pago",
        GOOGLE_WALLET to "Google Wallet (NFC)",
        TICKET to "Ticket (Edenred)",
    )
}
