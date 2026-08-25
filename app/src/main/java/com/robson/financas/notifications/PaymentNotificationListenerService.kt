package com.robson.financas.notifications

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.robson.financas.data.local.entity.CategoryType
import com.robson.financas.data.local.entity.TransactionEntity
import com.robson.financas.data.local.entity.TransactionSource
import com.robson.financas.data.local.entity.TransactionType
import com.robson.financas.data.repository.CategoryRepository
import com.robson.financas.data.repository.NotificationAppMappingRepository
import com.robson.financas.data.repository.TransactionRepository
import com.robson.financas.notifications.parser.NotificationParserRegistry
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@AndroidEntryPoint
class PaymentNotificationListenerService : NotificationListenerService() {

    @Inject
    lateinit var mappingRepository: NotificationAppMappingRepository

    @Inject
    lateinit var transactionRepository: TransactionRepository

    @Inject
    lateinit var categoryRepository: CategoryRepository

    @Inject
    lateinit var categorySuggestionEngine: CategorySuggestionEngine

    @Inject
    lateinit var notifier: PendingTransactionNotifier

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName !in MonitoredApps.packageNames) return

        val extras = sbn.notification.extras
        val title = extras.getCharSequence(android.app.Notification.EXTRA_TITLE)?.toString()
        val text = extras.getCharSequence(android.app.Notification.EXTRA_TEXT)?.toString()
        val bigText = extras.getCharSequence(android.app.Notification.EXTRA_BIG_TEXT)?.toString()

        Log.d(TAG, "packageName=${sbn.packageName} title=\"$title\" text=\"$text\" bigText=\"$bigText\"")

        val parser = NotificationParserRegistry.parserFor(sbn.packageName) ?: return
        val parsed = parser.parse(title, bigText ?: text) ?: return

        scope.launch {
            val mapping = mappingRepository.getByPackageName(sbn.packageName)
            if (mapping == null || !mapping.enabled) return@launch

            val categoryType = if (parsed.type == TransactionType.INCOME) CategoryType.INCOME else CategoryType.EXPENSE
            val categoryId = categorySuggestionEngine.suggestCategoryId(
                counterpartyName = parsed.counterpartyName,
                description = parsed.counterpartyName.orEmpty(),
                type = categoryType,
            )

            val transaction = TransactionEntity(
                type = parsed.type,
                amountCents = parsed.amountCents,
                accountId = mapping.accountId,
                categoryId = categoryId,
                date = LocalDate.now(),
                description = parsed.counterpartyName.orEmpty(),
                source = TransactionSource.NOTIFICATION,
                needsReview = true,
                counterpartyName = parsed.counterpartyName,
                rawNotificationText = bigText ?: text,
            )
            val transactionId = transactionRepository.create(transaction)

            val categoryName = categoryId?.let { categoryRepository.getById(it)?.name }
            notifier.notify(
                transactionId = transactionId,
                type = parsed.type,
                amountCents = parsed.amountCents,
                counterpartyName = parsed.counterpartyName,
                categoryName = categoryName,
            )
        }
    }

    companion object {
        private const val TAG = "PaymentNotifListener"
    }
}
