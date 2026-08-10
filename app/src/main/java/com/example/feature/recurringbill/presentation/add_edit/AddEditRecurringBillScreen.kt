package com.example.feature.recurringbill.presentation.add_edit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.feature.recurringbill.domain.model.BillFrequency
import com.example.feature.recurringbill.domain.model.BillType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditRecurringBillScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddEditRecurringBillViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recurring Bill") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (state.error != null) {
                Text(text = state.error!!, color = MaterialTheme.colorScheme.error)
            }

            OutlinedTextField(
                value = state.name,
                onValueChange = { viewModel.onEvent(AddEditRecurringBillEvent.NameChanged(it)) },
                label = { Text("Bill Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.expectedAmount,
                onValueChange = { viewModel.onEvent(AddEditRecurringBillEvent.AmountChanged(it)) },
                label = { Text("Expected Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            Text("Frequency", style = MaterialTheme.typography.titleSmall)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BillFrequency.values().forEach { frequency ->
                    FilterChip(
                        selected = state.frequency == frequency,
                        onClick = { viewModel.onEvent(AddEditRecurringBillEvent.FrequencyChanged(frequency)) },
                        label = { Text(frequency.name) }
                    )
                }
            }

            Text("Bill Type", style = MaterialTheme.typography.titleSmall)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BillType.values().forEach { type ->
                    FilterChip(
                        selected = state.type == type,
                        onClick = { viewModel.onEvent(AddEditRecurringBillEvent.TypeChanged(type)) },
                        label = { Text(type.name) }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { viewModel.onEvent(AddEditRecurringBillEvent.SaveBill) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Save Bill")
                }
            }
        }
    }
}
