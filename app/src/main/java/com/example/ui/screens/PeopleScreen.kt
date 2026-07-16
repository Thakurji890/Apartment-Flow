package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Roommate
import com.example.data.Debt
import com.example.ui.ApartmentViewModel
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.style.TextOverflow
import com.example.ui.theme.extendedColors
import com.example.R
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeopleScreen(
    viewModel: ApartmentViewModel,
    onTriggerSettleUpDirectly: (String, String, Double) -> Unit,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val roommates by viewModel.roommates.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val debts by viewModel.debts.collectAsState()
    val currentCurrency by viewModel.currency.collectAsState()

    var showAddRoommateDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
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
                            modifier = Modifier.testTag("people_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = stringResource(R.string.cd_back)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = stringResource(R.string.people_screen_title),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = pluralStringResource(R.plurals.people_count, roommates.size, roommates.size),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                }

                Button(
                    onClick = { showAddRoommateDialog = true },
                    modifier = Modifier.testTag("add_roommate_header_button"),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.people_add_button),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.action_add),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("people_list"),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // Roster Cards Section
                item {
                    Text(
                        text = stringResource(R.string.people_roster_header),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                items(roommates, key = { it.id }) { roommate ->
                    val extendedColors = MaterialTheme.extendedColors
                    val balanceColor = if (roommate.balance > 0.0) {
                        extendedColors.positive
                    } else if (roommate.balance < 0.0) {
                        extendedColors.negative
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    }
                    val balanceBg = if (roommate.balance > 0.0) {
                        extendedColors.positiveContainer
                    } else if (roommate.balance < 0.0) {
                        extendedColors.negativeContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("people_item_${roommate.id}"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Avatar circle
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(roommate.avatarBgColor)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = roommate.initials,
                                    color = Color(roommate.avatarTextColor),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            // Details
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = roommate.name,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = stringResource(R.string.people_total_paid_template, viewModel.formatCurrency(roommate.totalPaid, currentCurrency)),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Column(
                                horizontalAlignment = Alignment.End
                            ) {
                                // Net Balance Badge
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = balanceBg
                                ) {
                                    val badgeText = when {
                                        roommate.balance > 0.01 -> stringResource(R.string.home_roommate_owed, viewModel.formatCurrency(roommate.balance, currentCurrency))
                                        roommate.balance < -0.01 -> stringResource(R.string.home_roommate_owes, viewModel.formatCurrency(-roommate.balance, currentCurrency))
                                        else -> stringResource(R.string.people_settled)
                                    }
                                    Text(
                                        text = badgeText,
                                        color = balanceColor,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                
                                if (roommate.id != viewModel.currentUserId.value) {
                                    androidx.compose.material3.IconButton(
                                        onClick = {
                                            if (Math.abs(roommate.balance) > 0.01) {
                                                android.widget.Toast.makeText(context, context.getString(R.string.people_remove_balance_error, roommate.name), android.widget.Toast.LENGTH_LONG).show()
                                            } else {
                                                viewModel.removeRoommate(roommate.id)
                                            }
                                        },
                                        modifier = Modifier.size(32.dp).padding(top = 4.dp).testTag("remove_roommate_${roommate.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = stringResource(R.string.people_remove_roommate),
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Debts / Settlements Matrix Section
                item {
                    Text(
                        text = stringResource(R.string.people_matrix_header),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }

                if (debts.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Payments,
                                    contentDescription = stringResource(R.string.cd_settled_icon),
                                    tint = MaterialTheme.extendedColors.positive,
                                    modifier = Modifier.size(36.dp)
                                )
                                Text(
                                    text = stringResource(R.string.people_no_debts_title),
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = stringResource(R.string.people_no_debts_desc),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                } else {
                    items(debts) { debt ->
                        val debtorName = roommates.find { it.id == debt.fromId }?.name ?: "Unknown"
                        val creditorName = roommates.find { it.id == debt.toId }?.name ?: "Unknown"

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("debt_item_${debt.fromId}_to_${debt.toId}"),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = debtorName,
                                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f, fill = false)
                                            )
                                            Icon(
                                                imageVector = Icons.Default.ArrowForward,
                                                contentDescription = stringResource(R.string.cd_owes_icon),
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = creditorName,
                                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f, fill = false)
                                            )
                                        }
                                        Text(
                                            text = stringResource(R.string.people_bilateral_debt_desc),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = viewModel.formatCurrency(debt.amount, currentCurrency),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.error
                                    )

                                    Button(
                                        onClick = {
                                            onTriggerSettleUpDirectly(debt.fromId, debt.toId, debt.amount)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        ),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier
                                            .height(36.dp)
                                            .testTag("settle_button_${debt.fromId}_to_${debt.toId}"),
                                        shape = RoundedCornerShape(18.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.people_settle_direct),
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Roommate Dialog
        if (showAddRoommateDialog) {
            var name by remember { mutableStateOf("") }
            var nameError by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { showAddRoommateDialog = false },
                title = {
                    Text(
                        text = stringResource(R.string.people_add_title),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                viewModel.addRoommate(name)
                                showAddRoommateDialog = false
                            } else {
                                nameError = true
                            }
                        },
                        modifier = Modifier.testTag("roommate_save_button")
                    ) {
                        Text(stringResource(R.string.people_add_button))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showAddRoommateDialog = false },
                        modifier = Modifier.testTag("roommate_cancel_button")
                    ) {
                        Text(stringResource(R.string.action_cancel))
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.people_add_roommate_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        OutlinedTextField(
                            value = name,
                            onValueChange = {
                                name = it
                                nameError = false
                            },
                            label = { Text(stringResource(R.string.people_name_label)) },
                            placeholder = { Text(stringResource(R.string.people_name_hint)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("roommate_name_input"),
                            isError = nameError,
                            supportingText = {
                                if (nameError) {
                                    Text(stringResource(R.string.people_name_empty_error))
                                }
                            },
                            singleLine = true
                        )
                    }
                }
            )
        }
    }
}
