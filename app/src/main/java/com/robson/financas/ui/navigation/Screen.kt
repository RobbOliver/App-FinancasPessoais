package com.robson.financas.ui.navigation

sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")

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

    data object TransactionDetail : Screen("transactions/{transactionId}") {
        const val ARG_TRANSACTION_ID = "transactionId"
        fun routeFor(transactionId: Long) = "transactions/$transactionId"
    }

    data object AddEditTransaction :
        Screen("transactions/edit?transactionId={transactionId}&templateId={templateId}&fiscalDocumentId={fiscalDocumentId}") {
        const val ARG_TRANSACTION_ID = "transactionId"
        const val ARG_TEMPLATE_ID = "templateId"
        const val ARG_FISCAL_DOCUMENT_ID = "fiscalDocumentId"
        fun routeFor(transactionId: Long? = null, templateId: Long? = null, fiscalDocumentId: Long? = null) =
            "transactions/edit?transactionId=${transactionId ?: -1L}&templateId=${templateId ?: -1L}" +
                "&fiscalDocumentId=${fiscalDocumentId ?: -1L}"
    }

    data object Settings : Screen("settings")

    data object Goals : Screen("goals")

    data object GoalDetail : Screen("goals/{goalId}") {
        const val ARG_GOAL_ID = "goalId"
        fun routeFor(goalId: Long) = "goals/$goalId"
    }

    data object More : Screen("more")

    data object Tags : Screen("tags")

    data object Objectives : Screen("objectives")

    data object AddEditObjective : Screen("objectives/edit?objectiveId={objectiveId}") {
        const val ARG_OBJECTIVE_ID = "objectiveId"
        fun routeFor(objectiveId: Long? = null) = "objectives/edit?objectiveId=${objectiveId ?: -1L}"
    }

    data object ObjectiveDetail : Screen("objectives/detail/{objectiveId}") {
        const val ARG_OBJECTIVE_ID = "objectiveId"
        fun routeFor(objectiveId: Long) = "objectives/detail/$objectiveId"
    }

    data object CreditCards : Screen("creditcards")

    data object AddEditCreditCard : Screen("creditcards/edit?cardId={cardId}") {
        const val ARG_CARD_ID = "cardId"
        fun routeFor(cardId: Long? = null) = "creditcards/edit?cardId=${cardId ?: -1L}"
    }

    data object CreditCardDetail : Screen("creditcards/detail/{cardId}") {
        const val ARG_CARD_ID = "cardId"
        fun routeFor(cardId: Long) = "creditcards/detail/$cardId"
    }

    data object AddPurchase : Screen("creditcards/{cardId}/purchase") {
        const val ARG_CARD_ID = "cardId"
        fun routeFor(cardId: Long) = "creditcards/$cardId/purchase"
    }

    data object FiscalDocuments : Screen("fiscal")

    data object FiscalImport : Screen("fiscal/import")

    data object FiscalReview : Screen("fiscal/review")

    data object FiscalBudget : Screen("fiscal/budget")

    data object QrScanner : Screen("fiscal/scan")

    data object QrScanResult : Screen("fiscal/scan/result/{accessKey}?rawQr={rawQr}") {
        const val ARG_ACCESS_KEY = "accessKey"
        const val ARG_RAW_QR = "rawQr"
        fun routeFor(accessKey: String, rawQr: String) =
            "fiscal/scan/result/$accessKey?rawQr=${java.net.URLEncoder.encode(rawQr, "UTF-8")}"
    }

    data object AiExtraction : Screen("fiscal/scan/ai?rawQr={rawQr}") {
        const val ARG_RAW_QR = "rawQr"
        fun routeFor(rawQr: String) = "fiscal/scan/ai?rawQr=${java.net.URLEncoder.encode(rawQr, "UTF-8")}"
    }

    data object ProductPriceHistory : Screen("fiscal/products/{productId}") {
        const val ARG_PRODUCT_ID = "productId"
        fun routeFor(productId: Long) = "fiscal/products/$productId"
    }

    data object Brands : Screen("fiscal/brands")

    data object FiscalDocumentDetail : Screen("fiscal/{documentId}") {
        const val ARG_DOCUMENT_ID = "documentId"
        fun routeFor(documentId: Long) = "fiscal/$documentId"
    }
}
