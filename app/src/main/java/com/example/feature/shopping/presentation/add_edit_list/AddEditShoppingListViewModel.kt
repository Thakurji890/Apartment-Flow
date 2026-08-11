package com.example.feature.shopping.presentation.add_edit_list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.auth.domain.repository.AuthRepository
import com.example.feature.shopping.domain.model.ShoppingList
import com.example.feature.shopping.domain.repository.ShoppingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddEditShoppingListState(
    val name: String = "",
    val description: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class AddEditShoppingListEvent {
    object SaveSuccess : AddEditShoppingListEvent()
}

@HiltViewModel
class AddEditShoppingListViewModel @Inject constructor(
    private val shoppingRepository: ShoppingRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val apartmentId: String = checkNotNull(savedStateHandle["apartmentId"])
    private val listId: String? = savedStateHandle["listId"]

    private val _state = MutableStateFlow(AddEditShoppingListState())
    val state = _state.asStateFlow()

    private val _eventFlow = MutableSharedFlow<AddEditShoppingListEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        if (listId != null) {
            loadList(listId)
        }
    }

    private fun loadList(id: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val list = shoppingRepository.getShoppingList(apartmentId, id)
            if (list != null) {
                _state.value = _state.value.copy(
                    name = list.name,
                    description = list.description,
                    isLoading = false
                )
            } else {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Failed to load list"
                )
            }
        }
    }

    fun onNameChange(name: String) {
        _state.value = _state.value.copy(name = name)
    }

    fun onDescriptionChange(desc: String) {
        _state.value = _state.value.copy(description = desc)
    }

    fun saveList() {
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

                if (listId != null) {
                    val existing = shoppingRepository.getShoppingList(apartmentId, listId)
                    if (existing != null) {
                        shoppingRepository.updateShoppingList(
                            existing.copy(
                                name = _state.value.name,
                                description = _state.value.description,
                                updatedAt = System.currentTimeMillis()
                            )
                        )
                    }
                } else {
                    shoppingRepository.createShoppingList(
                        ShoppingList(
                            apartmentId = apartmentId,
                            name = _state.value.name,
                            description = _state.value.description,
                            createdBy = user.uid
                        )
                    )
                }
                _state.value = _state.value.copy(isLoading = false)
                _eventFlow.emit(AddEditShoppingListEvent.SaveSuccess)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to save list"
                )
            }
        }
    }
}
