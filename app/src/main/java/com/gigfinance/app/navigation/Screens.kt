package com.gigfinance.app.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object SmartFinanceAI : Screen("smart_finance_ai")
    object AddTransaction : Screen("add_transaction?transactionId={transactionId}") {
        fun createRoute(transactionId: Int = -1) = "add_transaction?transactionId=$transactionId"
    }
}
