package com.example.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.feature.apartmentmanager.ui.ApartmentManagementScreen
import com.example.feature.expense.presentation.add.AddExpenseScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.ApartmentManager.route
    ) {
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
