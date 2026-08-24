package com.robson.financas.ui.navigation

sealed class Screen(val route: String) {
    data object Accounts : Screen("accounts")

    data object AddEditAccount : Screen("accounts/edit?accountId={accountId}") {
        const val ARG_ACCOUNT_ID = "accountId"
        fun routeFor(accountId: Long? = null) = "accounts/edit?accountId=${accountId ?: -1L}"
    }

    data object Categories : Screen("categories")

    data object AddEditCategory : Screen("categories/edit?categoryId={categoryId}") {
        const val ARG_CATEGORY_ID = "categoryId"
        fun routeFor(categoryId: Long? = null) = "categories/edit?categoryId=${categoryId ?: -1L}"
    }

    data object Transactions : Screen("transactions")

    data object AddEditTransaction : Screen("transactions/edit?transactionId={transactionId}") {
        const val ARG_TRANSACTION_ID = "transactionId"
        fun routeFor(transactionId: Long? = null) = "transactions/edit?transactionId=${transactionId ?: -1L}"
    }
}
