package com.robson.financas.notifications

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class PaymentNotificationListenerService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName !in MonitoredApps.packageNames) return

        val extras = sbn.notification.extras
        val title = extras.getCharSequence(android.app.Notification.EXTRA_TITLE)
        val text = extras.getCharSequence(android.app.Notification.EXTRA_TEXT)
        val bigText = extras.getCharSequence(android.app.Notification.EXTRA_BIG_TEXT)

        Log.d(
            TAG,
            "packageName=${sbn.packageName} title=\"$title\" text=\"$text\" bigText=\"$bigText\"",
        )
    }

    companion object {
        private const val TAG = "PaymentNotifListener"
    }
}
