package com.example.feature.chore.presentation.add_edit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.feature.chore.domain.model.ChoreCategory
import com.example.feature.chore.domain.model.ChorePriority
import com.example.feature.chore.domain.model.ChoreRecurrence
import com.example.ui.components.PrimaryButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditChoreScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddEditChoreViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(true) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is AddEditChoreEvent.SaveSuccess -> onNavigateBack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.choreId == null) "New Chore" else "Edit Chore") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Chore Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            OutlinedTextField(
                value = state.description,
                onValueChange = viewModel::onDescriptionChange,
                label = { Text("Description (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            // Category Dropdown
            var categoryExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = it }
            ) {
                OutlinedTextField(
                    value = state.category.toString(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    ChoreCategory.values().forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.toString()) },
                            onClick = {
                                viewModel.onCategoryChange(cat)
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            // Priority Dropdown
            var priorityExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = priorityExpanded,
                onExpandedChange = { priorityExpanded = it }
            ) {
                OutlinedTextField(
                    value = state.priority.toString(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Priority") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = priorityExpanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = priorityExpanded,
                    onDismissRequest = { priorityExpanded = false }
                ) {
                    ChorePriority.values().forEach { prio ->
                        DropdownMenuItem(
                            text = { Text(prio.toString()) },
                            onClick = {
                                viewModel.onPriorityChange(prio)
                                priorityExpanded = false
                            }
                        )
                    }
                }
            }
            
            // Assignee Dropdown
            var assigneeExpanded by remember { mutableStateOf(false) }
            val assigneeName = state.availableMembers.find { it.userId == state.assignedTo }?.displayName ?: "Unassigned"
            ExposedDropdownMenuBox(
                expanded = assigneeExpanded,
                onExpandedChange = { assigneeExpanded = it }
            ) {
                OutlinedTextField(
                    value = assigneeName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Assign To") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = assigneeExpanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = assigneeExpanded,
                    onDismissRequest = { assigneeExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Unassigned") },
                        onClick = {
                            viewModel.onAssignedToChange(null)
                            assigneeExpanded = false
                        }
                    )
                    state.availableMembers.forEach { member ->
                        DropdownMenuItem(
                            text = { Text(member.displayName) },
                            onClick = {
                                viewModel.onAssignedToChange(member.userId)
                                assigneeExpanded = false
                            }
                        )
                    }
                }
            }

            // Recurrence Dropdown
            var recurrenceExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = recurrenceExpanded,
                onExpandedChange = { recurrenceExpanded = it }
            ) {
                OutlinedTextField(
                    value = state.recurrence.toString(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Recurrence") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = recurrenceExpanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = recurrenceExpanded,
                    onDismissRequest = { recurrenceExpanded = false }
                ) {
                    ChoreRecurrence.values().forEach { rec ->
                        DropdownMenuItem(
                            text = { Text(rec.toString()) },
                            onClick = {
                                viewModel.onRecurrenceChange(rec)
                                recurrenceExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = state.points.toString(),
                onValueChange = { viewModel.onPointsChange(it.toIntOrNull() ?: 0) },
                label = { Text("Points / Difficulty") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            // Due Date simplified input (we'll just use a button or leave it as current time + offset for simplicity, or DatePickerDialog)
            // For now, let's just make it a checkbox to set due tomorrow or none to keep it fast, or standard offset.
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Checkbox(
                    checked = state.dueDate != null && state.dueDate!! > 0L,
                    onCheckedChange = { checked ->
                        if (checked) {
                            // Set due date to tomorrow by default
                            viewModel.onDueDateChange(System.currentTimeMillis() + 86400000)
                        } else {
                            viewModel.onDueDateChange(null)
                        }
                    }
                )
                Text("Set Due Date (Defaults to Tomorrow)")
            }

            if (state.error != null) {
                Text(
                    text = state.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            PrimaryButton(
                text = "Save Chore",
                onClick = viewModel::saveChore,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
