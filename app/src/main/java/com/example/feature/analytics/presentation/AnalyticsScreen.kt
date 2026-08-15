package com.example.feature.analytics.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.feature.analytics.domain.model.AnalyticsPeriod
import com.example.feature.analytics.presentation.components.SimpleBarChart
import com.example.feature.analytics.presentation.components.SimpleDonutChart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reports & Analytics") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    PeriodSelector(
                        selected = state.selectedPeriod,
                        onSelect = { viewModel.setPeriod(it) }
                    )
                }

                item {
                    SummaryCard(state)
                }

                if (state.trends.isNotEmpty()) {
                    item {
                        Text("Spending Trend", style = MaterialTheme.typography.titleMedium)
                        Card(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                            SimpleBarChart(
                                data = state.trends,
                                modifier = Modifier.fillMaxSize().padding(16.dp)
                            )
                        }
                    }
                }

                if (state.categorySpending.isNotEmpty()) {
                    item {
                        Text("Category Analysis", style = MaterialTheme.typography.titleMedium)
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                SimpleDonutChart(
                                    data = state.categorySpending,
                                    modifier = Modifier.fillMaxWidth().height(200.dp).padding(16.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                state.categorySpending.forEach { cat ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("${cat.categoryId} (${cat.percentage.toInt()}%)")
                                        Text("₹${String.format("%.2f", cat.amount)}")
                                    }
                                }
                            }
                        }
                    }
                }

                if (state.memberSpending.isNotEmpty()) {
                    item {
                        Text("Member Spending", style = MaterialTheme.typography.titleMedium)
                    }
                    items(state.memberSpending) { member ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(member.displayName, fontWeight = FontWeight.Bold)
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Paid", style = MaterialTheme.typography.labelSmall)
                                        Text("₹${String.format("%.2f", member.amountPaid)}")
                                    }
                                    Column {
                                        Text("Share", style = MaterialTheme.typography.labelSmall)
                                        Text("₹${String.format("%.2f", member.actualShare)}")
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Net", style = MaterialTheme.typography.labelSmall)
                                        val color = if (member.netBalance > 0) MaterialTheme.colorScheme.primary else if (member.netBalance < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                        Text(
                                            "${if(member.netBalance > 0) "+" else ""}₹${String.format("%.2f", member.netBalance)}",
                                            color = color,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeriodSelector(
    selected: AnalyticsPeriod,
    onSelect: (AnalyticsPeriod) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selected.displayName,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            AnalyticsPeriod.values().forEach { period ->
                DropdownMenuItem(
                    text = { Text(period.displayName) },
                    onClick = {
                        onSelect(period)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun SummaryCard(state: AnalyticsState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Total Spending", style = MaterialTheme.typography.labelLarge)
            Text(
                "₹${String.format("%.2f", state.summary.totalSpending)}",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Average", style = MaterialTheme.typography.labelMedium)
                    Text("₹${String.format("%.2f", state.summary.averageExpense)}", style = MaterialTheme.typography.bodyLarge)
                }
                Column {
                    Text("Expenses", style = MaterialTheme.typography.labelMedium)
                    Text("${state.summary.expenseCount}", style = MaterialTheme.typography.bodyLarge)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Top Category", style = MaterialTheme.typography.labelMedium)
                    Text(state.summary.topCategory.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.bodyLarge)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("My Spending", style = MaterialTheme.typography.titleMedium)
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("My Share", style = MaterialTheme.typography.labelMedium)
                    Text("₹${String.format("%.2f", state.personalSpending.totalShare)}", style = MaterialTheme.typography.bodyLarge)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("I Paid", style = MaterialTheme.typography.labelMedium)
                    Text("₹${String.format("%.2f", state.personalSpending.amountPaid)}", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
