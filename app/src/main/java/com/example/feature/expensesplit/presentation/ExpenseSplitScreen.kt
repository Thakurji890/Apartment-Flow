package com.example.feature.expensesplit.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.feature.expensesplit.domain.model.SplitType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseSplitScreen(
    viewModel: ExpenseSplitViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Split Expense") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Total Amount: $${state.totalAmount}")
            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SplitType.entries.forEach { type ->
                    FilterChip(
                        selected = state.splitType == type,
                        onClick = { viewModel.onSplitTypeChanged(type) },
                        label = { Text(type.name) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!state.isValid) {
                Text(
                    text = "Remaining to assign: $${String.format("%.2f", state.remainingAmount)}",
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            LazyColumn {
                items(state.splits) { split ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "User ${split.userId.take(4)}", modifier = Modifier.weight(1f))
                        
                        if (state.splitType == SplitType.EXACT || state.splitType == SplitType.CUSTOM) {
                            OutlinedTextField(
                                value = split.amount.toString(),
                                onValueChange = { 
                                    viewModel.onSplitValueChanged(split.userId, it.toDoubleOrNull() ?: 0.0) 
                                },
                                modifier = Modifier.width(120.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        } else if (state.splitType == SplitType.PERCENTAGE || state.splitType == SplitType.SHARES) {
                            OutlinedTextField(
                                value = (split.value ?: 0.0).toString(),
                                onValueChange = { 
                                    viewModel.onSplitValueChanged(split.userId, it.toDoubleOrNull() ?: 0.0) 
                                },
                                modifier = Modifier.width(80.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                suffix = { 
                                    Text(if (state.splitType == SplitType.PERCENTAGE) "%" else "sh") 
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("$${String.format("%.2f", split.amount)}")
                        } else {
                            Text("$${String.format("%.2f", split.amount)}")
                        }
                    }
                }
            }
        }
    }
}
