package com.example.navigation

sealed class Screen(val route: String) {
    object ApartmentManager : Screen("apartment_manager")
}
