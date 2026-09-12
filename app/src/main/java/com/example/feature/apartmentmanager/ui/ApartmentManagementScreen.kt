package com.example.feature.apartmentmanager.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.feature.apartmentmanager.model.NotificationCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApartmentManagementScreen(
    viewModel: ApartmentManagerViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val balanceSummaries = remember(state.expenses, state.settlements, state.roommates) {
        viewModel.getBalanceSummaries()
    }
    val settlementSuggestions = remember(state.expenses, state.settlements, state.roommates) {
        viewModel.getDebtSettlementSuggestions()
    }
    val totalPoolSpending = remember(state.expenses) {
        viewModel.getTotalPoolSpending()
    }
    val activeRoommate = state.activeRoommate

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.statusMessage) {
        state.statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearStatusMessage()
        }
    }

    // Universal Back Navigation: if not on Balances tab (tab 0), hardware Back takes user back to tab 0
    BackHandler(enabled = state.selectedTab != 0) {
        viewModel.selectTab(0)
    }

    var userDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                navigationIcon = {
                    if (state.selectedTab != 0) {
                        IconButton(onClick = { viewModel.selectTab(0) }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to Balances",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                },
                title = {
                    Column {
                        val tabTitle = when (state.selectedTab) {
                            1 -> "Expenses & Groceries"
                            2 -> "Settlements"
                            3 -> "Admin Controls"
                            else -> state.profile.name
                        }
                        val tabSubtitle = when (state.selectedTab) {
                            1 -> "${state.expenses.size} purchases • ${state.profile.name}"
                            2 -> "${state.settlements.size} records • ${state.pendingSettlementCount} pending"
                            3 -> "Flat ${state.profile.flatNumber} • Admin Settings"
                            else -> "Flat ${state.profile.flatNumber}"
                        }
                        Text(
                            text = tabTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = tabSubtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                actions = {
                    // Proportional Rent Splitter Shortcut
                    IconButton(onClick = { viewModel.openRentCalculatorDialog() }) {
                        Icon(
                            Icons.Default.Calculate,
                            contentDescription = "Proportional Rent Splitter",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    // Notification Activity Bell with Unread Badge
                    IconButton(onClick = { viewModel.openNotificationsDialog() }) {
                        BadgedBox(
                            badge = {
                                if (state.unreadNotificationCount > 0) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = MaterialTheme.colorScheme.onError
                                    ) {
                                        Text(state.unreadNotificationCount.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = "Apartment Activity and Notifications",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }

                    // Active Roommate Identity Pill with Dropdown
                    Box {
                        Surface(
                            onClick = { userDropdownExpanded = true },
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(Color(activeRoommate?.colorHex ?: 0xFFFFFFFFL)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = activeRoommate?.name?.take(1) ?: "U",
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = activeRoommate?.name ?: "User",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.widthIn(max = 75.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = userDropdownExpanded,
                            onDismissRequest = { userDropdownExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Switch Viewing Profile:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) },
                                onClick = {},
                                enabled = false
                            )
                            state.roommates.forEach { rm ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(14.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(rm.colorHex))
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(rm.name)
                                            if (rm.isAdmin) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("(Admin)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                    },
                                    onClick = {
                                        viewModel.setActiveRoommate(rm.id)
                                        userDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = state.selectedTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    icon = { Icon(Icons.Default.Assessment, contentDescription = "Balances") },
                    label = { Text("Balances") }
                )
                NavigationBarItem(
                    selected = state.selectedTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (state.pendingExpenseCount > 0) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.tertiary,
                                        contentColor = MaterialTheme.colorScheme.onTertiary
                                    ) {
                                        Text(state.pendingExpenseCount.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Expenses")
                        }
                    },
                    label = { Text("Expenses") }
                )
                NavigationBarItem(
                    selected = state.selectedTab == 2,
                    onClick = { viewModel.selectTab(2) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (state.pendingSettlementCount > 0) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = MaterialTheme.colorScheme.onError
                                    ) {
                                        Text(state.pendingSettlementCount.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.SyncAlt, contentDescription = "Settlements")
                        }
                    },
                    label = { Text("Settlements") }
                )
                NavigationBarItem(
                    selected = state.selectedTab == 3,
                    onClick = { viewModel.selectTab(3) },
                    icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin") },
                    label = { Text("Admin") }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (state.selectedTab) {
                0 -> BalancesSummaryTab(
                    state = state,
                    balanceSummaries = balanceSummaries,
                    settlementSuggestions = settlementSuggestions,
                    totalPoolSpending = totalPoolSpending,
                    onQuickSettle = { fromId, toId, amount ->
                        viewModel.openAddSettlementDialog(fromId, toId, amount)
                    },
                    onBatchSettleAll = { transfers ->
                        viewModel.recordBatchSettlements(transfers)
                    },
                    onAddExpenseClick = { viewModel.openAddExpenseDialog() },
                    onOpenRentCalculator = { viewModel.openRentCalculatorDialog() },
                    onOpenEnvelopeBudget = { viewModel.openEnvelopeBudgetDialog(it) }
                )
                1 -> ExpensesGroceriesTab(
                    state = state,
                    onFilterRoommate = { viewModel.setExpenseFilterRoommateId(it) },
                    onSearchQueryChange = { viewModel.setSearchQuery(it) },
                    onAddExpense = { viewModel.openAddExpenseDialog() },
                    onEditExpense = { viewModel.openEditExpenseDialog(it) },
                    onDeleteExpense = { viewModel.deleteExpense(it) },
                    onApproveExpense = { viewModel.approveExpense(it) },
                    onRejectExpense = { id, reason -> viewModel.rejectExpense(id, reason) }
                )
                2 -> SettlementsTab(
                    state = state,
                    onAddSettlement = { viewModel.openAddSettlementDialog() },
                    onApproveSettlement = { viewModel.approveSettlement(it) },
                    onRejectSettlement = { id, reason -> viewModel.rejectSettlement(id, reason) },
                    onDeleteSettlement = { viewModel.deleteSettlement(it) }
                )
                3 -> AdminControlTab(
                    state = state,
                    balanceSummaries = balanceSummaries,
                    settlementSuggestions = settlementSuggestions,
                    onSetActiveRoommate = { viewModel.setActiveRoommate(it) },
                    onEditApartment = { viewModel.openEditApartmentDialog() },
                    onAddRoommate = { viewModel.openAddRoommateDialog() },
                    onEditRoommate = { viewModel.openEditRoommateDialog(it) },
                    onDeleteRoommate = { viewModel.deleteRoommate(it) },
                    onResetToDefault = { viewModel.openResetConfirmationDialog() },
                    onApproveExpense = { viewModel.approveExpense(it) },
                    onRejectExpense = { id, reason -> viewModel.rejectExpense(id, reason) },
                    onApproveSettlement = { viewModel.approveSettlement(it) },
                    onRejectSettlement = { id, reason -> viewModel.rejectSettlement(id, reason) },
                    onOpenEnvelopeBudget = { viewModel.openEnvelopeBudgetDialog(it) },
                    onOpenRentCalculator = { viewModel.openRentCalculatorDialog() }
                )
            }
        }
    }

    // --- Dialogs with Back Navigation & Non-overlapping Layouts ---
    if (state.showNotificationsDialog) {
        ApartmentNotificationsDialog(
            notifications = state.notifications,
            onDismiss = { viewModel.closeNotificationsDialog() },
            onMarkAllAsRead = { viewModel.markAllNotificationsAsRead() },
            onClearAll = { viewModel.clearNotifications() },
            onNavigateToSettlements = {
                viewModel.selectTab(2)
            }
        )
    }

    if (state.showAddExpenseDialog) {
        AddEditExpenseDialog(
            expense = state.editingExpense,
            roommates = state.roommates,
            currencySymbol = state.profile.currencySymbol,
            onDismiss = { viewModel.closeExpenseDialog() },
            onSave = { date, item, amount, paidById, sharedByIds, notes, category ->
                viewModel.saveExpense(date, item, amount, paidById, sharedByIds, notes, category)
            }
        )
    }

    if (state.showAddSettlementDialog) {
        AddSettlementDialog(
            roommates = state.roommates,
            currencySymbol = state.profile.currencySymbol,
            isAdmin = state.isActiveUserAdmin,
            prefillFromId = state.settlementPrefillFromId,
            prefillToId = state.settlementPrefillToId,
            prefillAmount = state.settlementPrefillAmount,
            onDismiss = { viewModel.closeSettlementDialog() },
            onSave = { date, fromId, toId, amount, note ->
                viewModel.saveSettlement(date, fromId, toId, amount, note)
            }
        )
    }

    if (state.showAddRoommateDialog) {
        AddEditRoommateDialog(
            roommate = state.editingRoommate,
            onDismiss = { viewModel.closeRoommateDialog() },
            onSave = { name, notes, isAdmin ->
                viewModel.saveRoommate(name, notes, isAdmin)
            }
        )
    }

    if (state.showEditApartmentDialog) {
        EditApartmentProfileDialog(
            profile = state.profile,
            onDismiss = { viewModel.closeEditApartmentDialog() },
            onSave = { name, flatNumber, currencySymbol, currencyCode, inviteCode ->
                viewModel.updateApartmentProfile(name, flatNumber, currencySymbol, currencyCode, inviteCode)
            }
        )
    }

    if (state.showResetConfirmationDialog) {
        BackHandler { viewModel.closeResetConfirmationDialog() }

        AlertDialog(
            onDismissRequest = { viewModel.closeResetConfirmationDialog() },
            title = { Text("Reset to Original Sheet Data?") },
            text = { Text("This will restore all 5 roommates (Aniket, Amit, Rahul, Debang, Akshay), 48 grocery expenses, and 36 settlements from the original spreadsheet. Any custom edits will be replaced.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.resetToDefaultSheetData() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeResetConfirmationDialog() }) {
                    Text("Back")
                }
            }
        )
    }

    if (state.showEnvelopeBudgetDialog) {
        SharedEnvelopeBudgetDialog(
            budget = state.editingEnvelopeBudget,
            currencySymbol = state.profile.currencySymbol,
            onDismiss = { viewModel.closeEnvelopeBudgetDialog() },
            onSave = { category, monthlyCap, alertThresholdPercent, notes ->
                viewModel.setEnvelopeBudget(category, monthlyCap, alertThresholdPercent, notes)
            },
            onDelete = { budgetId ->
                viewModel.removeEnvelopeBudget(budgetId)
            }
        )
    }

    if (state.showRentCalculatorDialog) {
        ProportionalRentCalculatorDialog(
            roommates = state.roommates,
            initialRent = state.profile.monthlyRent,
            currencySymbol = state.profile.currencySymbol,
            onDismiss = { viewModel.closeRentCalculatorDialog() },
            onRecordExpense = { totalAmount, desc, shares ->
                viewModel.recordProportionalRentSplit(totalAmount, desc, shares)
            }
        )
    }
}
