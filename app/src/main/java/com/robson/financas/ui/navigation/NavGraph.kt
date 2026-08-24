package com.robson.financas.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.robson.financas.ui.accounts.AccountsScreen
import com.robson.financas.ui.accounts.AddEditAccountScreen

@Composable
fun FinanceNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Accounts.route) {
        composable(Screen.Accounts.route) {
            AccountsScreen(
                onAddAccount = { navController.navigate(Screen.AddEditAccount.routeFor()) },
                onEditAccount = { id -> navController.navigate(Screen.AddEditAccount.routeFor(id)) },
            )
        }
        composable(
            route = Screen.AddEditAccount.route,
            arguments = listOf(
                navArgument(Screen.AddEditAccount.ARG_ACCOUNT_ID) {
                    type = NavType.LongType
                    defaultValue = -1L
                },
            ),
        ) {
            AddEditAccountScreen(onBack = { navController.popBackStack() })
        }
    }
}
