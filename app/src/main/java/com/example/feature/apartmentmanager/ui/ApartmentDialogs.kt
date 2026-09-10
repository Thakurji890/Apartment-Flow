package com.example.feature.apartmentmanager.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.feature.apartmentmanager.model.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditExpenseDialog(
    expense: ApartmentExpense?,
    roommates: List<ApartmentRoommate>,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (date: String, item: String, amount: Double, paidById: String, sharedByIds: List<String>, notes: String) -> Unit
) {
    val today = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()) }
    var date by remember { mutableStateOf(expense?.date ?: today) }
    var item by remember { mutableStateOf(expense?.item ?: "") }
    var amountText by remember { mutableStateOf(expense?.amount?.let { "%.2f".format(it) } ?: "") }
    var paidById by remember { mutableStateOf(expense?.paidByRoommateId ?: roommates.firstOrNull()?.id ?: "") }
    var sharedByIds by remember {
        mutableStateOf(
            expense?.sharedByRoommateIds ?: roommates.map { it.id }
        )
    }
    var notes by remember { mutableStateOf(expense?.notes ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val amountValue = amountText.toDoubleOrNull() ?: 0.0
    val sharingCount = sharedByIds.size
    val shareEach = if (sharingCount > 0) amountValue / sharingCount else 0.0

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (expense != null) "Edit Expense" else "Add Grocery / Expense",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Item Name
                OutlinedTextField(
                    value = item,
                    onValueChange = { item = it },
                    label = { Text("Item Name (e.g. Rice, Chicken, Oil)") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) },
                    singleLine = true
                )

                // Amount & Date
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Amount ($currencySymbol)") },
                        modifier = Modifier.weight(1f),
                        leadingIcon = { Text(currencySymbol, fontWeight = FontWeight.Bold) },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Date") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Paid By Dropdown
                Text(
                    text = "Paid By",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )

                var payerDropdownExpanded by remember { mutableStateOf(false) }
                val selectedPayer = roommates.find { it.id == paidById }

                ExposedDropdownMenuBox(
                    expanded = payerDropdownExpanded,
                    onExpandedChange = { payerDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedPayer?.name ?: "Select Payer",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = payerDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
                    )
                    ExposedDropdownMenu(
                        expanded = payerDropdownExpanded,
                        onDismissRequest = { payerDropdownExpanded = false }
                    ) {
                        roommates.forEach { rm ->
                            DropdownMenuItem(
                                text = { Text(rm.name) },
                                onClick = {
                                    paidById = rm.id
                                    payerDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Roommate Split Matrix (Checkboxes)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Split Between (${sharedByIds.size}/${roommates.size})",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row {
                        TextButton(
                            onClick = { sharedByIds = roommates.map { it.id } },
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text("All")
                        }
                        TextButton(
                            onClick = { sharedByIds = emptyList() },
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text("Clear")
                        }
                    }
                }

                // Checkboxes list
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    roommates.forEach { rm ->
                        val isChecked = sharedByIds.contains(rm.id)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isChecked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    sharedByIds = if (isChecked) {
                                        sharedByIds.filter { it != rm.id }
                                    } else {
                                        sharedByIds + rm.id
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { checked ->
                                            sharedByIds = if (checked) sharedByIds + rm.id else sharedByIds.filter { it != rm.id }
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(rm.name, fontWeight = if (isChecked) FontWeight.SemiBold else FontWeight.Normal)
                                }
                                if (isChecked && sharingCount > 0 && amountValue > 0) {
                                    Text(
                                        "$currencySymbol${"%.2f".format(shareEach)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }

                // Live Summary Banner
                if (sharingCount > 0 && amountValue > 0) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "$sharingCount sharing",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "Each pays $currencySymbol${"%.2f".format(shareEach)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                // Optional Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (item.isBlank()) {
                                errorMessage = "Please enter an item name"
                                return@Button
                            }
                            if (amountValue <= 0.0) {
                                errorMessage = "Please enter a valid amount"
                                return@Button
                            }
                            if (sharedByIds.isEmpty()) {
                                errorMessage = "Please select at least one roommate to share"
                                return@Button
                            }
                            onSave(date, item.trim(), amountValue, paidById, sharedByIds, notes.trim())
                        }
                    ) {
                        Text(if (expense != null) "Update" else "Save Expense")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSettlementDialog(
    roommates: List<ApartmentRoommate>,
    currencySymbol: String,
    prefillFromId: String? = null,
    prefillToId: String? = null,
    prefillAmount: Double? = null,
    onDismiss: () -> Unit,
    onSave: (date: String, fromId: String, toId: String, amount: Double, note: String) -> Unit
) {
    val today = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()) }
    var date by remember { mutableStateOf(today) }
    var fromId by remember { mutableStateOf(prefillFromId ?: roommates.firstOrNull()?.id ?: "") }
    var toId by remember {
        mutableStateOf(
            prefillToId ?: roommates.getOrNull(1)?.id ?: roommates.firstOrNull()?.id ?: ""
        )
    }
    var amountText by remember {
        mutableStateOf(prefillAmount?.let { "%.2f".format(it) } ?: "")
    }
    var note by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Record Cash Settlement",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Date
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date (dd/MM/yyyy)") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                    singleLine = true
                )

                // From (Payer)
                var fromExpanded by remember { mutableStateOf(false) }
                val fromRoommate = roommates.find { it.id == fromId }

                ExposedDropdownMenuBox(
                    expanded = fromExpanded,
                    onExpandedChange = { fromExpanded = it }
                ) {
                    OutlinedTextField(
                        value = fromRoommate?.name ?: "Select Payer",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("From (Who Paid)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fromExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        leadingIcon = { Icon(Icons.Default.ArrowUpward, contentDescription = null) }
                    )
                    ExposedDropdownMenu(
                        expanded = fromExpanded,
                        onDismissRequest = { fromExpanded = false }
                    ) {
                        roommates.forEach { rm ->
                            DropdownMenuItem(
                                text = { Text(rm.name) },
                                onClick = {
                                    fromId = rm.id
                                    fromExpanded = false
                                }
                            )
                        }
                    }
                }

                // To (Receiver)
                var toExpanded by remember { mutableStateOf(false) }
                val toRoommate = roommates.find { it.id == toId }

                ExposedDropdownMenuBox(
                    expanded = toExpanded,
                    onExpandedChange = { toExpanded = it }
                ) {
                    OutlinedTextField(
                        value = toRoommate?.name ?: "Select Receiver",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("To (Who Received)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = toExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        leadingIcon = { Icon(Icons.Default.ArrowDownward, contentDescription = null) }
                    )
                    ExposedDropdownMenu(
                        expanded = toExpanded,
                        onDismissRequest = { toExpanded = false }
                    ) {
                        roommates.forEach { rm ->
                            DropdownMenuItem(
                                text = { Text(rm.name) },
                                onClick = {
                                    toId = rm.id
                                    toExpanded = false
                                }
                            )
                        }
                    }
                }

                // Amount
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount Transferred ($currencySymbol)") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Text(currencySymbol, fontWeight = FontWeight.Bold) },
                    singleLine = true
                )

                // Note
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note / Mode (e.g. Google Pay, Cash, UPI)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (fromId == toId) {
                                errorMessage = "Payer and Receiver cannot be the same roommate"
                                return@Button
                            }
                            val amt = amountText.toDoubleOrNull() ?: 0.0
                            if (amt <= 0.0) {
                                errorMessage = "Please enter a valid amount greater than 0"
                                return@Button
                            }
                            onSave(date, fromId, toId, amt, note.trim())
                        }
                    ) {
                        Text("Record Settlement")
                    }
                }
            }
        }
    }
}

@Composable
fun AddEditRoommateDialog(
    roommate: ApartmentRoommate?,
    onDismiss: () -> Unit,
    onSave: (name: String, notes: String, isAdmin: Boolean) -> Unit
) {
    var name by remember { mutableStateOf(roommate?.name ?: "") }
    var notes by remember { mutableStateOf(roommate?.notes ?: "") }
    var isAdmin by remember { mutableStateOf(roommate?.isAdmin ?: false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (roommate != null) "Edit Roommate" else "Add New Roommate",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Roommate Name") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Contact / Room #") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isAdmin,
                        onCheckedChange = { isAdmin = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Apartment Admin", fontWeight = FontWeight.SemiBold)
                        Text(
                            "Can edit apartment details, add/remove roommates",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                errorMessage = "Name cannot be empty"
                                return@Button
                            }
                            onSave(name.trim(), notes.trim(), isAdmin)
                        }
                    ) {
                        Text(if (roommate != null) "Update" else "Add Roommate")
                    }
                }
            }
        }
    }
}

@Composable
fun EditApartmentProfileDialog(
    profile: ApartmentProfile,
    onDismiss: () -> Unit,
    onSave: (name: String, flatNumber: String, currencySymbol: String, inviteCode: String) -> Unit
) {
    var name by remember { mutableStateOf(profile.name) }
    var flatNumber by remember { mutableStateOf(profile.flatNumber) }
    var currencySymbol by remember { mutableStateOf(profile.currencySymbol) }
    var inviteCode by remember { mutableStateOf(profile.inviteCode) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Apartment Admin Controls",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Apartment / Household Name") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = flatNumber,
                        onValueChange = { flatNumber = it },
                        label = { Text("Flat / Unit #") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = currencySymbol,
                        onValueChange = { currencySymbol = it },
                        label = { Text("Currency") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = inviteCode,
                    onValueChange = { inviteCode = it },
                    label = { Text("Invite Code") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSave(name, flatNumber, currencySymbol, inviteCode)
                        }
                    ) {
                        Text("Save Changes")
                    }
                }
            }
        }
    }
}
