package com.example.feature.expense.presentation.details

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
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

    if (showDeleteDialog) {
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
