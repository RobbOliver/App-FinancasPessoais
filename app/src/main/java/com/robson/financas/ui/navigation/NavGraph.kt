package com.robson.financas.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
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

@Composable
fun FinanceNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val showBottomBar = backStackEntry?.destination?.route in bottomTabRoutes

    Scaffold(
        bottomBar = { if (showBottomBar) FinanceBottomNavBar(navController) },
    ) { scaffoldPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Accounts.route,
            modifier = androidx.compose.ui.Modifier.padding(scaffoldPadding),
        ) {
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
            composable(Screen.Categories.route) {
                CategoriesScreen(
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
        }
    }
}
