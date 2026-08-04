package com.example.feature.apartment.presentation.setup

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.example.ui.components.PrimaryButton
import com.example.ui.components.SecondaryButton
import com.example.ui.theme.LocalSpacing

@Composable
fun ApartmentSetupScreen(
    onNavigateToCreate: () -> Unit,
    onNavigateToJoin: () -> Unit
) {
    val spacing = LocalSpacing.current

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = spacing.large),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Set Up Your Apartment",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(spacing.medium))
                    Text(
                        text = "Are you creating a new shared space, or joining an existing one?",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = spacing.extraLarge),
                verticalArrangement = Arrangement.spacedBy(spacing.medium)
            ) {
                PrimaryButton(
                    text = "Create New Apartment",
                    onClick = onNavigateToCreate,
                    modifier = Modifier.fillMaxWidth()
                )
                SecondaryButton(
                    text = "Join with Invite Code",
                    onClick = onNavigateToJoin,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
