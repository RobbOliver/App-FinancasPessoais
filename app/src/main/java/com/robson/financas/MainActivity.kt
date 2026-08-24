package com.robson.financas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.robson.financas.ui.navigation.FinanceNavHost
import com.robson.financas.ui.theme.FinancasPessoaisTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinancasPessoaisTheme {
                FinanceNavHost()
            }
        }
    }
}
