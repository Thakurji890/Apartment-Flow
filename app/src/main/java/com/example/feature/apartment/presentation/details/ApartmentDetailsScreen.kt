package com.example.feature.apartment.presentation.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.feature.apartment.domain.model.Apartment
import com.example.feature.apartment.domain.model.ApartmentMember
import com.example.feature.apartment.domain.model.Role
import com.example.ui.components.FullScreenLoader
import com.example.ui.components.StandardTopAppBar
import com.example.ui.theme.LocalSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApartmentDetailsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ApartmentDetailsViewModel = hiltViewModel()
) {
    val apartment by viewModel.apartment.collectAsState()
    val members by viewModel.members.collectAsState()
    val inviteCode by viewModel.inviteCode.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
    
    val spacing = LocalSpacing.current
    val clipboardManager = LocalClipboardManager.current
    
    val currentUserMember = members.find { it.userId == currentUserId }
    val isOwnerOrAdmin = currentUserMember?.role == Role.OWNER || currentUserMember?.role == Role.ADMIN

    Scaffold(
        topBar = {
            StandardTopAppBar(
                title = apartment?.name ?: "Apartment Details",
                onNavigateBack = onNavigateBack
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (isLoading && apartment == null) {
                FullScreenLoader()
            } else if (error != null && apartment == null) {
                Text(
                    text = error ?: "Unknown error",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (apartment != null) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(spacing.medium)
                ) {
                    item {
                        ApartmentHeaderInfo(apartment!!)
                        Spacer(modifier = Modifier.height(spacing.extraLarge))
                    }
                    
                    if (isOwnerOrAdmin) {
                        item {
                            Text(
                                text = "Invite Code",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(spacing.medium))
                            
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(spacing.medium),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = inviteCode?.code ?: "No active code",
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "Share this with new roommates",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    
                                    Row {
                                        IconButton(onClick = {
                                            inviteCode?.code?.let {
                                                clipboardManager.setText(AnnotatedString(it))
                                            }
                                        }) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy Code")
                                        }
                                        IconButton(onClick = { viewModel.generateNewInviteCode() }) {
                                            Icon(Icons.Default.Refresh, contentDescription = "Generate New Code")
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(spacing.extraLarge))
                        }
                    }
                    
                    item {
                        Text(
                            text = "Members (${members.size})",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(spacing.medium))
                    }
                    
                    items(members) { member ->
                        MemberItem(member = member)
                        Spacer(modifier = Modifier.height(spacing.small))
                    }
                }
            }
        }
    }
}

@Composable
fun ApartmentHeaderInfo(apartment: Apartment) {
    val spacing = LocalSpacing.current
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(spacing.large)) {
            Text(
                text = "Address",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${apartment.address}, ${apartment.city}, ${apartment.state} ${apartment.pinCode}",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(spacing.medium))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(
                        text = "Rent Due Date",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Day ${apartment.monthlyRentDueDate}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Currency",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = apartment.currency,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
fun MemberItem(member: ApartmentMember) {
    val spacing = LocalSpacing.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = spacing.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = member.displayName.firstOrNull()?.uppercase() ?: "?",
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.titleMedium
            )
        }
        Spacer(modifier = Modifier.width(spacing.medium))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = member.displayName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = member.role.name,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
