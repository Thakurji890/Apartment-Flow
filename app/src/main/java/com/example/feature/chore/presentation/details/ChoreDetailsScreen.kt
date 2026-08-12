package com.example.feature.chore.presentation.details

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.feature.chore.domain.model.ChoreStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChoreDetailsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEditChore: (String) -> Unit,
    viewModel: ChoreDetailsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showCompleteDialog by remember { mutableStateOf(false) }
    var completeNotes by remember { mutableStateOf("") }

    LaunchedEffect(true) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is ChoreDetailsEvent.DeleteSuccess -> onNavigateBack()
                is ChoreDetailsEvent.CompleteSuccess -> showCompleteDialog = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chore Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToEditChore(viewModel.choreId) }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit Chore")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete Chore", tint = MaterialTheme.colorScheme.error)
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
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
            } else if (state.chore != null) {
                val chore = state.chore!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(text = chore.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    if (chore.description.isNotBlank()) {
                        Text(text = chore.description, style = MaterialTheme.typography.bodyLarge)
                    }
                    
                    HorizontalDivider()
                    
                    DetailRow("Status", chore.status.name)
                    DetailRow("Category", chore.category.toString())
                    DetailRow("Priority", chore.priority.toString())
                    DetailRow("Points", chore.points.toString())
                    DetailRow("Recurrence", chore.recurrence.toString())
                    
                    val assigneeName = state.assignee?.displayName ?: "Unassigned"
                    DetailRow("Assigned To", assigneeName)
                    
                    if (chore.dueDate > 0) {
                        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        DetailRow("Due Date", sdf.format(Date(chore.dueDate)))
                    }
                    
                    if (chore.status == ChoreStatus.COMPLETED) {
                        HorizontalDivider()
                        val completedByName = state.completedBy?.displayName ?: "Unknown"
                        val sdf = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
                        val compDate = chore.completedAt?.let { sdf.format(Date(it)) } ?: "Unknown"
                        
                        DetailRow("Completed By", completedByName)
                        DetailRow("Completed At", compDate)
                        if (chore.notes.isNotBlank()) {
                            DetailRow("Notes", chore.notes)
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    
                    if (chore.status != ChoreStatus.COMPLETED) {
                        Button(
                            onClick = { showCompleteDialog = true },
                            modifier = Modifier.fillMaxWidth().height(56.dp)
                        ) {
                            Text("Mark as Completed")
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Chore") },
            text = { Text("Are you sure you want to delete this chore? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteChore()
                }) {
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
    
    if (showCompleteDialog) {
        AlertDialog(
            onDismissRequest = { showCompleteDialog = false },
            title = { Text("Complete Chore") },
            text = { 
                Column {
                    Text("Mark this chore as completed?")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = completeNotes,
                        onValueChange = { completeNotes = it },
                        label = { Text("Optional Notes") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.completeChore(completeNotes) }) {
                    Text("Complete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCompleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
    }
}
