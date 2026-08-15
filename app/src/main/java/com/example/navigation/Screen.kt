package com.example.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Welcome : Screen("welcome")
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object ForgotPassword : Screen("forgot_password")
    object EmailVerification : Screen("email_verification")

    object Onboarding : Screen("onboarding")
    object ApartmentSetup : Screen("apartment_setup")
    object CreateApartment : Screen("create_apartment")
    object JoinApartment : Screen("join_apartment")
    object ApartmentList : Screen("apartment_list")

    object HomeDashboard : Screen("home_dashboard/{apartmentId}") {
        fun createRoute(apartmentId: String) = "home_dashboard/$apartmentId"
    }

    object ApartmentDetails : Screen("apartment_details/{apartmentId}") {
        fun createRoute(apartmentId: String) = "apartment_details/$apartmentId"
    }

    object AddMember : Screen("add_member/{apartmentId}") {
        fun createRoute(apartmentId: String) = "add_member/$apartmentId"
    }

    object ExpenseList : Screen("expense_list/{apartmentId}") {
        fun createRoute(apartmentId: String) = "expense_list/$apartmentId"
    }
    
    object AddExpense : Screen("add_expense/{apartmentId}") {
        fun createRoute(apartmentId: String) = "add_expense/$apartmentId"
    }
    
    object EditExpense : Screen("edit_expense/{apartmentId}/{expenseId}") {
        fun createRoute(apartmentId: String, expenseId: String) = "edit_expense/$apartmentId/$expenseId"
    }
    
    object ExpenseDetails : Screen("expense_details/{apartmentId}/{expenseId}") {
        fun createRoute(apartmentId: String, expenseId: String) = "expense_details/$apartmentId/$expenseId"
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

    object RecurringBillList : Screen("recurring_bill_list/{apartmentId}") {
        fun createRoute(apartmentId: String) = "recurring_bill_list/$apartmentId"
    }

    object AddRecurringBill : Screen("add_recurring_bill/{apartmentId}") {
        fun createRoute(apartmentId: String) = "add_recurring_bill/$apartmentId"
    }

    object EditRecurringBill : Screen("edit_recurring_bill/{apartmentId}/{billId}") {
        fun createRoute(apartmentId: String, billId: String) = "edit_recurring_bill/$apartmentId/$billId"
    }

    object RecurringBillDetails : Screen("recurring_bill_details/{apartmentId}/{billId}") {
        fun createRoute(apartmentId: String, billId: String) = "recurring_bill_details/$apartmentId/$billId"
    }

    object ShoppingLists : Screen("shopping_lists/{apartmentId}") {
        fun createRoute(apartmentId: String) = "shopping_lists/$apartmentId"
    }

    object AddEditShoppingList : Screen("add_edit_shopping_list/{apartmentId}?listId={listId}") {
        fun createRoute(apartmentId: String, listId: String? = null) = 
            "add_edit_shopping_list/$apartmentId" + (listId?.let { "?listId=$it" } ?: "")
    }

    object ShoppingListDetails : Screen("shopping_list_details/{apartmentId}/{listId}") {
        fun createRoute(apartmentId: String, listId: String) = "shopping_list_details/$apartmentId/$listId"
    }

    object AddEditShoppingItem : Screen("add_edit_shopping_item/{apartmentId}/{listId}?itemId={itemId}") {
        fun createRoute(apartmentId: String, listId: String, itemId: String? = null) = 
            "add_edit_shopping_item/$apartmentId/$listId" + (itemId?.let { "?itemId=$it" } ?: "")
    }

    object PurchaseItem : Screen("purchase_item/{apartmentId}/{listId}/{itemId}") {
        fun createRoute(apartmentId: String, listId: String, itemId: String) = 
            "purchase_item/$apartmentId/$listId/$itemId"
    }

    object ChoreList : Screen("chore_list/{apartmentId}") {
        fun createRoute(apartmentId: String) = "chore_list/$apartmentId"
    }

    object AddEditChore : Screen("add_edit_chore/{apartmentId}?choreId={choreId}") {
        fun createRoute(apartmentId: String, choreId: String? = null) = 
            "add_edit_chore/$apartmentId" + (choreId?.let { "?choreId=$it" } ?: "")
    }

    object ChoreDetails : Screen("chore_details/{apartmentId}/{choreId}") {
        fun createRoute(apartmentId: String, choreId: String) = "chore_details/$apartmentId/$choreId"
    }

    object Notifications : Screen("notifications")
    object Analytics : Screen("analytics/{apartmentId}") {
        fun createRoute(apartmentId: String) = "analytics/$apartmentId"
    }
    object FairnessSummary : Screen("fairness_summary/{apartmentId}") {
        fun createRoute(apartmentId: String) = "fairness_summary/$apartmentId"
    }
}
