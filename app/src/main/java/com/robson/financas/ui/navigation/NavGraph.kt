package com.robson.financas.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import com.robson.financas.ui.creditcards.AddEditCreditCardScreen
import com.robson.financas.ui.creditcards.AddPurchaseScreen
import com.robson.financas.ui.creditcards.CreditCardDetailScreen
import com.robson.financas.ui.creditcards.CreditCardsScreen
import com.robson.financas.ui.dashboard.DashboardScreen
import com.robson.financas.ui.fiscal.documents.FiscalDocumentDetailScreen
import com.robson.financas.ui.fiscal.documents.FiscalDocumentsScreen
import com.robson.financas.ui.fiscal.importing.ImportScreen
import com.robson.financas.ui.goals.GoalsScreen
import com.robson.financas.ui.more.MoreScreen
import com.robson.financas.ui.objectives.AddEditObjectiveScreen
import com.robson.financas.ui.objectives.ObjectiveDetailScreen
import com.robson.financas.ui.objectives.ObjectivesScreen
import com.robson.financas.ui.settings.SettingsScreen
import com.robson.financas.ui.tags.TagsScreen
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
            enterTransition = { fadeIn(tween(220)) + slideInHorizontally(tween(220)) { it / 10 } },
            exitTransition = { fadeOut(tween(160)) },
            popEnterTransition = { fadeIn(tween(220)) },
            popExitTransition = { fadeOut(tween(160)) + slideOutHorizontally(tween(160)) { it / 10 } },
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
                    onOpenCreditCards = { navController.navigate(Screen.CreditCards.route) },
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
                    onUseAsTemplate = { id -> navController.navigate(Screen.AddEditTransaction.routeFor(templateId = id)) },
                )
            }
            composable(
                route = Screen.AddEditTransaction.route,
                arguments = listOf(
                    navArgument(Screen.AddEditTransaction.ARG_TRANSACTION_ID) {
                        type = NavType.LongType
                        defaultValue = -1L
                    },
                    navArgument(Screen.AddEditTransaction.ARG_TEMPLATE_ID) {
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
                    onNavigateToTags = { navController.navigate(Screen.Tags.route) },
                    onNavigateToObjectives = { navController.navigate(Screen.Objectives.route) },
                    onNavigateToCreditCards = { navController.navigate(Screen.CreditCards.route) },
                    onNavigateToFiscalDocuments = { navController.navigate(Screen.FiscalDocuments.route) },
                )
            }
            composable(Screen.Tags.route) {
                TagsScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.Objectives.route) {
                ObjectivesScreen(
                    onBack = { navController.popBackStack() },
                    onAddObjective = { navController.navigate(Screen.AddEditObjective.routeFor()) },
                    onOpenObjective = { id -> navController.navigate(Screen.ObjectiveDetail.routeFor(id)) },
                )
            }
            composable(
                route = Screen.AddEditObjective.route,
                arguments = listOf(
                    navArgument(Screen.AddEditObjective.ARG_OBJECTIVE_ID) {
                        type = NavType.LongType
                        defaultValue = -1L
                    },
                ),
            ) {
                AddEditObjectiveScreen(onBack = { navController.popBackStack() })
            }
            composable(
                route = Screen.ObjectiveDetail.route,
                arguments = listOf(
                    navArgument(Screen.ObjectiveDetail.ARG_OBJECTIVE_ID) { type = NavType.LongType },
                ),
            ) {
                ObjectiveDetailScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.CreditCards.route) {
                CreditCardsScreen(
                    onBack = { navController.popBackStack() },
                    onAddCard = { navController.navigate(Screen.AddEditCreditCard.routeFor()) },
                    onOpenCard = { id -> navController.navigate(Screen.CreditCardDetail.routeFor(id)) },
                )
            }
            composable(
                route = Screen.AddEditCreditCard.route,
                arguments = listOf(
                    navArgument(Screen.AddEditCreditCard.ARG_CARD_ID) {
                        type = NavType.LongType
                        defaultValue = -1L
                    },
                ),
            ) {
                AddEditCreditCardScreen(onBack = { navController.popBackStack() })
            }
            composable(
                route = Screen.CreditCardDetail.route,
                arguments = listOf(
                    navArgument(Screen.CreditCardDetail.ARG_CARD_ID) { type = NavType.LongType },
                ),
            ) { backStackEntry ->
                val cardId = backStackEntry.arguments?.getLong(Screen.CreditCardDetail.ARG_CARD_ID) ?: 0L
                CreditCardDetailScreen(
                    onBack = { navController.popBackStack() },
                    onAddPurchase = { navController.navigate(Screen.AddPurchase.routeFor(cardId)) },
                )
            }
            composable(
                route = Screen.AddPurchase.route,
                arguments = listOf(
                    navArgument(Screen.AddPurchase.ARG_CARD_ID) { type = NavType.LongType },
                ),
            ) {
                AddPurchaseScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.FiscalDocuments.route) {
                FiscalDocumentsScreen(
                    onBack = { navController.popBackStack() },
                    onAddDocument = { navController.navigate(Screen.FiscalImport.route) },
                    onOpenDocument = { id -> navController.navigate(Screen.FiscalDocumentDetail.routeFor(id)) },
                )
            }
            composable(Screen.FiscalImport.route) {
                ImportScreen(
                    onBack = { navController.popBackStack() },
                    onImported = { id ->
                        navController.navigate(Screen.FiscalDocumentDetail.routeFor(id)) {
                            popUpTo(Screen.FiscalDocuments.route)
                        }
                    },
                )
            }
            composable(
                route = Screen.FiscalDocumentDetail.route,
                arguments = listOf(
                    navArgument(Screen.FiscalDocumentDetail.ARG_DOCUMENT_ID) { type = NavType.LongType },
                ),
            ) {
                FiscalDocumentDetailScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
