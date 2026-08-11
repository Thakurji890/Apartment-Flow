package com.example.feature.shopping.presentation.lists

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

data class ShoppingListsState(
    val lists: List<ShoppingList> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class ShoppingListsViewModel @Inject constructor(
    private val shoppingRepository: ShoppingRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val apartmentId: String = checkNotNull(savedStateHandle["apartmentId"])
    
    private val _state = MutableStateFlow(ShoppingListsState())
    val state = _state.asStateFlow()

    init {
        getLists()
    }

    private fun getLists() {
        shoppingRepository.getShoppingLists(apartmentId)
            .onEach { lists ->
                _state.value = state.value.copy(
                    lists = lists,
                    isLoading = false,
                    error = null
                )
            }
            .catch { e ->
                _state.value = state.value.copy(
                    isLoading = false,
                    error = e.message ?: "An unexpected error occurred"
                )
            }
            .launchIn(viewModelScope)
    }
}
