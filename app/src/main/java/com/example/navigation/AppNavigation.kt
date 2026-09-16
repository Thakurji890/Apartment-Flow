package com.example.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.feature.apartmentmanager.ui.ApartmentManagementScreen
import com.example.feature.expense.presentation.add.AddExpenseScreen
import com.example.feature.onboarding.ui.WelcomeScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Welcome.route
    ) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onGetStartedClick = {
                    navController.navigate(Screen.ApartmentManager.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.ApartmentManager.route) {
            ApartmentManagementScreen(
                onNavigateToAddExpense = {
                    navController.navigate(Screen.AddExpense.route)
                }
            )
        }
        composable(Screen.AddExpense.route) {
            AddExpenseScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onExpenseAdded = {
                    navController.popBackStack()
                }
            )
        }
    }
}
