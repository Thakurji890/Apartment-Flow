package com.example.feature.shopping.presentation.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.shopping.domain.model.ShoppingItem
import com.example.feature.shopping.domain.model.ShoppingItemStatus
import com.example.feature.shopping.domain.model.ShoppingList
import com.example.feature.shopping.domain.repository.ShoppingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ShoppingListDetailsState(
    val list: ShoppingList? = null,
    val items: List<ShoppingItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class ShoppingListDetailsViewModel @Inject constructor(
    private val shoppingRepository: ShoppingRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val apartmentId: String = checkNotNull(savedStateHandle["apartmentId"])
    val listId: String = checkNotNull(savedStateHandle["listId"])
    
    private val _state = MutableStateFlow(ShoppingListDetailsState())
    val state = _state.asStateFlow()

    init {
        loadList()
        loadItems()
    }

    private fun loadList() {
        viewModelScope.launch {
            try {
                val list = shoppingRepository.getShoppingList(apartmentId, listId)
                _state.value = _state.value.copy(list = list)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private fun loadItems() {
        shoppingRepository.getShoppingItems(apartmentId, listId)
            .onEach { items ->
                _state.value = _state.value.copy(
                    items = items.sortedByDescending { it.createdAt }, // Simple sort for now
                    isLoading = false
                )
            }
            .catch { e ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load items"
                )
            }
            .launchIn(viewModelScope)
    }

    fun toggleItemStatus(item: ShoppingItem) {
        viewModelScope.launch {
            val newStatus = if (item.status == ShoppingItemStatus.NEEDED) {
                ShoppingItemStatus.IN_CART
            } else {
                ShoppingItemStatus.NEEDED
            }
            shoppingRepository.updateShoppingItem(apartmentId, item.copy(status = newStatus))
        }
    }
}
