package com.example.feature.apartmentmanager.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.feature.apartmentmanager.domain.DebtSimplificationEngine
import com.example.feature.apartmentmanager.model.*
import kotlin.math.abs

@Composable
fun BalancesSummaryTab(
    state: ApartmentUiState,
    balanceSummaries: List<RoommateBalanceSummary>,
    settlementSuggestions: List<DebtTransfer>,
    totalPoolSpending: Double,
    onQuickSettle: (fromId: String, toId: String, amount: Double) -> Unit,
    onBatchSettleAll: (List<DebtTransfer>) -> Unit = {},
    onAddExpenseClick: () -> Unit,
    onOpenEnvelopeBudget: (SharedEnvelopeBudget?) -> Unit = {},
    onNavigateToExpenses: () -> Unit = {}
) {
    val currency = state.profile.currencySymbol
    val currencyCode = state.profile.currencyCode
    var selectedRoommateForDetail by remember { mutableStateOf<RoommateBalanceSummary?>(null) }
    var showBatchConfirmDialog by remember { mutableStateOf(false) }
    var envelopesExpanded by remember { mutableStateOf(false) }

    val adminNames = remember(state.roommates) {
        state.roommates.filter { it.isAdmin }.joinToString { it.name }.ifEmpty { "Flat Admin" }
    }

    val activeSummary = remember(balanceSummaries, state.activeRoommateId) {
        balanceSummaries.find { it.roommate.id == state.activeRoommateId }
    }

    // When the active user is a Roommate (non-admin), show their personal remaining balance & pay-to-others dashboard
    if (!state.isActiveUserAdmin) {
        RoommateBalancesView(
            state = state,
            activeSummary = activeSummary,
            settlementSuggestions = settlementSuggestions,
            currency = currency,
            adminNames = adminNames,
            onQuickSettle = onQuickSettle,
            onAddExpenseClick = onAddExpenseClick,
            onNavigateToExpenses = onNavigateToExpenses
        )
        return
    }

    val simplificationResult = remember(balanceSummaries) {
        DebtSimplificationEngine.simplifyDebts(balanceSummaries)
    }
    val metrics = simplificationResult.metrics

    val debtors = remember(balanceSummaries) {
        balanceSummaries.filter { it.netBalance < -0.01 }.sortedBy { it.netBalance }
    }

    val creditors = remember(balanceSummaries) {
        balanceSummaries.filter { it.netBalance > 0.01 }.sortedByDescending { it.netBalance }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // --- Total Apartment Pool Spending Card ---
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Household Spending",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "$currency${"%,.2f".format(totalPoolSpending)}",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${state.roommates.size} Roommates • ${state.expenses.size} Logs",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    // Quick Primary Actions inside the Hero Card
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onAddExpenseClick,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Expense", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                if (settlementSuggestions.isNotEmpty()) {
                                    val first = settlementSuggestions.first()
                                    onQuickSettle(first.fromRoommate.id, first.toRoommate.id, first.amount)
                                } else {
                                    val firstRoommate = state.roommates.firstOrNull()
                                    val secondRoommate = state.roommates.getOrNull(1)
                                    if (firstRoommate != null && secondRoommate != null) {
                                        onQuickSettle(firstRoommate.id, secondRoommate.id, 0.0)
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Icon(Icons.Default.SyncAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Settle Up", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // --- Shared Envelope Budgets & Spending Progress ---
        item {
            val totalBudgetCap = remember(state.envelopeBudgets) {
                state.envelopeBudgets.sumOf { it.monthlyCap }
            }
            val totalBudgetSpent = remember(state.envelopeBudgets, state.expenses) {
                state.envelopeBudgets.sumOf { budget ->
                    state.expenses
                        .filter { it.status == ExpenseStatus.APPROVED && it.category == budget.category }
                        .sumOf { it.amount }
                }
            }
            val overallBudgetProgress = if (totalBudgetCap > 0) (totalBudgetSpent / totalBudgetCap).toFloat().coerceIn(0f, 1f) else 0f
            val warningCount = remember(state.envelopeBudgets, state.expenses) {
                state.envelopeBudgets.count { budget ->
                    val spent = state.expenses.filter { it.status == ExpenseStatus.APPROVED && it.category == budget.category }.sumOf { it.amount }
                    budget.isNearLimit(spent) || budget.isExceeded(spent)
                }
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Savings,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Monthly Envelope Budgets",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (totalBudgetCap > 0) "$currency${"%.0f".format(totalBudgetSpent)} of $currency${"%.0f".format(totalBudgetCap)} (${"%.0f".format(overallBudgetProgress * 100)}%)" else "No budgets configured",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (warningCount > 0) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFFFF3E0),
                                    modifier = Modifier.padding(end = 6.dp)
                                ) {
                                    Text(
                                        text = "$warningCount Alert",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFE65100),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            IconButton(
                                onClick = { onOpenEnvelopeBudget(null) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "New Envelope", modifier = Modifier.size(20.dp))
                            }
                        }
                    }

                    // Progress bar
                    if (totalBudgetCap > 0) {
                        LinearProgressIndicator(
                            progress = { overallBudgetProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (overallBudgetProgress >= 0.9f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }

                    // Toggle Button to expand or collapse
                    if (state.envelopeBudgets.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { envelopesExpanded = !envelopesExpanded }
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (envelopesExpanded) "Hide Categories ▴" else "View ${state.envelopeBudgets.size} Categories ▾",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Icon(
                                imageVector = if (envelopesExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        androidx.compose.animation.AnimatedVisibility(visible = envelopesExpanded) {
                            Column(
                                modifier = Modifier.padding(top = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                state.envelopeBudgets.forEach { budget ->
                                    val spent = state.expenses
                                        .filter { it.status == ExpenseStatus.APPROVED && it.category == budget.category }
                                        .sumOf { it.amount }
                                    val progress = budget.calculateProgress(spent)
                                    val isNear = budget.isNearLimit(spent)
                                    val isExceeded = budget.isExceeded(spent)

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onOpenEnvelopeBudget(budget) }
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(10.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(8.dp)
                                                            .clip(CircleShape)
                                                            .background(Color(budget.category.defaultColorHex))
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = budget.category.displayName,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                                Text(
                                                    text = "$currency${"%.0f".format(spent)} / $currency${"%.0f".format(budget.monthlyCap)}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                            LinearProgressIndicator(
                                                progress = { progress.toFloat().coerceIn(0f, 1f) },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(4.dp)
                                                    .clip(RoundedCornerShape(2.dp)),
                                                color = if (isExceeded) MaterialTheme.colorScheme.error else if (isNear) Color(0xFFF57C00) else MaterialTheme.colorScheme.primary,
                                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- Who Needs to Pay vs Who is Owed Money ---
        item {
            Text(
                text = "Settlement Status Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // People Who Need to Pay Card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFEBEE)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = Color(0xFFC62828),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Need to Pay",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC62828)
                            )
                        }

                        if (debtors.isEmpty()) {
                            Text(
                                text = "Everyone settled!",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF555555)
                            )
                        } else {
                            debtors.forEach { d ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = d.roommate.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF1E1E1E),
                                        modifier = Modifier.weight(1f, fill = false).padding(end = 4.dp),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "$currency${"%.2f".format(abs(d.netBalance))}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFC62828),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }

                // People Who Are Owed Money Card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE8F5E9)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Owed Money",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                        }

                        if (creditors.isEmpty()) {
                            Text(
                                text = "No balances owed",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF555555)
                            )
                        } else {
                            creditors.forEach { c ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = c.roommate.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF1E1E1E),
                                        modifier = Modifier.weight(1f, fill = false).padding(end = 4.dp),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "$currency${"%.2f".format(c.netBalance)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2E7D32),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- Smart Settle-Up Suggestions & Graph Minimization Engine ---
        if (settlementSuggestions.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Debt Simplification Engine",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Graph-based bilateral minimization",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "$currencyCode ($currency)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Minimization Metrics Badge Bar
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Optimized Transfers", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${metrics.simplifiedTransactionCount} payments", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Transactions Saved", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${metrics.transactionsSavedCount} avoided", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32), style = MaterialTheme.typography.bodyMedium)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Debt Volume", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("$currency${"%.2f".format(metrics.totalVolumeSettled)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }

                        // Settlement Transfers List
                        settlementSuggestions.forEach { transfer ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.surface,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = transfer.fromRoommate.name,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = " pays ",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = transfer.toRoommate.name,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "$currency${"%.2f".format(transfer.amount)}",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    FilledTonalButton(
                                        onClick = {
                                            onQuickSettle(
                                                transfer.fromRoommate.id,
                                                transfer.toRoommate.id,
                                                transfer.amount
                                            )
                                        },
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text("Settle", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }

                        // Smart Settlement Suggestion Action: Settle All at Once
                        Button(
                            onClick = { showBatchConfirmDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Propose Batch Settlement (${settlementSuggestions.size} transfers)")
                        }
                    }
                }
            }
        }

        // --- Roommate Balance Breakdown Header ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Roommate Net Balances",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = "Room DB Synced",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Net status from Room database (who owes whom) • Tap card for full breakdown",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Roommate Cards List
        items(balanceSummaries, key = { it.roommate.id }) { summary ->
            RoommateSummaryCard(
                summary = summary,
                currency = currency,
                onClick = { selectedRoommateForDetail = summary }
            )
        }
    }

    // --- Details Modal Bottom Sheet / Dialog with BackHandler ---
    if (selectedRoommateForDetail != null) {
        val s = selectedRoommateForDetail!!
        BackHandler { selectedRoommateForDetail = null }

        AlertDialog(
            onDismissRequest = { selectedRoommateForDetail = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(s.roommate.colorHex)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = s.roommate.name.take(1).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(s.roommate.name, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                if (s.roommate.isAdmin) "Admin • Flat Coordinator" else "Roommate",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = { selectedRoommateForDetail = null }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HorizontalDivider()
                    DetailRow("Total Paid (Groceries)", "$currency${"%.2f".format(s.totalPaidGroceries)}")
                    DetailRow("Total Owed (Their Share)", "$currency${"%.2f".format(s.totalOwedShare)}")
                    DetailRow("Paid to Others (Settlements)", "$currency${"%.2f".format(s.paidToOthers)}")
                    DetailRow("Received from Others (Settlements)", "$currency${"%.2f".format(s.receivedFromOthers)}")
                    HorizontalDivider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Net Balance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        val color = when {
                            s.netBalance > 0.01 -> Color(0xFF2E7D32)
                            s.netBalance < -0.01 -> Color(0xFFC62828)
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                        Text(
                            text = if (s.netBalance < 0) "-$currency${"%.2f".format(abs(s.netBalance))}" else "$currency${"%.2f".format(s.netBalance)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                    }
                    Surface(
                        color = when {
                            s.netBalance > 0.01 -> Color(0xFFE8F5E9)
                            s.netBalance < -0.01 -> Color(0xFFFFEBEE)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Status: ${s.status}",
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { selectedRoommateForDetail = null }) {
                    Text("Back")
                }
            }
        )
    }

    // --- Smart Batch Settlement Confirmation Dialog ---
    if (showBatchConfirmDialog) {
        BackHandler { showBatchConfirmDialog = false }

        AlertDialog(
            onDismissRequest = { showBatchConfirmDialog = false },
            icon = {
                Icon(
                    Icons.Default.Bolt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text("Execute Smart Batch Settlement?")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "The Debt Simplification Engine calculated that all roommate balances can be settled with just ${settlementSuggestions.size} direct payments instead of ${metrics.originalTransactionCountEstimate} bilateral transfers."
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            settlementSuggestions.forEach { t ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("${t.fromRoommate.name} → ${t.toRoommate.name}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                                    Text("$currency${"%.2f".format(t.amount)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        if (state.isActiveUserAdmin)
                            "As an Admin, these ${settlementSuggestions.size} transactions will be immediately approved and clear all balances."
                        else
                            "These ${settlementSuggestions.size} transactions will be submitted to the Admin for approval.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showBatchConfirmDialog = false
                        onBatchSettleAll(settlementSuggestions)
                    }
                ) {
                    Text(if (state.isActiveUserAdmin) "Approve & Execute All" else "Submit Batch")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showBatchConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun RoommateSummaryCard(
    summary: RoommateBalanceSummary,
    currency: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f).padding(end = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(summary.roommate.colorHex)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = summary.roommate.name.take(1).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = summary.roommate.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (summary.roommate.isAdmin) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    "Admin",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Paid: $currency${"%.2f".format(summary.totalPaidGroceries)} • Share: $currency${"%.2f".format(summary.totalOwedShare)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                val isPositive = summary.netBalance > 0.01
                val isNegative = summary.netBalance < -0.01

                val netColor = when {
                    isPositive -> Color(0xFF2E7D32)
                    isNegative -> Color(0xFFC62828)
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }

                val formattedNet = when {
                    isNegative -> "-$currency${"%.2f".format(abs(summary.netBalance))}"
                    else -> "$currency${"%.2f".format(summary.netBalance)}"
                }

                Text(
                    text = formattedNet,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = netColor,
                    maxLines = 1
                )

                Surface(
                    color = when {
                        isPositive -> Color(0xFFE8F5E9)
                        isNegative -> Color(0xFFFFEBEE)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = summary.status,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = netColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun RoommateBalancesView(
    state: ApartmentUiState,
    activeSummary: RoommateBalanceSummary?,
    settlementSuggestions: List<DebtTransfer>,
    currency: String,
    adminNames: String,
    onQuickSettle: (fromId: String, toId: String, amount: Double) -> Unit,
    onAddExpenseClick: () -> Unit,
    onNavigateToExpenses: () -> Unit
) {
    val activeRoommate = state.activeRoommate
    val net = activeSummary?.netBalance ?: 0.0
    val isDebtor = net < -0.01
    val isCreditor = net > 0.01

    val myOutgoingTransfers = remember(settlementSuggestions, state.activeRoommateId) {
        settlementSuggestions.filter { it.fromRoommate.id == state.activeRoommateId }
    }
    val myIncomingTransfers = remember(settlementSuggestions, state.activeRoommateId) {
        settlementSuggestions.filter { it.toRoommate.id == state.activeRoommateId }
    }
    val expensesDoneByOthers = remember(state.expenses, state.activeRoommateId) {
        state.expenses.filter { it.paidByRoommateId != state.activeRoommateId }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // --- Roommate Profile Banner ---
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(activeRoommate?.colorHex ?: 0xFF4CAF50)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = activeRoommate?.name?.take(1) ?: "R",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "${activeRoommate?.name ?: "Roommate"} (Your Balance)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Roommate Scope • Admin: $adminNames",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Text(
                            text = "Roommate View",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // --- Hero Card: My Remaining Amount ---
        item {
            val heroContainerColor = when {
                isDebtor -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                isCreditor -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }

            val heroContentColor = when {
                isDebtor -> MaterialTheme.colorScheme.onErrorContainer
                isCreditor -> MaterialTheme.colorScheme.onPrimaryContainer
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }

            val badgeColor = when {
                isDebtor -> Color(0xFFC62828)
                isCreditor -> Color(0xFF2E7D32)
                else -> MaterialTheme.colorScheme.primary
            }

            val badgeText = when {
                isDebtor -> "Payment Needed"
                isCreditor -> "Owed to You"
                else -> "All Settled Up"
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = heroContainerColor),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = when {
                                    isDebtor -> "Remaining Amount to Pay"
                                    isCreditor -> "Remaining Amount to Receive"
                                    else -> "Remaining Amount"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = heroContentColor.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "$currency${"%,.2f".format(abs(net))}",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isDebtor) Color(0xFFB71C1C) else heroContentColor
                            )
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = when {
                                        isDebtor -> Icons.Default.ArrowUpward
                                        isCreditor -> Icons.Default.ArrowDownward
                                        else -> Icons.Default.CheckCircle
                                    },
                                    contentDescription = null,
                                    tint = badgeColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = badgeText,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = badgeColor
                                )
                            }
                        }
                    }

                    Text(
                        text = when {
                            isDebtor -> "You need to pay $currency${"%.2f".format(abs(net))} to clear your apartment expenses balance."
                            isCreditor -> "Your flatmates owe you $currency${"%.2f".format(net)} for shared groceries and bills you paid."
                            else -> "You are completely settled up! You have no outstanding payments or dues."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = heroContentColor.copy(alpha = 0.85f)
                    )

                    // Personal Breakdown Details Card
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DetailRow(
                                label = "Your Grocery & Expense Spending",
                                value = "$currency${"%.2f".format(activeSummary?.totalPaidGroceries ?: 0.0)}"
                            )
                            DetailRow(
                                label = "Your Fair Share of Bills",
                                value = "$currency${"%.2f".format(activeSummary?.totalOwedShare ?: 0.0)}"
                            )
                            DetailRow(
                                label = "Settlement Payments Sent",
                                value = "$currency${"%.2f".format(activeSummary?.paidToOthers ?: 0.0)}"
                            )
                            DetailRow(
                                label = "Settlement Payments Received",
                                value = "$currency${"%.2f".format(activeSummary?.receivedFromOthers ?: 0.0)}"
                            )
                        }
                    }

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onAddExpenseClick,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Expense", fontWeight = FontWeight.Bold)
                        }

                        if (myOutgoingTransfers.isNotEmpty()) {
                            val first = myOutgoingTransfers.first()
                            FilledTonalButton(
                                onClick = {
                                    onQuickSettle(first.fromRoommate.id, first.toRoommate.id, first.amount)
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f).height(44.dp)
                            ) {
                                Icon(Icons.Default.SyncAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pay to Others", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // --- Pay to Others Section (Direct Settle-Up Transfers) ---
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Pay to Others",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Transfers required to clear your remaining balance",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "${myOutgoingTransfers.size} to pay",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    HorizontalDivider()

                    if (myOutgoingTransfers.isEmpty() && myIncomingTransfers.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "No pending payments for you!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Your apartment balance is fully settled.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        // Outgoing transfers (Pay to Others)
                        if (myOutgoingTransfers.isNotEmpty()) {
                            Text(
                                text = "You Need to Pay:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )

                            myOutgoingTransfers.forEach { transfer ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(Color(transfer.toRoommate.colorHex)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = transfer.toRoommate.name.take(1),
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "Pay to ${transfer.toRoommate.name}",
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Text(
                                                text = "Remaining flat debt",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "$currency${"%.2f".format(transfer.amount)}",
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(
                                            onClick = {
                                                onQuickSettle(
                                                    transfer.fromRoommate.id,
                                                    transfer.toRoommate.id,
                                                    transfer.amount
                                                )
                                            },
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.height(34.dp)
                                        ) {
                                            Text("Pay Now", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                            }
                        }

                        // Incoming transfers (To receive from others)
                        if (myIncomingTransfers.isNotEmpty()) {
                            Text(
                                text = "Others Need to Pay You:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )

                            myIncomingTransfers.forEach { transfer ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            Color(0xFFE8F5E9),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(Color(transfer.fromRoommate.colorHex)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = transfer.fromRoommate.name.take(1),
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "${transfer.fromRoommate.name} pays you",
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Text(
                                                text = "Awaiting settlement",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Text(
                                        text = "$currency${"%.2f".format(transfer.amount)}",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2E7D32),
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- Shared Expenses Done by Others Section ---
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Expenses Done by Others",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Groceries & household items paid by flatmates",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = "${expensesDoneByOthers.size} logged",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    HorizontalDivider()

                    if (expensesDoneByOthers.isEmpty()) {
                        Text(
                            text = "No shared expenses logged by other flatmates yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        expensesDoneByOthers.take(4).forEach { exp ->
                            val payer = state.roommates.find { it.id == exp.paidByRoommateId }
                            val isIncludedInSplit = exp.sharedByRoommateIds.contains(state.activeRoommateId)

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                        Text(
                                            text = exp.item,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "Paid by ${payer?.name ?: "Flatmate"} • ${exp.date}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "$currency${"%.2f".format(exp.amount)}",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        if (isIncludedInSplit) {
                                            Text(
                                                text = "Your Share: $currency${"%.2f".format(exp.shareEach)}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        } else {
                                            Text(
                                                text = "Not in split",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        OutlinedButton(
                            onClick = onNavigateToExpenses,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Browse All Expenses in Expenses Tab")
                        }
                    }
                }
            }
        }

        // --- Flat Admin Authentication Notice ---
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Admin Authentication & Ledger Oversight",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "All pending transactions and apartment administration are authenticated and approved by Flat Admin ($adminNames).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
