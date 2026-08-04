package com.example.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

import com.example.feature.auth.presentation.emailverification.EmailVerificationScreen
import com.example.feature.auth.presentation.forgotpassword.ForgotPasswordScreen
import com.example.feature.auth.presentation.login.LoginScreen
import com.example.feature.auth.presentation.onboarding.OnboardingScreen
import com.example.feature.auth.presentation.signup.SignUpScreen
import com.example.feature.auth.presentation.splash.SplashScreen
import com.example.feature.auth.presentation.welcome.WelcomeScreen

import com.example.feature.apartment.presentation.setup.ApartmentSetupScreen
import com.example.feature.apartment.presentation.create.CreateApartmentScreen
import com.example.feature.apartment.presentation.join.JoinApartmentScreen
import com.example.feature.apartment.presentation.details.DashboardScreen
import com.example.feature.apartment.presentation.details.ApartmentDetailsScreen

import com.example.feature.expense.presentation.list.ExpenseListScreen
import com.example.feature.expense.presentation.add.AddExpenseScreen
import com.example.feature.expense.presentation.details.ExpenseDetailsScreen
import com.example.feature.expense.presentation.edit.EditExpenseScreen

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
                    navController.navigate(Screen.Dashboard.route) {
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
                    navController.navigate(Screen.Dashboard.route) {
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
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.ApartmentSetup.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.JoinApartment.route) {
            JoinApartmentScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDashboard = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.ApartmentSetup.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToApartmentSetup = { navController.navigate(Screen.ApartmentSetup.route) },
                onNavigateToApartmentDetails = { apartmentId ->
                    navController.navigate(Screen.ApartmentDetails.createRoute(apartmentId))
                },
                onNavigateToExpenses = { apartmentId ->
                    navController.navigate(Screen.ExpenseList.createRoute(apartmentId))
                }
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
    }
}
