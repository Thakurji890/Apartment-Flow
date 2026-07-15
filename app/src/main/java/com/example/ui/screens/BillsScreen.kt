package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Bill
import com.example.data.BillCategory
import com.example.data.Roommate
import com.example.ui.ApartmentViewModel
import com.example.ui.components.ExpenseEntryForm
import androidx.compose.ui.res.stringResource
import com.example.R
import java.util.Locale

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
fun BillsScreen(
    viewModel: ApartmentViewModel,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val bills by viewModel.filteredBills.collectAsState()
    val actualBillsOnly by viewModel.actualBillsOnly.collectAsState()
    val roommates by viewModel.roommates.collectAsState()
    val currentFilter by viewModel.selectedCategoryFilter.collectAsState()
    val totalSpent by viewModel.totalSpent.collectAsState()
    val currentCurrency by viewModel.currency.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (onBackClick != null) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier.testTag("bills_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                    Column {
                        Text(
                            text = stringResource(R.string.bills_screen_title),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = stringResource(R.string.bills_active_count_template, actualBillsOnly.size),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                }

                // Quick stats of total spent
                Text(
                    text = viewModel.formatCurrency(totalSpent, currentCurrency),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Category Filter Pills Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("filter_row"),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    FilterChip(
                        selected = currentFilter == null,
                        onClick = { viewModel.selectCategoryFilter(null) },
                        label = { Text(stringResource(R.string.bills_all)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("filter_all_chip")
                    )
                }

                items(BillCategory.values().toList()) { category ->
                    FilterChip(
                        selected = currentFilter == category,
                        onClick = { viewModel.selectCategoryFilter(category) },
                        label = { Text(category.getLocalizedName()) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(category.bgColor),
                            selectedLabelColor = Color(category.textColor),
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier.testTag("filter_chip_${category.name.lowercase()}")
                    )
                }
            }

            // Bills List
            if (bills.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = "Empty",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                        )
                        Text(
                            text = stringResource(R.string.bills_no_expenses),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                        Text(
                            text = stringResource(R.string.bills_tap_plus_hint),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .testTag("bills_list"),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(bills, key = { it.id }) { bill ->
                        val isCashTransfer = bill.title == "Cash Settlement"
                        val categoryBg = if (isCashTransfer) 0xFFF3F4F9 else bill.category.bgColor
                        val categoryText = if (isCashTransfer) 0xFF1A1C1E else bill.category.textColor
                        
                        val payerName = roommates.find { it.id == bill.payerId }?.name ?: "Unknown"

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("bill_item_${bill.id}"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Category Icon Indicator
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color(categoryBg)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val icon = when {
                                        isCashTransfer -> Icons.Default.Handshake
                                        bill.category == BillCategory.GROCERIES -> Icons.Default.ShoppingCart
                                        bill.category == BillCategory.UTILITIES -> Icons.Default.ElectricBolt
                                        bill.category == BillCategory.RENT -> Icons.Default.Home
                                        else -> Icons.Default.Payments
                                    }
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = bill.category.displayName,
                                        tint = Color(categoryText),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                // Bill Details
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = if (isCashTransfer) stringResource(R.string.bills_cash_settlement) else bill.title,
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.bills_paid_by_template, payerName),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(3.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                                        )
                                        Text(
                                            text = bill.date,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                    }
                                    if (bill.description.isNotEmpty() && !isCashTransfer) {
                                        Text(
                                            text = bill.description,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                            maxLines = 1,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                // Amount & Action
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = viewModel.formatCurrency(bill.amount, currentCurrency),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCashTransfer) Color(0xFF386A20) else MaterialTheme.colorScheme.onSurface
                                    )

                                    IconButton(
                                        onClick = { viewModel.deleteBill(bill.id) },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .testTag("delete_bill_${bill.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Delete Expense",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button to Add Bill
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp, end = 16.dp)
                .testTag("add_bill_fab"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.home_add_bill)
            )
        }

        // Add Bill Dialog
        if (showAddDialog) {
            AddBillDialog(
                roommates = roommates,
                onDismiss = { showAddDialog = false },
                onAddBill = { title, amount, category, payerId, description, date, splitAmongIds ->
                    viewModel.addBill(title, amount, category, payerId, description, date, splitAmongIds)
                    showAddDialog = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBillDialog(
    roommates: List<Roommate>,
    onDismiss: () -> Unit,
    onAddBill: (String, Double, BillCategory, String, String, String, List<String>) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.padding(16.dp),
        content = {
            Surface(
                shape = RoundedCornerShape(28.dp),
                tonalElevation = 6.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.widthIn(max = 560.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.bills_add_shared_title),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    ExpenseEntryForm(
                        roommates = roommates,
                        onCancelClick = onDismiss,
                        onSaveClick = { title, amount, category, date, payerId, description, splitAmongIds ->
                            onAddBill(title, amount, category, payerId, description, date, splitAmongIds)
                        }
                    )
                }
            }
        }
    )
}
