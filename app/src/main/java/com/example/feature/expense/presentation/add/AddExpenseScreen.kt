package com.example.feature.expense.presentation.add

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.feature.apartmentmanager.model.ExpenseCategory
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddExpenseScreen(
    onNavigateBack: () -> Unit,
    onExpenseAdded: () -> Unit = onNavigateBack,
    viewModel: AddExpenseViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    var payerDropdownExpanded by remember { mutableStateOf(false) }
    var showDatePickerDialog by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = state.date
    )

    BackHandler {
        onNavigateBack()
    }

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is AddExpenseUiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is AddExpenseUiEvent.Success -> {
                    onExpenseAdded()
                }
            }
        }
    }

    // Material 3 Date Picker Dialog
    if (showDatePickerDialog) {
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            viewModel.onEvent(AddExpenseEvent.DateChanged(it))
                        }
                        showDatePickerDialog = false
                    },
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Text("Select")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePickerDialog = false },
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Add New Expense",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Shared Household Ledger",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("add_expense_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Ledger"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.onEvent(AddExpenseEvent.ResetForm) },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset fields"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Button(
                        onClick = { viewModel.onEvent(AddExpenseEvent.Submit) },
                        enabled = !state.isLoading,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("save_expense_button")
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (state.amountValue > 0) {
                                    "Save Expense • ${state.currencySymbol}${"%.2f".format(state.amountValue)}"
                                } else {
                                    "Save Expense"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Amount Display Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Total Expense",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${state.currencySymbol} ${if (state.amount.isBlank()) "0.00" else state.amount}",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    if (state.splitCount > 0 && state.amountValue > 0) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                        ) {
                            Text(
                                text = "Split: ${state.currencySymbol}${"%.2f".format(state.splitEach)} each (${state.splitCount} people)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Description Field
            OutlinedTextField(
                value = state.description,
                onValueChange = { viewModel.onEvent(AddExpenseEvent.DescriptionChanged(it)) },
                label = { Text("Description / Item Name *") },
                placeholder = { Text("e.g. Weekly Groceries, Electricity Bill, WiFi") },
                leadingIcon = {
                    Icon(Icons.Default.ReceiptLong, contentDescription = "Description icon")
                },
                trailingIcon = {
                    if (state.description.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.onEvent(AddExpenseEvent.DescriptionChanged("")) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear description")
                        }
                    }
                },
                isError = state.descriptionError != null,
                supportingText = {
                    state.descriptionError?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.error)
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("expense_description_input")
            )

            // Amount Input Field
            OutlinedTextField(
                value = state.amount,
                onValueChange = { viewModel.onEvent(AddExpenseEvent.AmountChanged(it)) },
                label = { Text("Amount *") },
                placeholder = { Text("0.00") },
                prefix = {
                    Text(
                        text = "${state.currencySymbol} ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                leadingIcon = {
                    Icon(Icons.Default.Payments, contentDescription = "Amount icon")
                },
                trailingIcon = {
                    if (state.amount.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.onEvent(AddExpenseEvent.AmountChanged("")) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear amount")
                        }
                    }
                },
                isError = state.amountError != null,
                supportingText = {
                    state.amountError?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.error)
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("expense_amount_input")
            )

            // Quick Preset Amount Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(50.0, 100.0, 200.0, 500.0, 1000.0).forEach { delta ->
                    SuggestionChip(
                        onClick = { viewModel.onEvent(AddExpenseEvent.AddPresetAmount(delta)) },
                        label = {
                            Text(
                                "+${state.currencySymbol}${"%.0f".format(delta)}",
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.height(36.dp)
                    )
                }
            }

            // Date Field with DatePicker Dialog
            OutlinedTextField(
                value = state.formattedDate,
                onValueChange = {},
                readOnly = true,
                label = { Text("Date *") },
                leadingIcon = {
                    Icon(Icons.Default.CalendarToday, contentDescription = "Date icon")
                },
                trailingIcon = {
                    IconButton(
                        onClick = { showDatePickerDialog = true },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.EditCalendar, contentDescription = "Pick date")
                    }
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("expense_date_picker")
                    .clickable { showDatePickerDialog = true }
            )

            // Roommate Paid Dropdown (ExposedDropdownMenuBox)
            ExposedDropdownMenuBox(
                expanded = payerDropdownExpanded,
                onExpandedChange = { payerDropdownExpanded = !payerDropdownExpanded },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("expense_paid_by_dropdown")
            ) {
                val selected = state.selectedRoommate
                OutlinedTextField(
                    value = selected?.name ?: "Select Roommate",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Paid By Roommate *") },
                    leadingIcon = {
                        if (selected != null) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(selected.colorHex)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = selected.name.take(1).uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        } else {
                            Icon(Icons.Default.Person, contentDescription = null)
                        }
                    },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = payerDropdownExpanded)
                    },
                    supportingText = {
                        if (selected != null) {
                            Text(
                                text = "${selected.email} • Status: ${selected.balanceStatus}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else if (state.paidByError != null) {
                            Text(text = state.paidByError!!, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    isError = state.paidByError != null,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = payerDropdownExpanded,
                    onDismissRequest = { payerDropdownExpanded = false }
                ) {
                    state.roommates.forEach { rm ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(rm.colorHex)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = rm.name.take(1).uppercase(),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = rm.name,
                                                fontWeight = FontWeight.SemiBold,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                            if (rm.isAdmin) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = MaterialTheme.colorScheme.primaryContainer
                                                ) {
                                                    Text(
                                                        text = "Admin",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = rm.email,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Balance: ${rm.balanceStatus}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (rm.balanceAmount > 0.005) {
                                                Color(0xFF2E7D32)
                                            } else if (rm.balanceAmount < -0.005) {
                                                MaterialTheme.colorScheme.error
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            }
                                        )
                                    }
                                    if (rm.id == state.paidByRoommateId) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            },
                            onClick = {
                                viewModel.onEvent(AddExpenseEvent.PaidByChanged(rm.id))
                                payerDropdownExpanded = false
                            },
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                        )
                    }
                }
            }

            // Category Selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExpenseCategory.entries.forEach { cat ->
                        val isSelected = state.category == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onEvent(AddExpenseEvent.CategoryChanged(cat)) },
                            label = { Text(cat.displayName) },
                            leadingIcon = {
                                val icon = when (cat) {
                                    ExpenseCategory.GROCERIES -> Icons.Default.ShoppingCart
                                    ExpenseCategory.UTILITIES -> Icons.Default.Bolt
                                    ExpenseCategory.RENT -> Icons.Default.Home
                                    ExpenseCategory.HOUSEHOLD_SUPPLIES -> Icons.Default.CleaningServices
                                    ExpenseCategory.INTERNET_WIFI -> Icons.Default.Wifi
                                    ExpenseCategory.ENTERTAINMENT -> Icons.Default.Tv
                                    ExpenseCategory.MAINTENANCE -> Icons.Default.Build
                                    ExpenseCategory.OTHER -> Icons.Default.Category
                                }
                                Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }

            // Split Share Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Split Between",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${state.splitCount} roommate(s) • ${state.currencySymbol}${"%.2f".format(state.splitEach)} each",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        TextButton(
                            onClick = { viewModel.onEvent(AddExpenseEvent.ToggleAllRoommates) },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Text(
                                text = if (state.sharedWithRoommateIds.size == state.roommates.size) "Payer Only" else "Select All",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        state.roommates.forEach { rm ->
                            val isChecked = state.sharedWithRoommateIds.contains(rm.id)
                            FilterChip(
                                selected = isChecked,
                                onClick = { viewModel.onEvent(AddExpenseEvent.ToggleSharedRoommate(rm.id)) },
                                label = { Text(rm.name) },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(Color(rm.colorHex)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = rm.name.take(1).uppercase(),
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }
            }

            // Optional Notes Field
            OutlinedTextField(
                value = state.notes,
                onValueChange = { viewModel.onEvent(AddExpenseEvent.NotesChanged(it)) },
                label = { Text("Notes / Store Details (Optional)") },
                placeholder = { Text("e.g. Receipt #4829, Paid via UPI, Store location") },
                leadingIcon = {
                    Icon(Icons.Default.Notes, contentDescription = null)
                },
                maxLines = 3,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
