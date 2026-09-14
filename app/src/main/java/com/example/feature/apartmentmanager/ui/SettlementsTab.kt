package com.example.feature.apartmentmanager.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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

@Composable
fun SettlementsTab(
    state: ApartmentUiState,
    onAddSettlement: () -> Unit,
    onApproveSettlement: (String) -> Unit,
    onRejectSettlement: (String, String) -> Unit,
    onDeleteSettlement: (String) -> Unit
) {
    val currency = state.profile.currencySymbol
    var settlementFilterStatus by remember { mutableStateOf<SettlementStatus?>(null) }
    var settlementToDelete by remember { mutableStateOf<ApartmentSettlement?>(null) }
    var settlementToReject by remember { mutableStateOf<ApartmentSettlement?>(null) }
    var rejectionReasonText by remember { mutableStateOf("") }

    val pendingSettlements = remember(state.settlements) {
        state.settlements.filter { it.status == SettlementStatus.PENDING }
    }

    val approvedSettlements = remember(state.settlements) {
        state.settlements.filter { it.status == SettlementStatus.APPROVED }
    }

    val displayedSettlements = remember(state.settlements, settlementFilterStatus) {
        when (settlementFilterStatus) {
            SettlementStatus.PENDING -> state.settlements.filter { it.status == SettlementStatus.PENDING }
            SettlementStatus.APPROVED -> state.settlements.filter { it.status == SettlementStatus.APPROVED }
            SettlementStatus.REJECTED -> state.settlements.filter { it.status == SettlementStatus.REJECTED }
            else -> state.settlements
        }
    }

    val totalSettled = remember(approvedSettlements) {
        approvedSettlements.sumOf { it.amount }
    }

    val isAdmin = state.isActiveUserAdmin
    val adminNames = remember(state.roommates) {
        state.roommates.filter { it.isAdmin }.map { it.name }.joinToString(", ").ifEmpty { "Admin" }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header summary
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(
                                text = "Settlement Ledger",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "${approvedSettlements.size} Approved Transfers",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Total Settled",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "$currency${"%,.2f".format(totalSettled)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Admin status indicator
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isAdmin) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (isAdmin) Icons.Default.AdminPanelSettings else Icons.Default.Info,
                                contentDescription = null,
                                tint = if (isAdmin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isAdmin) "You are logged in as Admin ($adminNames). You have approval authority."
                                else "Viewing as (${state.activeRoommate?.name}). Only Admin ($adminNames) can approve settlements.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Clean Non-overlapping Action Button
        item {
            Button(
                onClick = onAddSettlement,
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
                Text(
                    text = if (isAdmin) "Record or Approve Settlement" else "Request Settlement Payment",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Pending Approvals Banner if any
        if (pendingSettlements.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(14.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.HourglassTop,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${pendingSettlements.size} Settlement${if (pendingSettlements.size > 1) "s" else ""} Awaiting Approval",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = if (isAdmin) "Review and tap Approve or Decline below." else "Waiting for Admin ($adminNames) to verify.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }

        // Filter chips (All, Pending, Approved, Declined)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = settlementFilterStatus == null,
                    onClick = { settlementFilterStatus = null },
                    label = { Text("All (${state.settlements.size})") }
                )
                FilterChip(
                    selected = settlementFilterStatus == SettlementStatus.PENDING,
                    onClick = {
                        settlementFilterStatus = if (settlementFilterStatus == SettlementStatus.PENDING) null else SettlementStatus.PENDING
                    },
                    label = { Text("Pending (${pendingSettlements.size})") },
                    leadingIcon = if (pendingSettlements.isNotEmpty()) {
                        {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.error)
                            )
                        }
                    } else null
                )
                FilterChip(
                    selected = settlementFilterStatus == SettlementStatus.APPROVED,
                    onClick = {
                        settlementFilterStatus = if (settlementFilterStatus == SettlementStatus.APPROVED) null else SettlementStatus.APPROVED
                    },
                    label = { Text("Approved (${approvedSettlements.size})") }
                )
                FilterChip(
                    selected = settlementFilterStatus == SettlementStatus.REJECTED,
                    onClick = {
                        settlementFilterStatus = if (settlementFilterStatus == SettlementStatus.REJECTED) null else SettlementStatus.REJECTED
                    },
                    label = {
                        val count = state.settlements.count { it.status == SettlementStatus.REJECTED }
                        Text("Declined ($count)")
                    }
                )
            }
        }

        // Settlement list
        if (displayedSettlements.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Payments,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No settlements in this category",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(displayedSettlements, key = { it.id }) { settlement ->
                val fromRoommate = state.roommates.find { it.id == settlement.fromRoommateId }
                val toRoommate = state.roommates.find { it.id == settlement.toRoommateId }
                val isPending = settlement.status == SettlementStatus.PENDING
                val isRejected = settlement.status == SettlementStatus.REJECTED

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            isPending -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
                            isRejected -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            else -> MaterialTheme.colorScheme.surface
                        }
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isPending) 3.dp else 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (isPending) Modifier.border(
                                1.dp,
                                MaterialTheme.colorScheme.error.copy(alpha = 0.3f),
                                RoundedCornerShape(14.dp)
                            ) else Modifier
                        )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Top Row: Avatars, transfer info, amount, and delete
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f).padding(end = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // From avatar
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color(fromRoommate?.colorHex ?: 0xFF006A6AL)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = fromRoommate?.name?.take(1) ?: "P",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                Icon(
                                    Icons.Default.ArrowForward,
                                    contentDescription = "paid to",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )

                                Spacer(modifier = Modifier.width(6.dp))

                                // To avatar
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color(toRoommate?.colorHex ?: 0xFF006A6AL)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = toRoommate?.name?.take(1) ?: "R",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = fromRoommate?.name ?: "Unknown",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = " → ",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = toRoommate?.name ?: "Unknown",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Text(
                                        text = if (settlement.note.isNotBlank()) "${settlement.date} • ${settlement.note}" else settlement.date,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "$currency${"%.2f".format(settlement.amount)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPending) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                )
                                IconButton(
                                    onClick = { settlementToDelete = settlement },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }

                        // Status Pill and Admin Actions
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            when (settlement.status) {
                                SettlementStatus.PENDING -> {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.errorContainer,
                                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.HourglassEmpty,
                                                contentDescription = null,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Pending Admin Approval",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                                SettlementStatus.APPROVED -> {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFF2E7D32).copy(alpha = 0.12f),
                                        contentColor = Color(0xFF1B5E20)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = Color(0xFF2E7D32),
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Approved & Verified",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                                SettlementStatus.REJECTED -> {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                                        contentColor = MaterialTheme.colorScheme.error
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.Cancel,
                                                contentDescription = null,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (!settlement.rejectionReason.isNullOrBlank()) "Declined: ${settlement.rejectionReason}" else "Declined by Admin",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }

                            if (settlement.syncState == DataSyncState.PENDING) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.CloudQueue,
                                            contentDescription = null,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Offline Queued",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }

                            // If pending: show Approve & Reject buttons IF user is Admin!
                            if (isPending) {
                                if (isAdmin) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedButton(
                                            onClick = {
                                                settlementToReject = settlement
                                                rejectionReasonText = ""
                                            },
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = MaterialTheme.colorScheme.error
                                            ),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.height(36.dp)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Decline", style = MaterialTheme.typography.labelSmall)
                                        }

                                        Button(
                                            onClick = { onApproveSettlement(settlement.id) },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFF2E7D32)
                                            ),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                            modifier = Modifier.height(36.dp)
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Approve", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                } else {
                                    Text(
                                        text = "Waiting for Admin ($adminNames)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Rejection Reason Dialog
    if (settlementToReject != null) {
        val s = settlementToReject!!
        val fromName = state.roommates.find { it.id == s.fromRoommateId }?.name ?: "Roommate"
        val toName = state.roommates.find { it.id == s.toRoommateId }?.name ?: "Roommate"

        AlertDialog(
            onDismissRequest = { settlementToReject = null },
            title = { Text("Decline Settlement?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Are you sure you want to decline this payment of $currency${"%.2f".format(s.amount)} from $fromName to $toName?")
                    OutlinedTextField(
                        value = rejectionReasonText,
                        onValueChange = { rejectionReasonText = it },
                        label = { Text("Reason (Optional)") },
                        placeholder = { Text("e.g. Not received in bank, duplicate entry") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRejectSettlement(s.id, rejectionReasonText.trim())
                        settlementToReject = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Decline Settlement")
                }
            },
            dismissButton = {
                TextButton(onClick = { settlementToReject = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (settlementToDelete != null) {
        val s = settlementToDelete!!
        val fromName = state.roommates.find { it.id == s.fromRoommateId }?.name ?: "Roommate"
        val toName = state.roommates.find { it.id == s.toRoommateId }?.name ?: "Roommate"

        AlertDialog(
            onDismissRequest = { settlementToDelete = null },
            title = { Text("Delete Settlement?") },
            text = { Text("Are you sure you want to delete payment of $currency${"%.2f".format(s.amount)} from $fromName to $toName?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteSettlement(s.id)
                        settlementToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { settlementToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
