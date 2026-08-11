package com.example.feature.shopping.presentation.purchase_item

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.auth.domain.repository.AuthRepository
import com.example.feature.shopping.domain.repository.ShoppingRepository
import com.example.feature.shopping.domain.usecase.ConvertPurchaseToExpenseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PurchaseItemState(
    val itemName: String = "",
    val actualPrice: String = "",
    val notes: String = "",
    val addToExpense: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class PurchaseItemEvent {
    object PurchaseSuccess : PurchaseItemEvent()
}

@HiltViewModel
class PurchaseItemViewModel @Inject constructor(
    private val shoppingRepository: ShoppingRepository,
    private val convertPurchaseToExpenseUseCase: ConvertPurchaseToExpenseUseCase,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val apartmentId: String = checkNotNull(savedStateHandle["apartmentId"])
    private val listId: String = checkNotNull(savedStateHandle["listId"])
    private val itemId: String = checkNotNull(savedStateHandle["itemId"])

    private val _state = MutableStateFlow(PurchaseItemState())
    val state = _state.asStateFlow()

    private val _eventFlow = MutableSharedFlow<PurchaseItemEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        loadItem()
    }

    private fun loadItem() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val item = shoppingRepository.getShoppingItem(apartmentId, listId, itemId)
            if (item != null) {
                _state.value = _state.value.copy(
                    itemName = item.name,
                    actualPrice = item.estimatedPrice?.toString() ?: "",
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

    fun onActualPriceChange(price: String) { _state.value = _state.value.copy(actualPrice = price) }
    fun onNotesChange(notes: String) { _state.value = _state.value.copy(notes = notes) }
    fun onAddToExpenseChange(add: Boolean) { _state.value = _state.value.copy(addToExpense = add) }

    fun confirmPurchase() {
        val price = _state.value.actualPrice.toDoubleOrNull()
        if (price == null || price <= 0) {
            _state.value = _state.value.copy(error = "Please enter a valid price")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            if (_state.value.addToExpense) {
                val result = convertPurchaseToExpenseUseCase(
                    apartmentId = apartmentId,
                    listId = listId,
                    itemId = itemId,
                    actualPrice = price,
                    notes = _state.value.notes
                )
                if (result.isSuccess) {
                    _state.value = _state.value.copy(isLoading = false)
                    _eventFlow.emit(PurchaseItemEvent.PurchaseSuccess)
                } else {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Failed to convert purchase to expense"
                    )
                }
            } else {
                // Just update item without expense
                val item = shoppingRepository.getShoppingItem(apartmentId, listId, itemId)
                if (item != null) {
                    val user = authRepository.getCurrentUser()
                    shoppingRepository.updateShoppingItem(
                        apartmentId,
                        item.copy(
                            status = com.example.feature.shopping.domain.model.ShoppingItemStatus.PURCHASED,
                            purchasedPrice = price,
                            purchasedBy = user?.uid,
                            purchasedAt = System.currentTimeMillis()
                        )
                    )
                    _state.value = _state.value.copy(isLoading = false)
                    _eventFlow.emit(PurchaseItemEvent.PurchaseSuccess)
                } else {
                    _state.value = _state.value.copy(isLoading = false, error = "Item not found")
                }
            }
        }
    }
}
