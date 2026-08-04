package com.example.feature.expense.presentation.edit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.feature.expense.domain.model.DefaultCategories
import com.example.ui.components.FullScreenLoader
import com.example.ui.components.PrimaryButton
import com.example.ui.components.PrimaryTextField
import com.example.ui.components.StandardTopAppBar
import com.example.ui.theme.LocalSpacing
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExpenseScreen(
    onNavigateBack: () -> Unit,
    viewModel: EditExpenseViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val spacing = LocalSpacing.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()
    
    var categoryExpanded by remember { mutableStateOf(false) }
    var payerExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is EditExpenseUiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is EditExpenseUiEvent.Success -> {
                    onNavigateBack()
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            StandardTopAppBar(title = "Edit Expense", onNavigateBack = onNavigateBack)
        }
    ) { innerPadding ->
        if (state.isLoading) {
            FullScreenLoader()
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = spacing.medium)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(spacing.medium)
                ) {
                    Spacer(modifier = Modifier.height(spacing.small))
                    
                    PrimaryTextField(
                        value = state.title,
                        onValueChange = { viewModel.onEvent(EditExpenseEvent.TitleChanged(it)) },
                        label = "Expense Title *",
                        isError = state.titleError != null,
                        errorMessage = state.titleError,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    PrimaryTextField(
                        value = state.amount,
                        onValueChange = { viewModel.onEvent(EditExpenseEvent.AmountChanged(it)) },
                        label = "Amount *",
                        isError = state.amountError != null,
                        errorMessage = state.amountError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = { categoryExpanded = !categoryExpanded }
                    ) {
                        OutlinedTextField(
                            value = state.category.name,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false }
                        ) {
                            DefaultCategories.list.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category.name) },
                                    onClick = {
                                        viewModel.onEvent(EditExpenseEvent.CategoryChanged(category))
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    ExposedDropdownMenuBox(
                        expanded = payerExpanded,
                        onExpandedChange = { payerExpanded = !payerExpanded }
                    ) {
                        val payerName = state.members.find { it.userId == state.paidByUserId }?.displayName ?: ""
                        OutlinedTextField(
                            value = payerName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Paid By *") },
                            isError = state.paidByError != null,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = payerExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = payerExpanded,
                            onDismissRequest = { payerExpanded = false }
                        ) {
                            state.members.forEach { member ->
                                DropdownMenuItem(
                                    text = { Text(member.displayName) },
                                    onClick = {
                                        viewModel.onEvent(EditExpenseEvent.PaidByChanged(member.userId))
                                        payerExpanded = false
                                    }
                                )
                            }
                        }
                        if (state.paidByError != null) {
                            Text(
                                text = state.paidByError!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = spacing.small, top = spacing.extraSmall)
                            )
                        }
                    }
                    
                    PrimaryTextField(
                        value = state.description,
                        onValueChange = { viewModel.onEvent(EditExpenseEvent.DescriptionChanged(it)) },
                        label = "Description (Optional)",
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Is Recurring Expense?")
                        Switch(
                            checked = state.isRecurring,
                            onCheckedChange = { viewModel.onEvent(EditExpenseEvent.IsRecurringChanged(it)) }
                        )
                    }

                    Spacer(modifier = Modifier.height(spacing.large))
                    
                    PrimaryButton(
                        text = "Save Changes",
                        onClick = { viewModel.onEvent(EditExpenseEvent.Submit) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isSaving
                    )
                    
                    Spacer(modifier = Modifier.height(spacing.large))
                }
                
                if (state.isSaving) {
                    FullScreenLoader()
                }
            }
        }
    }
}
