package com.example.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Welcome : Screen("welcome")
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object ForgotPassword : Screen("forgot_password")
    object EmailVerification : Screen("email_verification")
    
    object ApartmentSetup : Screen("apartment_setup")
    object CreateApartment : Screen("create_apartment")
    object JoinApartment : Screen("join_apartment")
    object ApartmentList : Screen("apartment_list") // Placeholder
    object ApartmentDetails : Screen("apartment_details/{apartmentId}") {
        fun createRoute(apartmentId: String) = "apartment_details/$apartmentId"
    }

    object ExpenseList : Screen("expense_list/{apartmentId}") {
        fun createRoute(apartmentId: String) = "expense_list/$apartmentId"
    }
    
    object AddExpense : Screen("add_expense/{apartmentId}") {
        fun createRoute(apartmentId: String) = "add_expense/$apartmentId"
    }
    
    object ExpenseDetails : Screen("expense_details/{apartmentId}/{expenseId}") {
        fun createRoute(apartmentId: String, expenseId: String) = "expense_details/$apartmentId/$expenseId"
    }
    
    object EditExpense : Screen("edit_expense/{apartmentId}/{expenseId}") {
        fun createRoute(apartmentId: String, expenseId: String) = "edit_expense/$apartmentId/$expenseId"
    }

    object SettlementList : Screen("settlement_list/{apartmentId}") {
        fun createRoute(apartmentId: String) = "settlement_list/$apartmentId"
    }
    
    object CreateSettlement : Screen("create_settlement/{apartmentId}") {
        fun createRoute(apartmentId: String) = "create_settlement/$apartmentId"
    }
    
    object SettlementDetails : Screen("settlement_details/{apartmentId}/{settlementId}") {
        fun createRoute(apartmentId: String, settlementId: String) = "settlement_details/$apartmentId/$settlementId"
    }

    object HomeDashboard : Screen("home_dashboard/{apartmentId}") {
        fun createRoute(apartmentId: String) = "home_dashboard/$apartmentId"
    }
}
