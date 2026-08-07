package com.example.feature.settlement.presentation.details

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.feature.settlement.domain.model.SettlementStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettlementDetailsScreen(
    viewModel: SettlementDetailsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settlement Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.error.isNotBlank()) {
                Text(
                    text = state.error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (state.settlement != null) {
                val settlement = state.settlement!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Amount: $${String.format("%.2f", settlement.amount)}",
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Text(text = "From (Debtor): ${settlement.debtorId}")
                            Text(text = "To (Creditor): ${settlement.creditorId}")
                            Text(text = "Method: ${settlement.paymentMethod.name}")
                            Text(text = "Status: ${settlement.status.name}")
                            
                            if (settlement.note.isNotBlank()) {
                                Text(text = "Note: ${settlement.note}")
                            }
                        }
                    }

                    if (settlement.status == SettlementStatus.PENDING) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Button(
                                onClick = viewModel::confirm,
                                modifier = Modifier.weight(1f),
                                enabled = !state.isActionLoading
                            ) {
                                Text("Accept")
                            }
                            
                            OutlinedButton(
                                onClick = viewModel::reject,
                                modifier = Modifier.weight(1f),
                                enabled = !state.isActionLoading
                            ) {
                                Text("Reject", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}
