package com.example.feature.apartment.presentation.details

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.ui.components.StandardTopAppBar
import com.example.ui.theme.LocalSpacing

@Composable
fun EditApartmentScreen(
    onNavigateBack: () -> Unit,
    viewModel: ApartmentDetailsViewModel = hiltViewModel()
) {
    val apartment by viewModel.apartment.collectAsState()
    val spacing = LocalSpacing.current
    
    Scaffold(
        topBar = {
            StandardTopAppBar(title = "Edit Apartment", onNavigateBack = onNavigateBack)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(spacing.medium)
        ) {
            Text(text = "Edit Apartment (Coming Soon for ${apartment?.name})")
        }
    }
}
