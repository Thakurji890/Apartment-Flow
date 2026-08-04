package com.example.feature.auth.presentation.signup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.core.util.PasswordStrength
import com.example.ui.components.*
import com.example.ui.theme.LocalSpacing
import com.example.ui.theme.SuccessColor
import com.example.ui.theme.WarningColor
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SignUpScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEmailVerification: () -> Unit,
    viewModel: SignUpViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsState().value
    val spacing = LocalSpacing.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is SignUpUiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is SignUpUiEvent.SignUpSuccess -> {
                    onNavigateToEmailVerification()
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
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Create Account",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(spacing.extraLarge))

                PrimaryTextField(
                    value = state.fullName,
                    onValueChange = { viewModel.onEvent(SignUpEvent.FullNameChanged(it)) },
                    label = "Full Name",
                    isError = state.fullNameError != null,
                    errorMessage = state.fullNameError,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(spacing.medium))

                PrimaryTextField(
                    value = state.email,
                    onValueChange = { viewModel.onEvent(SignUpEvent.EmailChanged(it)) },
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
                    onValueChange = { viewModel.onEvent(SignUpEvent.PasswordChanged(it)) },
                    label = "Password",
                    isError = state.passwordError != null,
                    errorMessage = state.passwordError,
                    modifier = Modifier.fillMaxWidth()
                )
                
                PasswordStrengthIndicator(strength = state.passwordStrength, passwordLength = state.password.length)

                Spacer(modifier = Modifier.height(spacing.medium))

                PasswordTextField(
                    value = state.confirmPassword,
                    onValueChange = { viewModel.onEvent(SignUpEvent.ConfirmPasswordChanged(it)) },
                    label = "Confirm Password",
                    isError = state.confirmPasswordError != null,
                    errorMessage = state.confirmPasswordError,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(spacing.medium))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = state.termsAccepted,
                        onCheckedChange = { viewModel.onEvent(SignUpEvent.TermsAcceptedChanged(it)) }
                    )
                    Text(
                        text = "I accept the Terms and Conditions",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (state.termsAcceptedError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(spacing.large))

                PrimaryButton(
                    text = "Sign Up",
                    onClick = { viewModel.onEvent(SignUpEvent.Submit) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isLoading
                )
                Spacer(modifier = Modifier.height(spacing.large))
            }

            if (state.isLoading) {
                FullScreenLoader()
            }
        }
    }
}

@Composable
fun PasswordStrengthIndicator(strength: PasswordStrength, passwordLength: Int) {
    if (passwordLength == 0) return
    
    val color = when (strength) {
        PasswordStrength.WEAK -> MaterialTheme.colorScheme.error
        PasswordStrength.MEDIUM -> WarningColor
        PasswordStrength.STRONG -> SuccessColor
    }
    
    val text = when (strength) {
        PasswordStrength.WEAK -> "Weak"
        PasswordStrength.MEDIUM -> "Medium"
        PasswordStrength.STRONG -> "Strong"
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, start = 16.dp, end = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(3) { index ->
                val barColor = if (index < strength.ordinal + 1) color else MaterialTheme.colorScheme.surfaceVariant
                Box(modifier = Modifier.height(4.dp).width(24.dp).background(barColor))
            }
        }
        Text(text = text, style = MaterialTheme.typography.labelSmall, color = color)
    }
}
