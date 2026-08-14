package com.example.feature.dashboard.presentation.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.feature.apartment.domain.model.ApartmentMember
import com.example.feature.dashboard.domain.model.ActivityItem
import com.example.feature.expense.domain.model.Expense
import com.example.feature.expensesplit.domain.model.Debt
import java.time.format.DateTimeFormatter
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale

import androidx.compose.material3.pulltorefresh.PullToRefreshBox

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeDashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateToExpenseDetails: (String) -> Unit,
    onNavigateToAddExpense: () -> Unit,
    onNavigateToSettlements: () -> Unit,
    onNavigateToExpenses: () -> Unit,
    onNavigateToApartment: () -> Unit,
    onNavigateToMemberDetails: (String) -> Unit,
    onNavigateToRecurringBills: () -> Unit,
    onNavigateToRecurringBillDetails: (String) -> Unit,
    onNavigateToShoppingLists: () -> Unit,
    onNavigateToChores: () -> Unit,
    onNavigateToNotifications: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = getGreeting(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = state.currentUser?.displayName ?: "User",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (state.isOffline) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.errorContainer,
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(
                                        text = "Offline",
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Notifications */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                    }
                    IconButton(onClick = { /* TODO: Profile */ }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profile")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddExpense) {
                Icon(Icons.Default.Add, contentDescription = "Add Expense")
            }
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isLoading && state.recentExpenses.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = state.searchQuery,
                            onValueChange = { viewModel.updateSearchQuery(it) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Search dashboard...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                            shape = MaterialTheme.shapes.extraLarge
                        )
                    }

                    item {
                        ApartmentSwitcher(
                            apartmentName = state.apartmentName,
                            apartments = state.apartments,
                            onApartmentSelected = { /* TODO: handle switch */ }
                        )
                    }

                item {
                    FinancialSummaryCard(state)
                }
                
                item {
                    QuickActionsRow(
                        onAddExpense = onNavigateToAddExpense,
                        onSettleUp = onNavigateToSettlements,
                        onViewExpenses = onNavigateToExpenses,
                        onApartment = onNavigateToApartment,
                        onNavigateToShoppingLists = onNavigateToShoppingLists,
                        onNavigateToChores = onNavigateToChores
                    )
                }

                item {
                    MonthlySummaryCard(
                        state = state,
                        onPreviousMonth = { viewModel.previousMonth() },
                        onNextMonth = { viewModel.nextMonth() }
                    )
                }

                if (state.outstandingDebts.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Who Owes Whom", onActionClick = onNavigateToSettlements)
                    }
                    items(state.outstandingDebts.take(3)) { debt ->
                        DebtItem(
                            debt = debt,
                            members = state.members,
                            currentUserId = state.currentUser?.uid ?: ""
                        )
                    }
                }

                item {
                    SectionHeader(title = "Recent Expenses", onActionClick = onNavigateToExpenses)
                }
                if (state.recentExpenses.isEmpty()) {
                    item {
                        Text("No recent expenses", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    items(state.recentExpenses) { expense ->
                        ExpenseItem(expense = expense, onClick = { onNavigateToExpenseDetails(expense.expenseId) })
                    }
                }

                item {
                    SettlementSummaryCard(state)
                }

                if (state.recurringBills.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Upcoming Bills", onActionClick = onNavigateToRecurringBills)
                    }
                    items(state.recurringBills.take(3)) { bill ->
                        RecurringBillItem(bill = bill, onClick = { onNavigateToRecurringBillDetails(bill.id) })
                    }
                }
                item {

                    SectionHeader(title = "Recent Activity")
                }
                items(state.recentActivities) { activity ->
                    ActivityListItem(activity = activity, members = state.members)
                }

                item {
                    SectionHeader(title = "Members")
                }
                items(state.members) { member ->
                    val balance = state.memberBalances.find { it.userId == member.userId }
                    MemberItem(
                        member = member,
                        netBalance = balance?.netBalance ?: 0.0,
                        onClick = { onNavigateToMemberDetails(member.userId) }
                    )
                }
            }
        }
    }
}
}

@Composable
fun ApartmentSwitcher(
    apartmentName: String,
    apartments: List<com.example.feature.apartment.domain.model.Apartment>,
    onApartmentSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box {
        Surface(
            onClick = { expanded = true },
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = MaterialTheme.shapes.medium
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Home, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = apartmentName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Switch Apartment")
            }
        }
        
        androidx.compose.material3.DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            apartments.forEach { apartment ->
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text(apartment.name) },
                    onClick = {
                        expanded = false
                        onApartmentSelected(apartment.id)
                    }
                )
            }
        }
    }
}

@Composable
fun FinancialSummaryCard(state: DashboardUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Net Balance",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
            
            val balanceColor = when {
                state.netBalance > 0 -> MaterialTheme.colorScheme.primary
                state.netBalance < 0 -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.onPrimaryContainer
            }
            val balancePrefix = if (state.netBalance > 0) "+" else ""
            
            Text(
                text = "$balancePrefix$${String.format(Locale.getDefault(), "%.2f", state.netBalance)}",
                style = MaterialTheme.typography.displaySmall,
                color = balanceColor,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Total Spent", style = MaterialTheme.typography.labelSmall)
                    Text("$${String.format(Locale.getDefault(), "%.2f", state.totalSpent)}", style = MaterialTheme.typography.titleMedium)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("You Owe", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                    Text("$${String.format(Locale.getDefault(), "%.2f", state.amountYouOwe)}", style = MaterialTheme.typography.titleMedium)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Owed To You", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Text("$${String.format(Locale.getDefault(), "%.2f", state.amountOwedToYou)}", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
fun MonthlySummaryCard(state: DashboardUiState, onPreviousMonth: () -> Unit, onNextMonth: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${state.currentMonth.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${state.currentMonth.year} Summary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onPreviousMonth) {
                        Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Previous Month")
                    }
                    IconButton(onClick = onNextMonth, enabled = state.currentMonth.isBefore(java.time.YearMonth.now())) {
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Next Month")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                SummaryStat(modifier = Modifier.weight(1f), label = "Total Expenses", value = "$${String.format(Locale.getDefault(), "%.2f", state.currentMonthExpenses)}")
                SummaryStat(modifier = Modifier.weight(1f), label = "Avg Daily", value = "$${String.format(Locale.getDefault(), "%.2f", state.averageDailySpending)}")
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                SummaryStat(modifier = Modifier.weight(1f), label = "Highest", value = "$${String.format(Locale.getDefault(), "%.2f", state.highestExpense)}")
                SummaryStat(modifier = Modifier.weight(1f), label = "Expenses #", value = "${state.numberOfExpenses}")
            }
        }
    }
}

@Composable
fun SummaryStat(modifier: Modifier = Modifier, label: String, value: String) {
    Column(modifier = modifier) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun SettlementSummaryCard(state: DashboardUiState) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Settlement Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Pending Settlements", color = MaterialTheme.colorScheme.onTertiaryContainer)
                Text("${state.pendingSettlementsCount}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Settled This Month", color = MaterialTheme.colorScheme.onTertiaryContainer)
                Text("${state.settledThisMonthCount}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer)
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, onActionClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        if (onActionClick != null) {
            TextButton(onClick = onActionClick) {
                Text("View All")
            }
        }
    }
}

@Composable
fun QuickActionsRow(
    onAddExpense: () -> Unit,
    onSettleUp: () -> Unit,
    onViewExpenses: () -> Unit,
    onApartment: () -> Unit,
    onNavigateToShoppingLists: () -> Unit,
    onNavigateToChores: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        QuickActionItem(icon = Icons.Default.Add, label = "Expense", onClick = onAddExpense)
        QuickActionItem(icon = Icons.Default.CheckCircle, label = "Settle", onClick = onSettleUp)
        QuickActionItem(icon = Icons.Default.List, label = "List", onClick = onViewExpenses)
        QuickActionItem(icon = Icons.Default.Settings, label = "Apartment", onClick = onApartment)
        QuickActionItem(icon = Icons.Default.ShoppingCart, label = "Shop", onClick = onNavigateToShoppingLists)
        QuickActionItem(icon = Icons.Default.CheckCircle, label = "Chores", onClick = onNavigateToChores)
    }
}

@Composable
fun QuickActionItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick).padding(8.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun ExpenseItem(expense: Expense, onClick: () -> Unit) {
    val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = expense.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(text = "Paid by ${expense.paidBy}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "$${String.format(Locale.getDefault(), "%.2f", expense.amount)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = formatter.format(Date(expense.expenseDate)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun DebtItem(debt: Debt, members: List<ApartmentMember>, currentUserId: String) {
    val debtor = members.find { it.userId == debt.debtorId }?.displayName ?: "Someone"
    val creditor = members.find { it.userId == debt.creditorId }?.displayName ?: "Someone"
    
    val text = if (debt.debtorId == currentUserId) {
        "You owe $creditor"
    } else if (debt.creditorId == currentUserId) {
        "$debtor owes you"
    } else {
        "$debtor owes $creditor"
    }
    
    val color = if (debt.creditorId == currentUserId) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
        Text(text = "$${String.format(Locale.getDefault(), "%.2f", debt.amount)}", style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ActivityListItem(activity: ActivityItem, members: List<ApartmentMember>) {
    val userName = members.find { it.userId == activity.userId }?.displayName ?: "Someone"
    val formatter = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = "$userName ${activity.title}", style = MaterialTheme.typography.bodyMedium)
            Text(text = formatter.format(Date(activity.timestamp)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun MemberItem(member: ApartmentMember, netBalance: Double, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = member.displayName.take(1).uppercase(), color = MaterialTheme.colorScheme.onPrimary)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = member.displayName, style = MaterialTheme.typography.bodyLarge)
        }
        
        val balanceColor = when {
            netBalance > 0 -> MaterialTheme.colorScheme.primary
            netBalance < 0 -> MaterialTheme.colorScheme.error
            else -> MaterialTheme.colorScheme.onSurface
        }
        val prefix = if (netBalance > 0) "+" else ""
        Text(text = "$prefix$${String.format(Locale.getDefault(), "%.2f", netBalance)}", style = MaterialTheme.typography.bodyMedium, color = balanceColor, fontWeight = FontWeight.Bold)
    }
}

private fun getGreeting(): String {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 0..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        else -> "Good Evening"
    }
}

@Composable
fun RecurringBillItem(bill: com.example.feature.recurringbill.domain.model.RecurringBill, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Recurring Bill",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = bill.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                val formatter = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                Text(
                    text = "Next: ${formatter.format(java.util.Date(bill.nextDueDate))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "$${String.format("%.2f", bill.expectedAmount)}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
