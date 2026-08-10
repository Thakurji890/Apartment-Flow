package com.example.feature.recurringbill.presentation.details

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringBillDetailsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEdit: () -> Unit,
    viewModel: RecurringBillDetailsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) {
            onNavigateBack()
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Bill") },
            text = { Text("Are you sure you want to delete this recurring bill?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    viewModel.deleteBill()
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bill Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.error != null) {
                Text(
                    text = state.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (state.bill != null) {
                val bill = state.bill!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(text = bill.name, style = MaterialTheme.typography.headlineMedium)
                    
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Amount", style = MaterialTheme.typography.bodyMedium)
                                Text("$${String.format("%.2f", bill.expectedAmount)}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Frequency", style = MaterialTheme.typography.bodyMedium)
                                Text(bill.frequency.name, style = MaterialTheme.typography.bodyLarge)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Type", style = MaterialTheme.typography.bodyMedium)
                                Text(bill.type.name, style = MaterialTheme.typography.bodyLarge)
                            }
                            
                            val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Next Due Date", style = MaterialTheme.typography.bodyMedium)
                                Text(formatter.format(Date(bill.nextDueDate)), style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    if (state.isGenerated) {
                        Text(
                            text = "Expense generated successfully!",
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedButton(
                            onClick = { viewModel.skipOccurrence() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Skip Next")
                        }
                        Button(
                            onClick = { viewModel.generateExpense() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Generate Now")
                        }
                    }
                }
            }
        }
    }
}
