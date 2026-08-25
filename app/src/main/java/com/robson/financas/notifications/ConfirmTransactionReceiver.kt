package com.robson.financas.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.robson.financas.data.repository.TransactionRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ConfirmTransactionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var transactionRepository: TransactionRepository

    override fun onReceive(context: Context, intent: Intent) {
        val transactionId = intent.getLongExtra(NotificationIntents.EXTRA_TRANSACTION_ID, -1L)
        val notificationId = intent.getIntExtra(NotificationIntents.EXTRA_NOTIFICATION_ID, -1)
        if (transactionId < 0) return

        NotificationManagerCompat.from(context).cancel(notificationId)

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                transactionRepository.getById(transactionId)?.let { transaction ->
                    transactionRepository.update(transaction.copy(needsReview = false))
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
