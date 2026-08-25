package com.robson.financas.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

object NotificationChannels {
    const val PENDING_TRANSACTIONS = "pending_transactions"

    fun createAll(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                PENDING_TRANSACTIONS,
                "Transações detectadas",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "Pix e pagamentos detectados automaticamente, aguardando confirmação"
            },
        )
    }
}
