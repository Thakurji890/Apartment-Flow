package com.example.feature.shopping.presentation.details

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.feature.shopping.domain.model.ShoppingItem
import com.example.feature.shopping.domain.model.ShoppingItemStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListDetailsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddItem: (String) -> Unit,
    onNavigateToEditItem: (String, String) -> Unit,
    onNavigateToPurchaseItem: (String, String) -> Unit,
    viewModel: ShoppingListDetailsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.list?.name ?: "Shopping List") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToAddItem(viewModel.listId) }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Item")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.items.isEmpty()) {
                Text(
                    "List is empty. Add some items!",
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
            } else {
                val activeItems = state.items.filter { it.status != ShoppingItemStatus.PURCHASED }
                val purchasedItems = state.items.filter { it.status == ShoppingItemStatus.PURCHASED }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (activeItems.isNotEmpty()) {
                        item {
                            Text("To Buy", style = MaterialTheme.typography.titleMedium)
                        }
                        items(activeItems) { item ->
                            ShoppingItemRow(
                                item = item,
                                onToggleStatus = { viewModel.toggleItemStatus(item) },
                                onClick = { onNavigateToEditItem(viewModel.listId, item.id) },
                                onPurchase = { onNavigateToPurchaseItem(viewModel.listId, item.id) }
                            )
                        }
                    }

                    if (purchasedItems.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Purchased", style = MaterialTheme.typography.titleMedium)
                        }
                        items(purchasedItems) { item ->
                            ShoppingItemRow(
                                item = item,
                                onToggleStatus = { },
                                onClick = { onNavigateToEditItem(viewModel.listId, item.id) },
                                onPurchase = { }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ShoppingItemRow(
    item: ShoppingItem,
    onToggleStatus: () -> Unit,
    onClick: () -> Unit,
    onPurchase: () -> Unit
) {
    val isPurchased = item.status == ShoppingItemStatus.PURCHASED
    val isInCart = item.status == ShoppingItemStatus.IN_CART
    
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isPurchased) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isInCart || isPurchased,
                onCheckedChange = { if (!isPurchased) onToggleStatus() },
                enabled = !isPurchased
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (isPurchased) TextDecoration.LineThrough else TextDecoration.None
                )
                if (item.quantity != null) {
                    Text(
                        text = "${item.quantity} ${item.unit}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            if (!isPurchased) {
                IconButton(onClick = onPurchase) {
                    Icon(
                        imageVector = Icons.Filled.ShoppingCart,
                        contentDescription = "Mark Purchased",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Purchased",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}
