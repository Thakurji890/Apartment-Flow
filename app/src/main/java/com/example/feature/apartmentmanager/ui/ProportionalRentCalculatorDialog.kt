package com.example.feature.apartmentmanager.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.feature.apartmentmanager.domain.ProportionalRentCalculatorEngine
import com.example.feature.apartmentmanager.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProportionalRentCalculatorDialog(
    roommates: List<ApartmentRoommate>,
    initialRent: Double,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onRecordExpense: (amount: Double, description: String, shares: List<RoomRentShare>) -> Unit
) {
    BackHandler { onDismiss() }

    var selectedStrategy by remember { mutableStateOf(RentSplitStrategy.SQUARE_FOOTAGE) }
    var totalRentText by remember {
        mutableStateOf(if (initialRent > 0) "%.0f".format(initialRent) else "45000")
    }
    var commonAreaPercentText by remember { mutableStateOf("25") }

    // State per roommate for Square Footage
    val sqFtInputs = remember(roommates) {
        mutableStateMapOf<String, String>().apply {
            roommates.forEachIndexed { index, rm ->
                this[rm.id] = (180 - (index * 15)).coerceAtLeast(100).toString()
            }
        }
    }

    // State per roommate for Amenities
    val privateBathStates = remember(roommates) {
        mutableStateMapOf<String, Boolean>().apply {
            roommates.forEachIndexed { index, rm ->
                this[rm.id] = (index == 0) // First roommate has en-suite bath by default
            }
        }
    }
    val balconyStates = remember(roommates) {
        mutableStateMapOf<String, Boolean>().apply {
            roommates.forEachIndexed { index, rm ->
                this[rm.id] = (index == 0 || index == 1)
            }
        }
    }
    val parkingStates = remember(roommates) {
        mutableStateMapOf<String, Boolean>().apply {
            roommates.forEachIndexed { index, rm ->
                this[rm.id] = (index == 0)
            }
        }
    }
    val closetStates = remember(roommates) {
        mutableStateMapOf<String, Boolean>().apply {
            roommates.forEachIndexed { index, rm ->
                this[rm.id] = (index == 0)
            }
        }
    }

    // State per roommate for Income Ratios
    val incomeInputs = remember(roommates) {
        mutableStateMapOf<String, String>().apply {
            roommates.forEachIndexed { index, rm ->
                this[rm.id] = (60000 + (index * 15000)).toString()
            }
        }
    }

    // Calculate Result dynamically
    val parsedTotalRent = totalRentText.toDoubleOrNull() ?: 0.0
    val parsedCommonAreaPercent = commonAreaPercentText.toDoubleOrNull() ?: 25.0

    val calculatedResult = remember(
        selectedStrategy,
        parsedTotalRent,
        parsedCommonAreaPercent,
        sqFtInputs.toMap(),
        privateBathStates.toMap(),
        balconyStates.toMap(),
        parkingStates.toMap(),
        closetStates.toMap(),
        incomeInputs.toMap(),
        roommates
    ) {
        when (selectedStrategy) {
            RentSplitStrategy.SQUARE_FOOTAGE -> {
                val inputs = roommates.map { rm ->
                    ProportionalRentCalculatorEngine.SquareFootageInput(
                        roommateId = rm.id,
                        roommateName = rm.name,
                        bedroomName = "${rm.name}'s Bedroom",
                        bedroomSqFt = sqFtInputs[rm.id]?.toDoubleOrNull() ?: 120.0
                    )
                }
                ProportionalRentCalculatorEngine.calculateBySquareFootage(
                    totalRent = parsedTotalRent,
                    inputs = inputs,
                    commonAreaPercent = parsedCommonAreaPercent
                )
            }
            RentSplitStrategy.AMENITY_BASED -> {
                val inputs = roommates.map { rm ->
                    ProportionalRentCalculatorEngine.AmenityInput(
                        roommateId = rm.id,
                        roommateName = rm.name,
                        bedroomName = "${rm.name}'s Bedroom",
                        hasPrivateEnsuiteBath = privateBathStates[rm.id] ?: false,
                        hasBalconyPatio = balconyStates[rm.id] ?: false,
                        hasDedicatedParking = parkingStates[rm.id] ?: false,
                        hasWalkInCloset = closetStates[rm.id] ?: false
                    )
                }
                ProportionalRentCalculatorEngine.calculateByAmenities(
                    totalRent = parsedTotalRent,
                    inputs = inputs
                )
            }
            RentSplitStrategy.INCOME_RATIO -> {
                val inputs = roommates.map { rm ->
                    ProportionalRentCalculatorEngine.IncomeRatioInput(
                        roommateId = rm.id,
                        roommateName = rm.name,
                        monthlyIncome = incomeInputs[rm.id]?.toDoubleOrNull() ?: 50000.0
                    )
                }
                ProportionalRentCalculatorEngine.calculateByIncomeRatio(
                    totalRent = parsedTotalRent,
                    inputs = inputs
                )
            }
            RentSplitStrategy.EQUAL -> {
                ProportionalRentCalculatorEngine.calculateEqual(
                    totalRent = parsedTotalRent,
                    roommates = roommates
                )
            }
            RentSplitStrategy.HYBRID_CUSTOM -> {
                val inputs = roommates.map { rm ->
                    ProportionalRentCalculatorEngine.CustomWeightInput(
                        roommateId = rm.id,
                        roommateName = rm.name,
                        roomName = "${rm.name}'s Room",
                        weight = 1.0
                    )
                }
                ProportionalRentCalculatorEngine.calculateByCustomWeights(
                    totalRent = parsedTotalRent,
                    inputs = inputs
                )
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Calculate,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Proportional Rent Calculator",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Split unequally based on room size & amenities",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                // Scrollable Body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Total Rent & Common Area Inputs
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Monthly Total Rent",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedTextField(
                                    value = totalRentText,
                                    onValueChange = { totalRentText = it },
                                    label = { Text("Total Rent ($currencySymbol)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    leadingIcon = { Text(currencySymbol, fontWeight = FontWeight.Bold) },
                                    modifier = Modifier.weight(1.2f),
                                    singleLine = true
                                )
                                if (selectedStrategy == RentSplitStrategy.SQUARE_FOOTAGE) {
                                    OutlinedTextField(
                                        value = commonAreaPercentText,
                                        onValueChange = { commonAreaPercentText = it },
                                        label = { Text("Common Area %") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                }
                            }
                        }
                    }

                    // Calculation Method Selector
                    Text(
                        text = "Split Fairness Method",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            RentSplitStrategy.SQUARE_FOOTAGE to "📏 Sq.Ft",
                            RentSplitStrategy.AMENITY_BASED to "👑 Amenities",
                            RentSplitStrategy.INCOME_RATIO to "💼 Income",
                            RentSplitStrategy.EQUAL to "⚖️ Equal"
                        ).forEach { (strategy, label) ->
                            FilterChip(
                                selected = selectedStrategy == strategy,
                                onClick = { selectedStrategy = strategy },
                                label = {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (selectedStrategy == strategy) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Explanation banner for the selected method
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = selectedStrategy.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    // Roommate Specific Parameters
                    if (selectedStrategy != RentSplitStrategy.EQUAL) {
                        Text(
                            text = "Roommate Details",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        roommates.forEach { rm ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                border = CardDefaults.outlinedCardBorder(),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(Color(rm.colorHex)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = rm.name.take(1),
                                                color = Color.White,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Text(
                                            text = rm.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (rm.isAdmin) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = "Admin",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    // Method Specific inputs
                                    when (selectedStrategy) {
                                        RentSplitStrategy.SQUARE_FOOTAGE -> {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                OutlinedTextField(
                                                    value = sqFtInputs[rm.id] ?: "",
                                                    onValueChange = { sqFtInputs[rm.id] = it },
                                                    label = { Text("Private Room Sq.Ft") },
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    modifier = Modifier.weight(1f),
                                                    singleLine = true
                                                )
                                                // Quick Preset buttons
                                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    listOf(120, 150, 180).forEach { size ->
                                                        SuggestionChip(
                                                            onClick = { sqFtInputs[rm.id] = size.toString() },
                                                            label = { Text("${size}sqft") }
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        RentSplitStrategy.AMENITY_BASED -> {
                                            Text(
                                                text = "Room Perks & Private Amenities:",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                FilterChip(
                                                    selected = privateBathStates[rm.id] == true,
                                                    onClick = { privateBathStates[rm.id] = !(privateBathStates[rm.id] ?: false) },
                                                    label = { Text("🛁 Bath (+25%)", style = MaterialTheme.typography.labelSmall) },
                                                    modifier = Modifier.weight(1f)
                                                )
                                                FilterChip(
                                                    selected = balconyStates[rm.id] == true,
                                                    onClick = { balconyStates[rm.id] = !(balconyStates[rm.id] ?: false) },
                                                    label = { Text("🌿 Balcony (+10%)", style = MaterialTheme.typography.labelSmall) },
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                FilterChip(
                                                    selected = parkingStates[rm.id] == true,
                                                    onClick = { parkingStates[rm.id] = !(parkingStates[rm.id] ?: false) },
                                                    label = { Text("🚗 Parking (+15%)", style = MaterialTheme.typography.labelSmall) },
                                                    modifier = Modifier.weight(1f)
                                                )
                                                FilterChip(
                                                    selected = closetStates[rm.id] == true,
                                                    onClick = { closetStates[rm.id] = !(closetStates[rm.id] ?: false) },
                                                    label = { Text("🚪 Closet (+10%)", style = MaterialTheme.typography.labelSmall) },
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                        }

                                        RentSplitStrategy.INCOME_RATIO -> {
                                            OutlinedTextField(
                                                value = incomeInputs[rm.id] ?: "",
                                                onValueChange = { incomeInputs[rm.id] = it },
                                                label = { Text("Monthly Income ($currencySymbol)") },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                leadingIcon = { Text(currencySymbol) },
                                                modifier = Modifier.fillMaxWidth(),
                                                singleLine = true
                                            )
                                        }
                                        else -> {}
                                    }
                                }
                            }
                        }
                    }

                    // --- Live Calculation Breakdown Cards ---
                    Text(
                        text = "Calculated Fair Shares",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            calculatedResult.shares.forEachIndexed { index, share ->
                                val rm = roommates.find { it.id == share.roommateId }
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(rm?.colorHex ?: 0xFF4CAF50L)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = share.roommateName.take(1),
                                                    color = Color.White,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = share.roommateName,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "(${"%.1f".format(share.percentageOfTotal)}%)",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }

                                        Text(
                                            text = "$currencySymbol${"%.2f".format(share.calculatedRent)}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    // Visual Progress bar of percentage share
                                    LinearProgressIndicator(
                                        progress = { (share.percentageOfTotal / 100.0).toFloat().coerceIn(0f, 1f) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = Color(rm?.colorHex ?: 0xFF4CAF50L),
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )

                                    if (share.detailNotes.isNotBlank()) {
                                        Text(
                                            text = share.detailNotes,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                if (index < calculatedResult.shares.size - 1) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                }
                            }
                        }
                    }
                }

                // Footer Actions
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                val desc = "Monthly Rent (${selectedStrategy.label})"
                                onRecordExpense(parsedTotalRent, desc, calculatedResult.shares)
                                onDismiss()
                            },
                            modifier = Modifier.weight(1.8f)
                        ) {
                            Icon(Icons.Default.PostAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Apply Rent Split")
                        }
                    }
                }
            }
        }
    }
}
