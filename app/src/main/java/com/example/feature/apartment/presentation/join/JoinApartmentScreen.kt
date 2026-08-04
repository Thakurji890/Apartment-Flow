package com.example.feature.apartment.presentation.join

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.ui.components.FullScreenLoader
import com.example.ui.components.PrimaryButton
import com.example.ui.components.PrimaryTextField
import com.example.ui.components.StandardTopAppBar
import com.example.ui.theme.LocalSpacing
import kotlinx.coroutines.flow.collectLatest

@Composable
fun JoinApartmentScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    viewModel: JoinApartmentViewModel = hiltViewModel()
) {
    val inviteCode by viewModel.inviteCode.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val spacing = LocalSpacing.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is JoinApartmentEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is JoinApartmentEvent.Success -> {
                    onNavigateToDashboard()
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            StandardTopAppBar(title = "Join Apartment", onNavigateBack = onNavigateBack)
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
                    text = "Have an invite code?",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Spacer(modifier = Modifier.height(spacing.medium))
                
                Text(
                    text = "Enter the 6-character code provided by your roommate to join their apartment.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(spacing.extraLarge))

                PrimaryTextField(
                    value = inviteCode,
                    onValueChange = viewModel::onInviteCodeChanged,
                    label = "Invite Code",
                    isError = error != null,
                    errorMessage = error,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(spacing.large))

                PrimaryButton(
                    text = "Join Apartment",
                    onClick = viewModel::submitInviteCode,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading && inviteCode.isNotBlank()
                )
            }

            if (isLoading) {
                FullScreenLoader()
            }
        }
    }
}
