package com.robson.financas

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.robson.financas.notifications.NotificationIntents
import com.robson.financas.ui.navigation.FinanceNavHost
import com.robson.financas.ui.theme.FinancasPessoaisTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermissionIfNeeded()

        var pendingEditTransactionId by mutableStateOf(extractEditTransactionId(intent))

        setContent {
            FinancasPessoaisTheme {
                FinanceNavHost(
                    pendingEditTransactionId = pendingEditTransactionId,
                    onPendingEditConsumed = { pendingEditTransactionId = null },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        recreate()
    }

    private fun extractEditTransactionId(intent: Intent?): Long? {
        val id = intent?.getLongExtra(NotificationIntents.EXTRA_EDIT_TRANSACTION_ID, -1L) ?: -1L
        return id.takeIf { it >= 0 }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (!granted) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
