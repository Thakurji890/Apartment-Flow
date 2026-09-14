package com.example.feature.apartmentmanager.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineInfoDialog(
    isOnline: Boolean,
    isSyncing: Boolean,
    pendingSyncCount: Int,
    onSyncNow: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = if (isOnline) Icons.Default.CloudDone else Icons.Default.CloudOff,
                    contentDescription = null,
                    tint = if (isOnline) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                )
                Text(
                    text = if (isOnline) "Cloud Synced & Live" else "Offline-First Architecture",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Live Status Banner
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isOnline) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(if (isOnline) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error)
                        )
                        Column {
                            Text(
                                text = if (isOnline) "Connected to Cloud" else "Travel Dead Zone / Offline",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isOnline) Color(0xFF1B5E20) else MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = if (pendingSyncCount > 0) "$pendingSyncCount record(s) queued for sync" else "All household records up to date",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isOnline) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                Text(
                    text = "ApartmentFlow uses an offline-first local database. You can split bills and manage apartment finances anywhere without internet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Feature Highlights
                OfflineFeatureItem(
                    icon = Icons.Default.FlightTakeoff,
                    title = "Travel Dead Zones & Remote Trips",
                    description = "Record vacation rentals, gas stops, and grocery splits on the road even with zero cellular coverage."
                )

                OfflineFeatureItem(
                    icon = Icons.Default.AccountBalanceWallet,
                    title = "Instant Local Ledgers",
                    description = "Balances, who owes whom, and debt simplification calculations update immediately on device."
                )

                OfflineFeatureItem(
                    icon = Icons.Default.Sync,
                    title = "Automatic Cloud Synchronization",
                    description = "Changes are safely stored in Room/local storage and auto-sync to cloud servers as soon as internet is restored."
                )

                if (isOnline && pendingSyncCount > 0) {
                    Button(
                        onClick = onSyncNow,
                        enabled = !isSyncing,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Syncing...")
                        } else {
                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sync $pendingSyncCount Queued Item(s) Now")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Got It")
            }
        }
    )
}

@Composable
private fun OfflineFeatureItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
