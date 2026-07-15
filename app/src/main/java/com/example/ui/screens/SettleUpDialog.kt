package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Roommate
import com.example.data.Debt
import androidx.compose.ui.res.stringResource
import com.example.R
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettleUpDialog(
    roommates: List<Roommate>,
    suggestedDebts: List<Debt>,
    currencyPref: String,
    onDismiss: () -> Unit,
    onSettle: (String, String, Double) -> Unit
) {
    var selectedDebtorId by remember { mutableStateOf(roommates.firstOrNull()?.id ?: "") }
    var selectedCreditorId by remember { mutableStateOf(roommates.getOrNull(1)?.id ?: roommates.firstOrNull()?.id ?: "") }
    var amountStr by remember { mutableStateOf("") }
    
    var amountError by remember { mutableStateOf(false) }
    var expandedDebtor by remember { mutableStateOf(false) }
    var expandedCreditor by remember { mutableStateOf(false) }

    fun formatCurrency(amount: Double): String {
        val symbol = when {
            currencyPref.contains("€") -> "€"
            currencyPref.contains("£") -> "£"
            currencyPref.contains("₹") -> "₹"
            currencyPref.contains("CAD") -> "CA$"
            else -> "$"
        }
        return String.format(Locale.US, "%s%.2f", symbol, amount)
    }

    val currencySymbol = currencyPref.substringAfter("(").substringBefore(")")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.settle_up_dialog_title),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountStr.toDoubleOrNull()
                    amountError = amount == null || amount <= 0.0 || selectedDebtorId == selectedCreditorId
                    
                    if (!amountError) {
                        onSettle(selectedDebtorId, selectedCreditorId, amount!!)
                    }
                },
                modifier = Modifier.testTag("settle_confirm_button")
            ) {
                Text(stringResource(R.string.settle_up_dialog_confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("settle_cancel_button")
            ) {
                Text(stringResource(R.string.action_cancel))
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // If there are outstanding debts, show "Quick Suggestion" buttons
                if (suggestedDebts.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.settle_up_dialog_suggested),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        suggestedDebts.take(2).forEach { debt ->
                            val debtorName = roommates.find { it.id == debt.fromId }?.name ?: "Unknown"
                            val creditorName = roommates.find { it.id == debt.toId }?.name ?: "Unknown"
                            
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                ),
                                shape = MaterialTheme.shapes.small,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedDebtorId = debt.fromId
                                        selectedCreditorId = debt.toId
                                        amountStr = String.format(Locale.US, "%.2f", debt.amount)
                                    }
                                    .testTag("suggested_debt_${debt.fromId}_to_${debt.toId}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = stringResource(R.string.settle_up_dialog_suggested_template, debtorName, creditorName),
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                        )
                                        Text(
                                            text = stringResource(R.string.settle_up_dialog_settle_full_balance),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                        )
                                    }
                                    
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = formatCurrency(debt.amount),
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Icon(
                                            imageVector = Icons.Default.Payment,
                                            contentDescription = "Pay",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                }

                Text(
                    text = stringResource(R.string.settle_up_dialog_manual_header),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                // Debtor Dropdown (who pays)
                Box(modifier = Modifier.fillMaxWidth()) {
                    val activeDebtorName = roommates.find { it.id == selectedDebtorId }?.name ?: stringResource(R.string.expense_form_payer_select)
                    OutlinedTextField(
                        value = activeDebtorName,
                        onValueChange = {},
                        label = { Text(stringResource(R.string.settle_up_dialog_debtor_label)) },
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedDebtor = true }
                            .testTag("settle_debtor_input"),
                        trailingIcon = {
                            IconButton(onClick = { expandedDebtor = !expandedDebtor }) {
                                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = expandedDebtor,
                        onDismissRequest = { expandedDebtor = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        roommates.forEach { rm ->
                            DropdownMenuItem(
                                text = { Text(rm.name) },
                                onClick = {
                                    selectedDebtorId = rm.id
                                    expandedDebtor = false
                                    // ensure debtor and creditor are not the same
                                    if (selectedDebtorId == selectedCreditorId) {
                                        selectedCreditorId = roommates.find { it.id != selectedDebtorId }?.id ?: ""
                                    }
                                }
                            )
                        }
                    }
                }

                // Creditor Dropdown (who receives)
                Box(modifier = Modifier.fillMaxWidth()) {
                    val activeCreditorName = roommates.find { it.id == selectedCreditorId }?.name ?: stringResource(R.string.expense_form_payer_select)
                    OutlinedTextField(
                        value = activeCreditorName,
                        onValueChange = {},
                        label = { Text(stringResource(R.string.settle_up_dialog_creditor_label)) },
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedCreditor = true }
                            .testTag("settle_creditor_input"),
                        trailingIcon = {
                            IconButton(onClick = { expandedCreditor = !expandedCreditor }) {
                                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = expandedCreditor,
                        onDismissRequest = { expandedCreditor = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        roommates.forEach { rm ->
                            if (rm.id != selectedDebtorId) { // Prevent paying oneself
                                DropdownMenuItem(
                                    text = { Text(rm.name) },
                                    onClick = {
                                        selectedCreditorId = rm.id
                                        expandedCreditor = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Amount
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = {
                        amountStr = it
                        amountError = false
                    },
                    label = { Text(stringResource(R.string.settle_up_dialog_amount_transferred_label, currencySymbol)) },
                    placeholder = { Text("0.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settle_amount_input"),
                    isError = amountError,
                    supportingText = {
                        if (amountError) {
                            if (selectedDebtorId == selectedCreditorId) {
                                Text(stringResource(R.string.settle_up_dialog_same_person_error))
                            } else {
                                Text(stringResource(R.string.settle_up_dialog_invalid_amount_error))
                            }
                        }
                    },
                    singleLine = true
                )
            }
        }
    )
}
