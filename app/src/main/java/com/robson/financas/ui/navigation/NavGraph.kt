package com.robson.financas.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.robson.financas.ui.accounts.AccountsScreen
import com.robson.financas.ui.accounts.AddEditAccountScreen
import com.robson.financas.ui.categories.AddEditCategoryScreen
import com.robson.financas.ui.categories.CategoriesScreen
import com.robson.financas.ui.dashboard.DashboardScreen
import com.robson.financas.ui.goals.GoalsScreen
import com.robson.financas.ui.more.MoreScreen
import com.robson.financas.ui.settings.SettingsScreen
import com.robson.financas.ui.transactions.AddEditTransactionScreen
import com.robson.financas.ui.transactions.TransactionsScreen

@Composable
fun FinanceNavHost(
    pendingEditTransactionId: Long? = null,
    onPendingEditConsumed: () -> Unit = {},
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val showBottomBar = backStackEntry?.destination?.route in bottomTabRoutes

    Scaffold(
        bottomBar = { if (showBottomBar) FinanceBottomNavBar(navController) },
    ) { scaffoldPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = androidx.compose.ui.Modifier.padding(scaffoldPadding),
        ) {
            composable(Screen.Dashboard.route) {
                LaunchedEffect(pendingEditTransactionId) {
                    if (pendingEditTransactionId != null) {
                        navController.navigate(Screen.AddEditTransaction.routeFor(pendingEditTransactionId))
                        onPendingEditConsumed()
                    }
                }
                DashboardScreen(
                    onAddTransaction = { navController.navigate(Screen.AddEditTransaction.routeFor()) },
                    onEditTransaction = { id -> navController.navigate(Screen.AddEditTransaction.routeFor(id)) },
                )
            }
            composable(Screen.Accounts.route) {
                AccountsScreen(
                    onBack = { navController.popBackStack() },
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
            composable(Screen.Categories.route) {
                CategoriesScreen(
                    onBack = { navController.popBackStack() },
                    onAddCategory = { navController.navigate(Screen.AddEditCategory.routeFor()) },
                    onEditCategory = { id -> navController.navigate(Screen.AddEditCategory.routeFor(id)) },
                )
            }
            composable(
                route = Screen.AddEditCategory.route,
                arguments = listOf(
                    navArgument(Screen.AddEditCategory.ARG_CATEGORY_ID) {
                        type = NavType.LongType
                        defaultValue = -1L
                    },
                ),
            ) {
                AddEditCategoryScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.Transactions.route) {
                TransactionsScreen(
                    onAddTransaction = { navController.navigate(Screen.AddEditTransaction.routeFor()) },
                    onEditTransaction = { id -> navController.navigate(Screen.AddEditTransaction.routeFor(id)) },
                )
            }
            composable(
                route = Screen.AddEditTransaction.route,
                arguments = listOf(
                    navArgument(Screen.AddEditTransaction.ARG_TRANSACTION_ID) {
                        type = NavType.LongType
                        defaultValue = -1L
                    },
                ),
            ) {
                AddEditTransactionScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.Settings.route) {
                SettingsScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.Goals.route) {
                GoalsScreen()
            }
            composable(Screen.More.route) {
                MoreScreen(
                    onNavigateToAccounts = { navController.navigate(Screen.Accounts.route) },
                    onNavigateToCategories = { navController.navigate(Screen.Categories.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                )
            }
        }
    }
}
