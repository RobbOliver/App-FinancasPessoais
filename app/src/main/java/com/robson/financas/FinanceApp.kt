package com.robson.financas

import android.app.Application
import com.robson.financas.notifications.NotificationChannels
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FinanceApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationChannels.createAll(this)
    }
}
