package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.data.BillCategory
import com.example.data.Roommate
import com.example.ui.ApartmentViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ApartmentViewModel,
    onNavigateToBills: () -> Unit,
    onNavigateToPeople: () -> Unit,
    onAddBillClick: () -> Unit,
    onSettleUpClick: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val roommates by viewModel.roommates.collectAsState()
    val bills by viewModel.bills.collectAsState()
    val debts by viewModel.debts.collectAsState()
    val totalSpent by viewModel.totalSpent.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
    val currentCurrency by viewModel.currency.collectAsState()

    val apartmentName by viewModel.activeApartmentName.collectAsState()
    val inviteCode by viewModel.activeApartmentInviteCode.collectAsState()

    // Find current user's balance
    val currentUser = roommates.find { it.id == currentUserId }
    val userBalance = currentUser?.balance ?: 0.0

    // Compute category totals
    val groceryTotal = bills.filter { it.category == BillCategory.GROCERIES }.sumOf { it.amount }
    val utilityTotal = bills.filter { it.category == BillCategory.UTILITIES }.sumOf { it.amount }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
    ) {
        // MD3 Top Header / Custom App Bar within the scroll or persistent
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable { onNavigateToProfile() }
                            .testTag("home_avatar_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "AF",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Text(
                        text = apartmentName ?: stringResource(R.string.login_welcome),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        )
                    )
                }

                val unreadNotificationsCount by viewModel.unreadNotificationsCount.collectAsState()

                IconButton(
                    onClick = onNavigateToNotifications,
                    modifier = Modifier.testTag("notification_button")
                ) {
                    BadgedBox(
                        badge = {
                            if (unreadNotificationsCount > 0) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError
                                ) {
                                    Text(
                                        text = if (unreadNotificationsCount > 99) "99+" else unreadNotificationsCount.toString(),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = stringResource(R.string.notifications_title),
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        // Balance Dashboard Card (MD3 Elevated Card, radius 28dp)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("balance_card"),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.home_total_pool),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                            )
                            Text(
                                text = viewModel.formatCurrency(totalSpent, currentCurrency),
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        // Due/Owed status badge
                        val badgeColor = if (userBalance < 0) Color(0xFFBA1A1A) else Color(0xFF386A20)
                        val badgeBg = if (userBalance < 0) Color(0xFFFFDAD6) else Color(0xFFE8F5E9)
                        val badgeText = if (userBalance < 0) {
                            stringResource(R.string.home_due, viewModel.formatCurrency(-userBalance, currentCurrency))
                        } else if (userBalance > 0) {
                            stringResource(R.string.home_owed, viewModel.formatCurrency(userBalance, currentCurrency))
                        } else {
                            stringResource(R.string.home_all_settled)
                        }

                        Surface(
                            shape = CircleShape,
                            color = badgeBg,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = badgeText,
                                color = badgeColor,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onSettleUpClick,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("settle_up_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.settle_up_dialog_title),
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                            )
                        }

                        Button(
                            onClick = onAddBillClick,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("add_bill_home_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Icon",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.home_add_bill),
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        // Invite Roommates Card
        inviteCode?.let { code ->
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("invite_share_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.home_invite_roommates_title),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = stringResource(R.string.home_invite_code_label, code),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Copy Button
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Apartment Invite Code", code)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, context.getString(R.string.home_copy_toast), Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.testTag("copy_invite_code_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Code",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            // Share Button
                            IconButton(
                                onClick = {
                                    val shareUrl = "https://apartment-flow.web.app/join/$code"
                                    val shareIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        type = "text/plain"
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            context.getString(R.string.home_share_text, shareUrl, code)
                                        )
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.home_share_chooser)))
                                },
                                modifier = Modifier.testTag("share_invite_link_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share Link",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Stats Grid (Groceries and Utilities cards)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Groceries card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(112.dp)
                        .testTag("grocery_card")
                        .clickable { onNavigateToBills() },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE6DEFF)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.home_groceries_label),
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 1.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFF1D1633).copy(alpha = 0.7f)
                        )
                        Text(
                            text = viewModel.formatCurrency(groceryTotal, currentCurrency),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1D1633)
                        )
                    }
                }

                // Utilities card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(112.dp)
                        .testTag("utility_card")
                        .clickable { onNavigateToBills() },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFD2E5D5)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.home_utilities_label),
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 1.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFF00210E).copy(alpha = 0.7f)
                        )
                        Text(
                            text = viewModel.formatCurrency(utilityTotal, currentCurrency),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00210E)
                        )
                    }
                }
            }
        }

        // Roommates Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.home_roommates_header),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                TextButton(
                    onClick = onNavigateToPeople,
                    modifier = Modifier.testTag("manage_roommates_button")
                ) {
                    Text(
                        text = stringResource(R.string.home_manage_button),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Roommates Summary List (displays first 3 roommates)
        val visibleRoommates = roommates.take(3)
        items(visibleRoommates, key = { it.id }) { roommate ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
                    .testTag("roommate_item_${roommate.id}"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(roommate.avatarBgColor)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = roommate.initials,
                            color = Color(roommate.avatarTextColor),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    // Name and Status
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = roommate.name,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                        
                        val isUserOwed = roommate.balance > 0
                        val statusColor = if (isUserOwed) Color(0xFF386A20) else if (roommate.balance < 0) Color(0xFFBA1A1A) else Color.Gray
                        
                        val statusTextStr = when {
                            roommate.balance > 0.01 -> stringResource(R.string.home_roommate_owed, viewModel.formatCurrency(roommate.balance, currentCurrency))
                            roommate.balance < -0.01 -> stringResource(R.string.home_roommate_owes, viewModel.formatCurrency(-roommate.balance, currentCurrency))
                            else -> stringResource(R.string.home_all_settled)
                        }
                        Text(
                            text = statusTextStr,
                            style = MaterialTheme.typography.bodyMedium,
                            color = statusColor
                        )
                    }

                    // Total Paid
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = viewModel.formatCurrency(roommate.totalPaid, currentCurrency),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.home_roommate_paid_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }

        // Mini Trend Chart / Graph (Analytics Mockup, radius 24dp)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("trend_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Text(
                        text = stringResource(R.string.home_trend_title),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Canvas rendering a stylized bar graph with heights matching spec: [40%, 60%, 30%, 85%, 55%, 100%]
                    // Active column (last one) gets primaryContainer highlight color!
                    val primaryColor = MaterialTheme.colorScheme.primary
                    val activeColor = MaterialTheme.colorScheme.primaryContainer
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(84.dp)
                            .padding(horizontal = 4.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val barCount = 6
                            val paddingFraction = 0.25f // percentage of gap size
                            val heightsFraction = listOf(0.40f, 0.60f, 0.30f, 0.85f, 0.55f, 1.00f)
                            
                            val width = size.width
                            val height = size.height
                            
                            val gap = width / (barCount + (barCount - 1) * paddingFraction)
                            val pad = gap * paddingFraction
                            
                            for (i in 0 until barCount) {
                                val barHeight = heightsFraction[i] * height
                                val x = i * (gap + pad)
                                val y = height - barHeight
                                
                                val isLast = i == barCount - 1
                                val barColor = if (isLast) activeColor else primaryColor
                                
                                drawRoundRect(
                                    color = barColor,
                                    topLeft = Offset(x, y),
                                    size = Size(gap, barHeight),
                                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                )
                            }
                        }
                    }
                    
                    // Months timeline
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val labels = listOf("Feb", "Mar", "Apr", "May", "Jun", "Jul (Active)")
                        labels.forEachIndexed { index, label ->
                            val isLast = index == labels.size - 1
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isLast) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                fontWeight = if (isLast) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}
