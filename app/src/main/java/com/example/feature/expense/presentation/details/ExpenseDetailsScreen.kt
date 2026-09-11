package com.example.feature.expense.presentation.details

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.feature.apartment.domain.model.Role
import com.example.feature.expense.domain.model.DefaultCategories
import com.example.ui.components.FullScreenLoader
import com.example.ui.components.StandardTopAppBar
import com.example.ui.theme.LocalSpacing
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDetailsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEditExpense: (String, String) -> Unit,
    viewModel: ExpenseDetailsViewModel = hiltViewModel()
) {
    val expense by viewModel.expense.collectAsState()
    val members by viewModel.members.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val spacing = LocalSpacing.current
    val snackbarHostState = remember { SnackbarHostState() }
    
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showRejectDialog by remember { mutableStateOf(false) }
    var rejectionReason by remember { mutableStateOf("") }

    val currentMember = members.find { it.userId == viewModel.currentUserId }
    val isAdmin = currentMember?.role == Role.ADMIN || currentMember?.role == Role.OWNER

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is ExpenseDetailsEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is ExpenseDetailsEvent.DeleteSuccess -> {
                    onNavigateBack()
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            StandardTopAppBar(
                title = "Expense Details",
                onNavigateBack = onNavigateBack,
                actions = {
                    val exp = expense
                    if (exp != null && (exp.createdBy == viewModel.currentUserId || members.find { it.userId == viewModel.currentUserId }?.role == com.example.feature.apartment.domain.model.Role.ADMIN)) {
                        IconButton(onClick = { onNavigateToEditExpense(exp.apartmentId, exp.expenseId) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (isLoading) {
            FullScreenLoader()
        } else if (error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text(text = error ?: "Unknown error", color = MaterialTheme.colorScheme.error)
            }
        } else if (expense != null) {
            val exp = expense!!
            val category = DefaultCategories.getCategoryById(exp.categoryId)
            val payerName = members.find { it.userId == exp.paidBy }?.displayName ?: "Unknown"
            val creatorName = members.find { it.userId == exp.createdBy }?.displayName ?: "Unknown"
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(spacing.medium)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(spacing.medium)
            ) {
                Text(
                    text = exp.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "${exp.amount} ${exp.currency}",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                // Status Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (exp.status.uppercase()) {
                        "PENDING" -> MaterialTheme.colorScheme.tertiaryContainer
                        "APPROVED", "ACTIVE" -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                        "REJECTED" -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (exp.status.uppercase()) {
                                "PENDING" -> Icons.Default.HourglassTop
                                "APPROVED", "ACTIVE" -> Icons.Default.CheckCircle
                                "REJECTED" -> Icons.Default.Cancel
                                else -> Icons.Default.Info
                            },
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = when (exp.status.uppercase()) {
                                "PENDING" -> MaterialTheme.colorScheme.onTertiaryContainer
                                "APPROVED", "ACTIVE" -> MaterialTheme.colorScheme.primary
                                "REJECTED" -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when (exp.status.uppercase()) {
                                "PENDING" -> "Status: Pending Admin Approval"
                                "APPROVED" -> "Status: Approved"
                                "ACTIVE" -> "Status: Active"
                                "REJECTED" -> "Status: Rejected"
                                else -> "Status: ${exp.status}"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = when (exp.status.uppercase()) {
                                "PENDING" -> MaterialTheme.colorScheme.onTertiaryContainer
                                "APPROVED", "ACTIVE" -> MaterialTheme.colorScheme.primary
                                "REJECTED" -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
                
                // Admin Actions for Pending Status
                if (exp.status.equals("PENDING", ignoreCase = true)) {
                    if (isAdmin) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "Admin Approval Required",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "This expense was submitted by $creatorName and requires your approval before being counted in the apartment balance.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel.approveExpense() },
                                        modifier = Modifier.weight(1f).height(44.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Approve", fontWeight = FontWeight.Bold)
                                    }
                                    OutlinedButton(
                                        onClick = { showRejectDialog = true },
                                        modifier = Modifier.weight(1f).height(44.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Reject", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    } else {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.HourglassTop, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "This expense is currently pending approval by an apartment administrator.",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                Divider()
                
                DetailRow(label = "Category", value = category.name)
                DetailRow(label = "Paid By", value = payerName)
                DetailRow(label = "Created By", value = creatorName)
                DetailRow(label = "Date", value = dateFormat.format(Date(exp.expenseDate)))
                
                if (exp.description.isNotBlank()) {
                    DetailRow(label = "Description", value = exp.description)
                }
                if (exp.notes.isNotBlank()) {
                    DetailRow(label = "Notes", value = exp.notes)
                }
                
                DetailRow(label = "Recurring", value = if (exp.isRecurring) "Yes" else "No")
                
                Spacer(modifier = Modifier.height(spacing.large))
                
                Text(
                    text = "Participants (Placeholder)",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(text = "Expense splitting will be implemented later.")
            }
        }
    }

    if (showRejectDialog) {
        BackHandler { showRejectDialog = false }
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            icon = { Icon(Icons.Default.Cancel, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Reject Expense") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Provide a reason for rejecting this expense:")
                    OutlinedTextField(
                        value = rejectionReason,
                        onValueChange = { rejectionReason = it },
                        placeholder = { Text("e.g., Wrong amount, duplicate entry") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRejectDialog = false
                        viewModel.rejectExpense(rejectionReason)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Reject Expense")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDeleteDialog) {
        BackHandler { showDeleteDialog = false }
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Expense") },
            text = { Text("Are you sure you want to delete this expense? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteExpense()
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    val spacing = LocalSpacing.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = spacing.extraSmall),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
    }
}
