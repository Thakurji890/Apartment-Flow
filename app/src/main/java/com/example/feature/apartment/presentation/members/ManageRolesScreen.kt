package com.example.feature.apartment.presentation.members

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.feature.apartment.presentation.details.ApartmentDetailsViewModel
import com.example.ui.components.StandardTopAppBar
import com.example.ui.theme.LocalSpacing

@Composable
fun ManageRolesScreen(
    onNavigateBack: () -> Unit,
    viewModel: ApartmentDetailsViewModel = hiltViewModel()
) {
    val members by viewModel.members.collectAsState()
    val spacing = LocalSpacing.current
    
    Scaffold(
        topBar = {
            StandardTopAppBar(title = "Manage Roles", onNavigateBack = onNavigateBack)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(spacing.medium)
        ) {
            Text(text = "Manage Roles (Coming Soon)")
        }
    }
}
