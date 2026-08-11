package com.example.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.feature.apartment.presentation.details.ApartmentDetailsScreen
import com.example.feature.apartment.presentation.details.ApartmentListScreen
import com.example.feature.apartment.presentation.setup.ApartmentSetupScreen
import com.example.feature.apartment.presentation.create.CreateApartmentScreen
import com.example.feature.apartment.presentation.join.JoinApartmentScreen
import com.example.feature.auth.presentation.emailverification.EmailVerificationScreen
import com.example.feature.auth.presentation.forgotpassword.ForgotPasswordScreen
import com.example.feature.auth.presentation.login.LoginScreen
import com.example.feature.auth.presentation.signup.SignUpScreen
import com.example.feature.expense.presentation.add.AddExpenseScreen
import com.example.feature.expense.presentation.edit.EditExpenseScreen
import com.example.feature.expense.presentation.details.ExpenseDetailsScreen
import com.example.feature.expense.presentation.list.ExpenseListScreen
import com.example.feature.auth.presentation.onboarding.OnboardingScreen
import com.example.feature.auth.presentation.splash.SplashScreen
import com.example.feature.auth.presentation.welcome.WelcomeScreen
import com.example.feature.settlement.presentation.create.CreateSettlementScreen
import com.example.feature.settlement.presentation.details.SettlementDetailsScreen
import com.example.feature.settlement.presentation.list.SettlementListScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToOnboarding = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToWelcome = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToApartmentSetup = {
                    navController.navigate(Screen.ApartmentSetup.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToDashboard = {
                    navController.navigate(Screen.ApartmentList.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onNavigateToWelcome = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) }
            )
        }
        
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) },
                onNavigateToDashboard = {
                    navController.navigate(Screen.ApartmentList.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onNavigateToEmailVerification = {
                    navController.navigate(Screen.EmailVerification.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.SignUp.route) {
            SignUpScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEmailVerification = {
                    navController.navigate(Screen.EmailVerification.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.EmailVerification.route) {
            EmailVerificationScreen(
                onNavigateToWelcome = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(Screen.EmailVerification.route) { inclusive = true }
                    }
                },
                onNavigateToDashboard = {
                    navController.navigate(Screen.Splash.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.ApartmentSetup.route) {
            ApartmentSetupScreen(
                onNavigateToCreate = { navController.navigate(Screen.CreateApartment.route) },
                onNavigateToJoin = { navController.navigate(Screen.JoinApartment.route) }
            )
        }
        
        composable(Screen.CreateApartment.route) {
            CreateApartmentScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDashboard = {
                    navController.navigate(Screen.ApartmentList.route) {
                        popUpTo(Screen.ApartmentSetup.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.JoinApartment.route) {
            JoinApartmentScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDashboard = {
                    navController.navigate(Screen.ApartmentList.route) {
                        popUpTo(Screen.ApartmentSetup.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.ApartmentList.route) {
            ApartmentListScreen(
                onNavigateToApartmentSetup = { navController.navigate(Screen.ApartmentSetup.route) },
                onNavigateToApartmentDetails = { apartmentId ->
                    navController.navigate(Screen.HomeDashboard.createRoute(apartmentId))
                },
                onNavigateToExpenses = { apartmentId ->
                    navController.navigate(Screen.ExpenseList.createRoute(apartmentId))
                },
                onNavigateToSettlements = { apartmentId ->
                    navController.navigate(Screen.SettlementList.createRoute(apartmentId))
                }
            )
        }

        composable(
            route = Screen.HomeDashboard.route,
            arguments = listOf(navArgument("apartmentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val apartmentId = backStackEntry.arguments?.getString("apartmentId") ?: ""
            com.example.feature.dashboard.presentation.dashboard.HomeDashboardScreen(
                onNavigateToExpenseDetails = { expenseId -> 
                    navController.navigate(Screen.ExpenseDetails.createRoute(apartmentId, expenseId)) 
                },
                onNavigateToAddExpense = { navController.navigate(Screen.AddExpense.createRoute(apartmentId)) },
                onNavigateToSettlements = { navController.navigate(Screen.SettlementList.createRoute(apartmentId)) },
                onNavigateToExpenses = { navController.navigate(Screen.ExpenseList.createRoute(apartmentId)) },
                onNavigateToApartment = { navController.navigate(Screen.ApartmentDetails.createRoute(apartmentId)) },
                onNavigateToMemberDetails = { /* TODO: Member details route */ },
                onNavigateToRecurringBills = { navController.navigate(Screen.RecurringBillList.createRoute(apartmentId)) },
                onNavigateToRecurringBillDetails = { billId -> navController.navigate(Screen.RecurringBillDetails.createRoute(apartmentId, billId)) },
                onNavigateToShoppingLists = { navController.navigate(Screen.ShoppingLists.createRoute(apartmentId)) }
            )
        }
        
        composable(
            route = Screen.ApartmentDetails.route,
            arguments = listOf(
                navArgument("apartmentId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            ApartmentDetailsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.ExpenseList.route,
            arguments = listOf(
                navArgument("apartmentId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val apartmentId = backStackEntry.arguments?.getString("apartmentId") ?: ""
            ExpenseListScreen(
                apartmentId = apartmentId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddExpense = {
                    navController.navigate(Screen.AddExpense.createRoute(apartmentId))
                },
                onNavigateToExpenseDetails = { expenseId ->
                    navController.navigate(Screen.ExpenseDetails.createRoute(apartmentId, expenseId))
                }
            )
        }

        composable(
            route = Screen.AddExpense.route,
            arguments = listOf(
                navArgument("apartmentId") { type = NavType.StringType }
            )
        ) {
            AddExpenseScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.ExpenseDetails.route,
            arguments = listOf(
                navArgument("apartmentId") { type = NavType.StringType },
                navArgument("expenseId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            ExpenseDetailsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEditExpense = { aptId, expId ->
                    navController.navigate(Screen.EditExpense.createRoute(aptId, expId))
                }
            )
        }

        composable(
            route = Screen.EditExpense.route,
            arguments = listOf(
                navArgument("apartmentId") { type = NavType.StringType },
                navArgument("expenseId") { type = NavType.StringType }
            )
        ) {
            EditExpenseScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.SettlementList.route,
            arguments = listOf(
                navArgument("apartmentId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val apartmentId = backStackEntry.arguments?.getString("apartmentId") ?: ""
            SettlementListScreen(
                onNavigateToCreate = { navController.navigate(Screen.CreateSettlement.createRoute(apartmentId)) },
                onNavigateToDetails = { settlementId ->
                    navController.navigate(Screen.SettlementDetails.createRoute(apartmentId, settlementId))
                }
            )
        }

        composable(
            route = Screen.CreateSettlement.route,
            arguments = listOf(
                navArgument("apartmentId") { type = NavType.StringType }
            )
        ) {
            CreateSettlementScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.SettlementDetails.route,
            arguments = listOf(
                navArgument("apartmentId") { type = NavType.StringType },
                navArgument("settlementId") { type = NavType.StringType }
            )
        ) {
            SettlementDetailsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(
            route = Screen.RecurringBillList.route,
            arguments = listOf(navArgument("apartmentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val apartmentId = backStackEntry.arguments?.getString("apartmentId") ?: ""
            com.example.feature.recurringbill.presentation.list.RecurringBillListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddBill = { navController.navigate(Screen.AddRecurringBill.createRoute(apartmentId)) },
                onNavigateToBillDetails = { billId -> navController.navigate(Screen.RecurringBillDetails.createRoute(apartmentId, billId)) }
            )
        }

        composable(
            route = Screen.AddRecurringBill.route,
            arguments = listOf(navArgument("apartmentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val apartmentId = backStackEntry.arguments?.getString("apartmentId") ?: ""
            com.example.feature.recurringbill.presentation.add_edit.AddEditRecurringBillScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.EditRecurringBill.route,
            arguments = listOf(
                navArgument("apartmentId") { type = NavType.StringType },
                navArgument("billId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val apartmentId = backStackEntry.arguments?.getString("apartmentId") ?: ""
            val billId = backStackEntry.arguments?.getString("billId") ?: ""
            com.example.feature.recurringbill.presentation.add_edit.AddEditRecurringBillScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.RecurringBillDetails.route,
            arguments = listOf(
                navArgument("apartmentId") { type = NavType.StringType },
                navArgument("billId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val apartmentId = backStackEntry.arguments?.getString("apartmentId") ?: ""
            val billId = backStackEntry.arguments?.getString("billId") ?: ""
            com.example.feature.recurringbill.presentation.details.RecurringBillDetailsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { navController.navigate(Screen.EditRecurringBill.createRoute(apartmentId, billId)) }
            )
        }

        composable(
            route = Screen.ShoppingLists.route,
            arguments = listOf(navArgument("apartmentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val apartmentId = backStackEntry.arguments?.getString("apartmentId") ?: ""
            com.example.feature.shopping.presentation.lists.ShoppingListsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToListDetails = { listId -> navController.navigate(Screen.ShoppingListDetails.createRoute(apartmentId, listId)) },
                onNavigateToAddList = { navController.navigate(Screen.AddEditShoppingList.createRoute(apartmentId)) }
            )
        }

        composable(
            route = Screen.ShoppingListDetails.route,
            arguments = listOf(
                navArgument("apartmentId") { type = NavType.StringType },
                navArgument("listId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val apartmentId = backStackEntry.arguments?.getString("apartmentId") ?: ""
            val listId = backStackEntry.arguments?.getString("listId") ?: ""
            com.example.feature.shopping.presentation.details.ShoppingListDetailsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddItem = { _ -> navController.navigate(Screen.AddEditShoppingItem.createRoute(apartmentId, listId)) },
                onNavigateToEditItem = { _, itemId -> navController.navigate(Screen.AddEditShoppingItem.createRoute(apartmentId, listId, itemId)) },
                onNavigateToPurchaseItem = { _, itemId -> navController.navigate(Screen.PurchaseItem.createRoute(apartmentId, listId, itemId)) }
            )
        }

        composable(
            route = Screen.AddEditShoppingList.route,
            arguments = listOf(
                navArgument("apartmentId") { type = NavType.StringType },
                navArgument("listId") { type = NavType.StringType; nullable = true }
            )
        ) { backStackEntry ->
            val apartmentId = backStackEntry.arguments?.getString("apartmentId") ?: ""
            com.example.feature.shopping.presentation.add_edit_list.AddEditShoppingListScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.AddEditShoppingItem.route,
            arguments = listOf(
                navArgument("apartmentId") { type = NavType.StringType },
                navArgument("listId") { type = NavType.StringType },
                navArgument("itemId") { type = NavType.StringType; nullable = true }
            )
        ) { backStackEntry ->
            val apartmentId = backStackEntry.arguments?.getString("apartmentId") ?: ""
            val listId = backStackEntry.arguments?.getString("listId") ?: ""
            com.example.feature.shopping.presentation.add_edit_item.AddEditShoppingItemScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.PurchaseItem.route,
            arguments = listOf(
                navArgument("apartmentId") { type = NavType.StringType },
                navArgument("listId") { type = NavType.StringType },
                navArgument("itemId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val apartmentId = backStackEntry.arguments?.getString("apartmentId") ?: ""
            val listId = backStackEntry.arguments?.getString("listId") ?: ""
            val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
            com.example.feature.shopping.presentation.purchase_item.PurchaseItemScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

