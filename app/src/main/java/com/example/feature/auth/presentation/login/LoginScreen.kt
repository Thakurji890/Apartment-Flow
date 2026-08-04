package com.example.feature.auth.presentation.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.ui.components.FullScreenLoader
import com.example.ui.components.PasswordTextField
import com.example.ui.components.PrimaryButton
import com.example.ui.components.PrimaryTextField
import com.example.ui.components.SecondaryButton
import com.example.ui.components.StandardTextButton
import com.example.ui.components.StandardTopAppBar
import com.example.ui.theme.LocalSpacing
import kotlinx.coroutines.flow.collectLatest

@Composable
fun LoginScreen(
    onNavigateBack: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToEmailVerification: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsState().value
    val spacing = LocalSpacing.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is UiEvent.LoginSuccess -> {
                    if (event.isEmailVerified) {
                        onNavigateToDashboard()
                    } else {
                        onNavigateToEmailVerification()
                    }
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            StandardTopAppBar(title = "", onNavigateBack = onNavigateBack)
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = spacing.large)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(spacing.extraLarge))
                Text(
                    text = "Welcome Back",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(spacing.small))
                Text(
                    text = "Login to your account",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(spacing.extraLarge))

                PrimaryTextField(
                    value = state.email,
                    onValueChange = { viewModel.onEvent(LoginEvent.EmailChanged(it)) },
                    label = "Email Address",
                    isError = state.emailError != null,
                    errorMessage = state.emailError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(spacing.medium))

                PasswordTextField(
                    value = state.password,
                    onValueChange = { viewModel.onEvent(LoginEvent.PasswordChanged(it)) },
                    label = "Password",
                    isError = state.passwordError != null,
                    errorMessage = state.passwordError,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = state.rememberMe,
                            onCheckedChange = { viewModel.onEvent(LoginEvent.RememberMeChanged(it)) }
                        )
                        Text(text = "Remember me", style = MaterialTheme.typography.bodyMedium)
                    }
                    StandardTextButton(
                        text = "Forgot Password?",
                        onClick = onNavigateToForgotPassword
                    )
                }

                Spacer(modifier = Modifier.height(spacing.large))

                PrimaryButton(
                    text = "Login",
                    onClick = { viewModel.onEvent(LoginEvent.Submit) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isLoading
                )
                
                Spacer(modifier = Modifier.height(spacing.medium))
                
                Text(text = "OR", style = MaterialTheme.typography.labelMedium)
                
                Spacer(modifier = Modifier.height(spacing.medium))
                
                SecondaryButton(
                    text = "Sign in with Google",
                    onClick = { /* TODO: Launch Google Credential Manager Flow and pass token to viewModel.onEvent(LoginEvent.SubmitGoogleSignIn(idToken)) */ },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isLoading
                )
            }

            if (state.isLoading) {
                FullScreenLoader()
            }
        }
    }
}
