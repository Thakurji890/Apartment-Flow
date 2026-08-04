package com.example.feature.apartment.presentation.details

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.feature.apartment.domain.model.Apartment
import com.example.ui.components.FullScreenLoader
import com.example.ui.theme.LocalSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToApartmentSetup: () -> Unit,
    onNavigateToApartmentDetails: (String) -> Unit,
    onNavigateToExpenses: (String) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val apartments by viewModel.apartments.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val spacing = LocalSpacing.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Apartments") },
                actions = {
                    IconButton(onClick = onNavigateToApartmentSetup) {
                        Icon(Icons.Default.Add, contentDescription = "Add Apartment")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isLoading) {
                FullScreenLoader()
            } else if (apartments.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "You are not part of any apartments.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(spacing.medium))
                    Button(onClick = onNavigateToApartmentSetup) {
                        Text("Create or Join One")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(spacing.medium),
                    verticalArrangement = Arrangement.spacedBy(spacing.medium)
                ) {
                    items(apartments) { apartment ->
                        ApartmentCard(
                            apartment = apartment,
                            onClick = { onNavigateToApartmentDetails(apartment.id) },
                            onExpensesClick = { onNavigateToExpenses(apartment.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ApartmentCard(
    apartment: Apartment,
    onClick: () -> Unit,
    onExpensesClick: () -> Unit
) {
    val spacing = LocalSpacing.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.medium)
        ) {
            Text(
                text = apartment.name,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(spacing.small))
            Text(
                text = apartment.address,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(spacing.medium))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Currency: ${apartment.currency}",
                    style = MaterialTheme.typography.bodySmall
                )
                
                Button(onClick = onExpensesClick) {
                    Icon(Icons.Default.AttachMoney, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(spacing.extraSmall))
                    Text("Expenses")
                }
            }
        }
    }
}
