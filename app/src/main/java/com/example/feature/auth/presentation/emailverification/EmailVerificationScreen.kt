package com.example.feature.auth.presentation.emailverification

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.ui.components.FullScreenLoader
import com.example.ui.components.PrimaryButton
import com.example.ui.components.SecondaryButton
import com.example.ui.components.StandardTextButton
import com.example.ui.theme.LocalSpacing
import kotlinx.coroutines.flow.collectLatest

@Composable
fun EmailVerificationScreen(
    onNavigateToWelcome: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    viewModel: EmailVerificationViewModel = hiltViewModel()
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val spacing = LocalSpacing.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is EmailVerificationEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is EmailVerificationEvent.VerificationSuccess -> {
                    onNavigateToDashboard()
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = spacing.large),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.MarkEmailUnread,
                    contentDescription = null,
                    modifier = Modifier.size(100.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(spacing.large))
                
                Text(
                    text = "Verify your email",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Spacer(modifier = Modifier.height(spacing.medium))
                
                Text(
                    text = "We've sent an email to your address. Please verify your email to continue.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(spacing.extraLarge))
                
                PrimaryButton(
                    text = "I've verified my email",
                    onClick = viewModel::checkVerificationStatus,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )
                
                Spacer(modifier = Modifier.height(spacing.medium))
                
                SecondaryButton(
                    text = "Resend Email",
                    onClick = viewModel::resendVerificationEmail,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )
                
                Spacer(modifier = Modifier.height(spacing.extraLarge))
                
                StandardTextButton(
                    text = "Logout",
                    onClick = { viewModel.signOut(onNavigateToWelcome) }
                )
            }

            if (isLoading) {
                FullScreenLoader()
            }
        }
    }
}
