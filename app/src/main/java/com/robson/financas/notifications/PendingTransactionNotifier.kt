package com.robson.financas.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.robson.financas.MainActivity
import com.robson.financas.R
import com.robson.financas.data.local.entity.TransactionType
import com.robson.financas.util.CurrencyFormatter
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

object NotificationIntents {
    const val EXTRA_EDIT_TRANSACTION_ID = "edit_transaction_id"
    const val EXTRA_TRANSACTION_ID = "transaction_id"
    const val EXTRA_NOTIFICATION_ID = "notification_id"
    const val ACTION_CONFIRM_TRANSACTION = "com.robson.financas.action.CONFIRM_TRANSACTION"
}

@Singleton
class PendingTransactionNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun notify(
        transactionId: Long,
        type: TransactionType,
        amountCents: Long,
        counterpartyName: String?,
        categoryName: String?,
    ) {
        val notificationId = transactionId.toInt()

        val verb = if (type == TransactionType.INCOME) "recebido" else "enviado"
        val title = "Pix $verb: ${CurrencyFormatter.formatCents(amountCents)}"
        val text = buildString {
            if (!counterpartyName.isNullOrBlank()) append(counterpartyName)
            if (!categoryName.isNullOrBlank()) {
                if (isNotEmpty()) append(" · ")
                append(categoryName)
            }
            if (isEmpty()) append("Toque para revisar")
        }

        val confirmIntent = Intent(context, ConfirmTransactionReceiver::class.java).apply {
            action = NotificationIntents.ACTION_CONFIRM_TRANSACTION
            putExtra(NotificationIntents.EXTRA_TRANSACTION_ID, transactionId)
            putExtra(NotificationIntents.EXTRA_NOTIFICATION_ID, notificationId)
        }
        val confirmPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            confirmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val editIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(NotificationIntents.EXTRA_EDIT_TRANSACTION_ID, transactionId)
        }
        val editPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            editIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, NotificationChannels.PENDING_TRANSACTIONS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .setContentIntent(editPendingIntent)
            .addAction(0, "Confirmar", confirmPendingIntent)
            .addAction(0, "Editar", editPendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    fun cancel(transactionId: Long) {
        NotificationManagerCompat.from(context).cancel(transactionId.toInt())
    }
}
