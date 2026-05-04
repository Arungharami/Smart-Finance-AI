package com.gigfinance.app.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gigfinance.app.repository.TransactionRepository
import com.gigfinance.app.ui.addtransaction.AddTransactionScreen
import com.gigfinance.app.ui.addtransaction.AddTransactionViewModel
import com.gigfinance.app.ui.dashboard.DashboardScreen
import com.gigfinance.app.ui.dashboard.DashboardViewModel
import com.gigfinance.app.ui.ai.SmartFinanceScreen
import com.gigfinance.app.ui.ai.SmartFinanceViewModel

@Composable
fun NavGraph(repository: TransactionRepository) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Dashboard.route) {
        composable(Screen.Dashboard.route) {
            val viewModel: DashboardViewModel = viewModel(
                factory = DashboardViewModel.Factory(repository)
            )
            DashboardScreen(
                viewModel = viewModel,
                onAddTransactionClick = {
                    navController.navigate(Screen.AddTransaction.createRoute())
                },
                onEditTransactionClick = { transactionId ->
                    navController.navigate(Screen.AddTransaction.createRoute(transactionId))
                },
                onAIClick = {
                    navController.navigate(Screen.SmartFinanceAI.route)
                }
            )
        }
        composable(Screen.SmartFinanceAI.route) {
            val viewModel: SmartFinanceViewModel = viewModel(
                factory = SmartFinanceViewModel.Factory(repository)
            )
            SmartFinanceScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(
            route = Screen.AddTransaction.route,
            arguments = listOf(navArgument("transactionId") { 
                type = NavType.IntType
                defaultValue = -1 
            })
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getInt("transactionId") ?: -1
            val viewModel: AddTransactionViewModel = viewModel(
                factory = AddTransactionViewModel.Factory(repository, transactionId)
            )
            AddTransactionScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
