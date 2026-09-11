package com.example.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.feature.apartmentmanager.ui.ApartmentManagementScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.ApartmentManager.route
    ) {
        composable(Screen.ApartmentManager.route) {
            ApartmentManagementScreen()
        }
    }
}
