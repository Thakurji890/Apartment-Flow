package com.example.feature.auth.presentation.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToWelcome: () -> Unit,
    onNavigateToApartmentSetup: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val destination = viewModel.destination.collectAsState().value

    LaunchedEffect(destination) {
        if (destination != SplashDestination.None) {
            delay(1500) // Simulate loading/branding time
            when (destination) {
                is SplashDestination.Onboarding -> onNavigateToOnboarding()
                is SplashDestination.Welcome -> onNavigateToWelcome()
                is SplashDestination.ApartmentSetup -> onNavigateToApartmentSetup()
                is SplashDestination.Dashboard -> onNavigateToDashboard()
                else -> {}
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "ApartmentFlow",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}
