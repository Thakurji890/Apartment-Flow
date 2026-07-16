package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BillCategory
import com.example.data.Roommate
import androidx.compose.ui.res.stringResource
import com.example.R
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BillCategory.getLocalizedName(): String {
    return when (this) {
        BillCategory.GROCERIES -> stringResource(R.string.bills_groceries)
        BillCategory.UTILITIES -> stringResource(R.string.bills_utilities)
        BillCategory.RENT -> stringResource(R.string.bills_rent)
        BillCategory.OTHERS -> stringResource(R.string.bills_others)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseEntryForm(
    roommates: List<Roommate>,
    initialTitle: String = "",
    initialAmount: String = "",
    initialCategory: BillCategory = BillCategory.GROCERIES,
    initialDate: String = "",
    initialPayerId: String = "",
    initialDescription: String = "",
    onCancelClick: () -> Unit,
    onSaveClick: (title: String, amount: Double, category: BillCategory, date: String, payerId: String, description: String, splitAmongIds: List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    // State management for form fields
    var title by remember { mutableStateOf(initialTitle) }
    var amountStr by remember { mutableStateOf(initialAmount) }
    var category by remember { mutableStateOf(initialCategory) }
    var payerId by remember { mutableStateOf(initialPayerId.ifEmpty { roommates.firstOrNull()?.id ?: "" }) }
    var description by remember { mutableStateOf(initialDescription) }

    val selectedParticipants = remember { mutableStateListOf<String>() }
    LaunchedEffect(roommates) {
        if (selectedParticipants.isEmpty()) {
            selectedParticipants.addAll(roommates.map { it.id })
        }
    }

    // Date state management
    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    var dateStr by remember { 
        mutableStateOf(
            initialDate.ifEmpty { dateFormatter.format(Date()) }
        ) 
    }

    // Validation errors
    var titleError by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }
    var dateError by remember { mutableStateOf(false) }
    var participantsError by remember { mutableStateOf(false) }

    // Dropdowns and Dialog visibility
    var expandedPayer by remember { mutableStateOf(false) }
    var showDatePickerDialog by remember { mutableStateOf(false) }

    // DatePicker state
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = try {
            dateFormatter.parse(dateStr)?.time
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    )

    // Handle date selection from dialog
    if (showDatePickerDialog) {
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedDate = Date(millis)
                            dateStr = dateFormatter.format(selectedDate)
                            dateError = false
                        }
                        showDatePickerDialog = false
                    },
                    modifier = Modifier.testTag("date_picker_confirm")
                ) {
                    Text(stringResource(R.string.action_ok))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePickerDialog = false },
                    modifier = Modifier.testTag("date_picker_cancel")
                ) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Form Fields Column
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Expense Name / Title Input
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    titleError = false
                },
                label = { Text(stringResource(R.string.expense_form_name_label)) },
                placeholder = { Text(stringResource(R.string.expense_form_name_hint)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Label,
                        contentDescription = stringResource(R.string.cd_title_icon),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("expense_form_title_input"),
                isError = titleError,
                supportingText = {
                    if (titleError) {
                        Text(stringResource(R.string.expense_form_name_empty_error))
                    }
                },
                singleLine = true
            )

            // Amount Input
            OutlinedTextField(
                value = amountStr,
                onValueChange = {
                    amountStr = it
                    amountError = false
                },
                label = { Text(stringResource(R.string.expense_form_amount_label, "$")) },
                placeholder = { Text(stringResource(R.string.expense_form_amount_hint)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.AttachMoney,
                        contentDescription = stringResource(R.string.cd_amount_icon),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("expense_form_amount_input"),
                isError = amountError,
                supportingText = {
                    if (amountError) {
                        Text(stringResource(R.string.expense_form_amount_invalid_error))
                    }
                },
                singleLine = true
            )

            // Date Input Field (Tapping anywhere on the field opens the M3 DatePicker)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePickerDialog = true }
            ) {
                OutlinedTextField(
                    value = dateStr,
                    onValueChange = {},
                    label = { Text(stringResource(R.string.expense_form_date_label)) },
                    readOnly = true,
                    enabled = false,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = stringResource(R.string.cd_calendar_icon),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = stringResource(R.string.cd_calendar_icon),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_form_date_input"),
                    isError = dateError,
                    supportingText = {
                        if (dateError) {
                            Text(stringResource(R.string.expense_form_date_invalid_error))
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = if (dateError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledLabelColor = if (dateError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledSupportingTextColor = MaterialTheme.colorScheme.error
                    ),
                    singleLine = true
                )
            }

            // Category Selection (Visual Chips Matrix)
            Text(
                text = stringResource(R.string.expense_form_category_label),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("expense_form_category_group"),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BillCategory.values().forEach { cat ->
                    val isSelected = category == cat
                    val chipBg = if (isSelected) Color(cat.bgColor) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    val chipText = if (isSelected) Color(cat.textColor) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    val chipBorder = if (isSelected) BorderStroke(1.5.dp, Color(cat.textColor)) else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(chipBg)
                            .clickable { category = cat }
                            .padding(vertical = 10.dp)
                            .testTag("expense_form_category_chip_${cat.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val icon = when (cat) {
                                BillCategory.GROCERIES -> Icons.Default.ShoppingCart
                                BillCategory.UTILITIES -> Icons.Default.ElectricBolt
                                BillCategory.RENT -> Icons.Default.Home
                                BillCategory.OTHERS -> Icons.Default.Payments
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = cat.getLocalizedName(),
                                tint = chipText,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = cat.getLocalizedName(),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = chipText,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Who Paid Dropdown (Tapping anywhere on the field opens the dropdown menu)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedPayer = true }
            ) {
                val activePayerName = roommates.find { it.id == payerId }?.name ?: stringResource(R.string.expense_form_payer_select)
                OutlinedTextField(
                    value = activePayerName,
                    onValueChange = {},
                    label = { Text(stringResource(R.string.expense_form_payer_label)) },
                    readOnly = true,
                    enabled = false,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = stringResource(R.string.cd_payer_icon),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_form_payer_input"),
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = stringResource(R.string.cd_dropdown_arrow),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    singleLine = true
                )
                
                DropdownMenu(
                    expanded = expandedPayer,
                    onDismissRequest = { expandedPayer = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    roommates.forEach { rm ->
                        DropdownMenuItem(
                            text = { Text(rm.name) },
                            onClick = {
                                payerId = rm.id
                                expandedPayer = false
                            },
                            modifier = Modifier.testTag("expense_form_payer_option_${rm.id}")
                        )
                    }
                }
            }

            // Short Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(R.string.expense_form_desc_label)) },
                placeholder = { Text(stringResource(R.string.expense_form_desc_hint)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = stringResource(R.string.cd_desc_icon),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("expense_form_description_input"),
                singleLine = true
            )

            // Split Between / Participants Checkbox List
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.expense_form_split_label),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )
                if (participantsError) {
                    Text(
                        text = "Select at least one",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("expense_form_split_participants"),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                roommates.forEach { rm ->
                    val isChecked = selectedParticipants.contains(rm.id)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isChecked) {
                                    if (selectedParticipants.size > 1) {
                                        selectedParticipants.remove(rm.id)
                                    }
                                } else {
                                    selectedParticipants.add(rm.id)
                                }
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    selectedParticipants.add(rm.id)
                                } else {
                                    if (selectedParticipants.size > 1) {
                                        selectedParticipants.remove(rm.id)
                                    }
                                }
                            },
                            modifier = Modifier.testTag("split_checkbox_${rm.id}")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = rm.name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Action Buttons Row (Save and Cancel)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancelClick,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("expense_form_cancel_button")
            ) {
                Text(stringResource(R.string.action_cancel))
            }

            Button(
                onClick = {
                    val parsedAmount = amountStr.toDoubleOrNull()
                    titleError = title.isBlank()
                    amountError = parsedAmount == null || parsedAmount <= 0.0

                    // Format date check
                    val dateParsed = try {
                        dateFormatter.isLenient = false
                        dateFormatter.parse(dateStr)
                        true
                    } catch (e: Exception) {
                        false
                    }
                    dateError = !dateParsed
                    participantsError = selectedParticipants.isEmpty()

                    if (!titleError && !amountError && !dateError && !participantsError) {
                        onSaveClick(title, parsedAmount!!, category, dateStr, payerId, description, selectedParticipants.toList())
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("expense_form_save_button")
            ) {
                Text(stringResource(R.string.expense_form_save_button))
            }
        }
    }
}
