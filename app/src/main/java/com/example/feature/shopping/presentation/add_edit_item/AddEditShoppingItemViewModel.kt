package com.example.feature.shopping.presentation.add_edit_item

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.auth.domain.repository.AuthRepository
import com.example.feature.shopping.domain.model.ShoppingItem
import com.example.feature.shopping.domain.model.ShoppingItemPriority
import com.example.feature.shopping.domain.repository.ShoppingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddEditShoppingItemState(
    val name: String = "",
    val quantity: String = "",
    val unit: String = "",
    val category: String = "Other",
    val notes: String = "",
    val priority: ShoppingItemPriority = ShoppingItemPriority.NORMAL,
    val estimatedPrice: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class AddEditShoppingItemEvent {
    object SaveSuccess : AddEditShoppingItemEvent()
}

@HiltViewModel
class AddEditShoppingItemViewModel @Inject constructor(
    private val shoppingRepository: ShoppingRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val apartmentId: String = checkNotNull(savedStateHandle["apartmentId"])
    private val listId: String = checkNotNull(savedStateHandle["listId"])
    private val itemId: String? = savedStateHandle["itemId"]

    private val _state = MutableStateFlow(AddEditShoppingItemState())
    val state = _state.asStateFlow()

    private val _eventFlow = MutableSharedFlow<AddEditShoppingItemEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        if (itemId != null) {
            loadItem(itemId)
        }
    }

    private fun loadItem(id: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val item = shoppingRepository.getShoppingItem(apartmentId, listId, id)
            if (item != null) {
                _state.value = _state.value.copy(
                    name = item.name,
                    quantity = item.quantity?.toString() ?: "",
                    unit = item.unit,
                    category = item.category,
                    notes = item.notes,
                    priority = item.priority,
                    estimatedPrice = item.estimatedPrice?.toString() ?: "",
                    isLoading = false
                )
            } else {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Failed to load item"
                )
            }
        }
    }

    fun onNameChange(name: String) { _state.value = _state.value.copy(name = name) }
    fun onQuantityChange(quantity: String) { _state.value = _state.value.copy(quantity = quantity) }
    fun onUnitChange(unit: String) { _state.value = _state.value.copy(unit = unit) }
    fun onCategoryChange(category: String) { _state.value = _state.value.copy(category = category) }
    fun onNotesChange(notes: String) { _state.value = _state.value.copy(notes = notes) }
    fun onEstimatedPriceChange(price: String) { _state.value = _state.value.copy(estimatedPrice = price) }

    fun saveItem() {
        if (_state.value.name.isBlank()) {
            _state.value = _state.value.copy(error = "Name cannot be empty")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val user = authRepository.getCurrentUser()
                if (user == null) {
                    _state.value = _state.value.copy(isLoading = false, error = "User not found")
                    return@launch
                }

                val quantityDouble = _state.value.quantity.toDoubleOrNull()
                val priceDouble = _state.value.estimatedPrice.toDoubleOrNull()

                if (itemId != null) {
                    val existing = shoppingRepository.getShoppingItem(apartmentId, listId, itemId)
                    if (existing != null) {
                        shoppingRepository.updateShoppingItem(
                            apartmentId,
                            existing.copy(
                                name = _state.value.name,
                                quantity = quantityDouble,
                                unit = _state.value.unit,
                                category = _state.value.category,
                                notes = _state.value.notes,
                                estimatedPrice = priceDouble,
                                updatedAt = System.currentTimeMillis()
                            )
                        )
                    }
                } else {
                    shoppingRepository.addShoppingItem(
                        apartmentId,
                        ShoppingItem(
                            listId = listId,
                            name = _state.value.name,
                            quantity = quantityDouble,
                            unit = _state.value.unit,
                            category = _state.value.category,
                            notes = _state.value.notes,
                            estimatedPrice = priceDouble,
                            addedBy = user.uid
                        )
                    )
                }
                _state.value = _state.value.copy(isLoading = false)
                _eventFlow.emit(AddEditShoppingItemEvent.SaveSuccess)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to save item"
                )
            }
        }
    }
}
