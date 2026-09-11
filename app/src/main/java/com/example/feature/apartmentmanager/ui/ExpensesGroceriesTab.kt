package com.example.feature.apartmentmanager.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.feature.apartmentmanager.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesGroceriesTab(
    state: ApartmentUiState,
    onFilterRoommate: (String?) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onAddExpense: () -> Unit,
    onEditExpense: (ApartmentExpense) -> Unit,
    onDeleteExpense: (String) -> Unit,
    onApproveExpense: (String) -> Unit,
    onRejectExpense: (String, String) -> Unit
) {
    val currency = state.profile.currencySymbol
    var expenseToDelete by remember { mutableStateOf<ApartmentExpense?>(null) }
    var expenseToReject by remember { mutableStateOf<ApartmentExpense?>(null) }
    var rejectionReasonText by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf<ExpenseStatus?>(null) }

    val filteredExpenses = remember(state.expenses, state.expenseFilterRoommateId, state.searchQuery, statusFilter) {
        state.expenses.filter { exp ->
            val matchesFilter = state.expenseFilterRoommateId == null ||
                    exp.paidByRoommateId == state.expenseFilterRoommateId ||
                    exp.sharedByRoommateIds.contains(state.expenseFilterRoommateId)

            val matchesSearch = state.searchQuery.isBlank() ||
                    exp.item.contains(state.searchQuery, ignoreCase = true) ||
                    exp.notes.contains(state.searchQuery, ignoreCase = true) ||
                    exp.date.contains(state.searchQuery, ignoreCase = true)

            val matchesStatus = statusFilter == null || exp.status == statusFilter

            matchesFilter && matchesSearch && matchesStatus
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Search & Filter Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search item, date, note...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Status Filter Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = statusFilter == null,
                    onClick = { statusFilter = null },
                    label = { Text("All Status") }
                )
                FilterChip(
                    selected = statusFilter == ExpenseStatus.PENDING,
                    onClick = {
                        statusFilter = if (statusFilter == ExpenseStatus.PENDING) null else ExpenseStatus.PENDING
                    },
                    label = { Text("Pending (${state.pendingExpenseCount})") },
                    leadingIcon = {
                        Icon(Icons.Default.HourglassTop, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                )
                FilterChip(
                    selected = statusFilter == ExpenseStatus.APPROVED,
                    onClick = {
                        statusFilter = if (statusFilter == ExpenseStatus.APPROVED) null else ExpenseStatus.APPROVED
                    },
                    label = { Text("Approved") },
                    leadingIcon = {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                )
                FilterChip(
                    selected = statusFilter == ExpenseStatus.REJECTED,
                    onClick = {
                        statusFilter = if (statusFilter == ExpenseStatus.REJECTED) null else ExpenseStatus.REJECTED
                    },
                    label = { Text("Rejected") },
                    leadingIcon = {
                        Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                )
            }

            // Roommate Filter Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = state.expenseFilterRoommateId == null,
                    onClick = { onFilterRoommate(null) },
                    label = { Text("All Members (${state.expenses.size})") }
                )

                state.roommates.forEach { rm ->
                    val isSelected = state.expenseFilterRoommateId == rm.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { onFilterRoommate(if (isSelected) null else rm.id) },
                        label = { Text(rm.name) },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(Color(rm.colorHex))
                            )
                        }
                    )
                }
            }

            // Admin Pending Banner Notice
            if (state.pendingExpenseCount > 0) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (state.isActiveUserAdmin)
                            MaterialTheme.colorScheme.tertiaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.HourglassTop,
                            contentDescription = null,
                            tint = if (state.isActiveUserAdmin)
                                MaterialTheme.colorScheme.onTertiaryContainer
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (state.isActiveUserAdmin)
                                    "${state.pendingExpenseCount} Pending Expense Approval(s)"
                                else
                                    "${state.pendingExpenseCount} Expense(s) Awaiting Admin Approval",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (state.isActiveUserAdmin)
                                    MaterialTheme.colorScheme.onTertiaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (state.isActiveUserAdmin)
                                    "Review and tap Approve below to update ledger balances."
                                else
                                    "Your admin must approve these before balances are calculated.",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (state.isActiveUserAdmin)
                                    MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                        if (statusFilter != ExpenseStatus.PENDING) {
                            TextButton(onClick = { statusFilter = ExpenseStatus.PENDING }) {
                                Text("Filter")
                            }
                        }
                    }
                }
            }

            // Clean Non-overlapping Add Button
            Button(
                onClick = onAddExpense,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Grocery or Expense", fontWeight = FontWeight.SemiBold)
            }
        }

        // Expense List
        if (filteredExpenses.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.ReceiptLong,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No expenses found",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Tap 'Add Grocery or Expense' to record a purchase",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredExpenses, key = { it.id }) { expense ->
                    val payer = state.roommates.find { it.id == expense.paidByRoommateId }
                    ExpenseCard(
                        expense = expense,
                        payer = payer,
                        roommates = state.roommates,
                        currency = currency,
                        isAdmin = state.isActiveUserAdmin,
                        onEdit = { onEditExpense(expense) },
                        onDelete = { expenseToDelete = expense },
                        onApprove = { onApproveExpense(expense.id) },
                        onReject = {
                            expenseToReject = expense
                            rejectionReasonText = ""
                        }
                    )
                }
            }
        }
    }

    // Reject Dialog
    if (expenseToReject != null) {
        val exp = expenseToReject!!
        val payerName = state.roommates.find { it.id == exp.paidByRoommateId }?.name ?: "Roommate"
        BackHandler { expenseToReject = null }

        AlertDialog(
            onDismissRequest = { expenseToReject = null },
            icon = { Icon(Icons.Default.Cancel, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Reject Expense?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Reject '${exp.item}' ($currency${"%.2f".format(exp.amount)}) by $payerName?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = rejectionReasonText,
                        onValueChange = { rejectionReasonText = it },
                        label = { Text("Reason (Optional)") },
                        placeholder = { Text("e.g. Duplicate entry, wrong split") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRejectExpense(exp.id, rejectionReasonText)
                        expenseToReject = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Reject")
                }
            },
            dismissButton = {
                TextButton(onClick = { expenseToReject = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (expenseToDelete != null) {
        BackHandler { expenseToDelete = null }
        AlertDialog(
            onDismissRequest = { expenseToDelete = null },
            title = { Text("Delete Expense?") },
            text = { Text("Are you sure you want to delete '${expenseToDelete!!.item}' ($currency${"%.2f".format(expenseToDelete!!.amount)})?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteExpense(expenseToDelete!!.id)
                        expenseToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { expenseToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ExpenseCard(
    expense: ApartmentExpense,
    payer: ApartmentRoommate?,
    roommates: List<ApartmentRoommate>,
    currency: String,
    isAdmin: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (expense.status) {
                ExpenseStatus.PENDING -> MaterialTheme.colorScheme.surface
                ExpenseStatus.APPROVED -> MaterialTheme.colorScheme.surface
                ExpenseStatus.REJECTED -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Item name + Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text = expense.item,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = expense.date,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "$currency${"%.2f".format(expense.amount)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Status Badge Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                when (expense.status) {
                    ExpenseStatus.PENDING -> {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.HourglassTop,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Pending Admin Approval",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                    ExpenseStatus.APPROVED -> {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Approved",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    ExpenseStatus.REJECTED -> {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Cancel,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Rejected: ${expense.rejectionReason ?: "By Admin"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.error,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

            // Paid By & Split Summary Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Paid By Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false).padding(end = 8.dp)
                ) {
                    Text(
                        text = "Paid by ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(Color(payer?.colorHex ?: 0xFF6200EEL))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = payer?.name ?: "Unknown",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                // Sharing stats pill
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${expense.sharingCount} split (${currency}${"%.2f".format(expense.shareEach)} ea)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Shared By Avatars / Badges
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                roommates.forEach { rm ->
                    val isShared = expense.sharedByRoommateIds.contains(rm.id)
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isShared) Color(rm.colorHex).copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isShared) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(rm.colorHex),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                            }
                            Text(
                                text = rm.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isShared) Color(rm.colorHex) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                fontWeight = if (isShared) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // Optional Notes
            if (expense.notes.isNotBlank()) {
                Text(
                    text = "Note: ${expense.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Admin Approval Action Bar for Pending Expenses
            if (expense.status == ExpenseStatus.PENDING) {
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                if (isAdmin) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onApprove,
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Approve", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onReject,
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reject", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Text(
                        text = "⏳ Awaiting Admin Approval — balances will update once approved.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }

            // Actions row (Edit & Delete)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onEdit, modifier = Modifier.size(48.dp)) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(48.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
