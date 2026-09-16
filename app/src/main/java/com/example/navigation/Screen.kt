package com.example.navigation

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object ApartmentManager : Screen("apartment_manager")
    object AddExpense : Screen("add_expense")
}
