package com.example.feature.apartment.presentation.create

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
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.ui.components.FullScreenLoader
import com.example.ui.components.PrimaryButton
import com.example.ui.components.PrimaryTextField
import com.example.ui.components.StandardTopAppBar
import com.example.ui.theme.LocalSpacing
import kotlinx.coroutines.flow.collectLatest

@Composable
fun CreateApartmentScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    viewModel: CreateApartmentViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsState().value
    val spacing = LocalSpacing.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is CreateApartmentUiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is CreateApartmentUiEvent.Success -> {
                    onNavigateToDashboard()
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            StandardTopAppBar(title = "Create Apartment", onNavigateBack = onNavigateBack)
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = spacing.large)
                    .verticalScroll(scrollState)
            ) {
                Spacer(modifier = Modifier.height(spacing.large))
                Text(
                    text = "Apartment Details",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(spacing.medium))

                PrimaryTextField(
                    value = state.name,
                    onValueChange = { viewModel.onEvent(CreateApartmentEvent.NameChanged(it)) },
                    label = "Apartment Name *",
                    isError = state.nameError != null,
                    errorMessage = state.nameError,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(spacing.medium))

                PrimaryTextField(
                    value = state.address,
                    onValueChange = { viewModel.onEvent(CreateApartmentEvent.AddressChanged(it)) },
                    label = "Street Address *",
                    isError = state.addressError != null,
                    errorMessage = state.addressError,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(spacing.medium))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing.small)) {
                    PrimaryTextField(
                        value = state.city,
                        onValueChange = { viewModel.onEvent(CreateApartmentEvent.CityChanged(it)) },
                        label = "City",
                        modifier = Modifier.weight(1f)
                    )
                    PrimaryTextField(
                        value = state.state,
                        onValueChange = { viewModel.onEvent(CreateApartmentEvent.StateChanged(it)) },
                        label = "State",
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(spacing.medium))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing.small)) {
                    PrimaryTextField(
                        value = state.country,
                        onValueChange = { viewModel.onEvent(CreateApartmentEvent.CountryChanged(it)) },
                        label = "Country",
                        modifier = Modifier.weight(1f)
                    )
                    PrimaryTextField(
                        value = state.pinCode,
                        onValueChange = { viewModel.onEvent(CreateApartmentEvent.PinCodeChanged(it)) },
                        label = "PIN Code",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(spacing.large))
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(spacing.medium))
                
                PrimaryTextField(
                    value = state.currency,
                    onValueChange = { viewModel.onEvent(CreateApartmentEvent.CurrencyChanged(it)) },
                    label = "Currency (e.g. USD, EUR)",
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(spacing.medium))
                
                PrimaryTextField(
                    value = state.monthlyRentDueDate,
                    onValueChange = { viewModel.onEvent(CreateApartmentEvent.RentDueDateChanged(it)) },
                    label = "Monthly Rent Due Date (1-31)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(spacing.extraLarge))

                PrimaryButton(
                    text = "Create Apartment",
                    onClick = { viewModel.onEvent(CreateApartmentEvent.Submit) },
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
