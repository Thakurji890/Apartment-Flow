package com.example.feature.expense.presentation.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.util.Resource
import com.example.feature.expense.domain.model.Expense
import com.example.feature.expense.domain.model.ExpenseFilter
import com.example.feature.expense.domain.model.ExpenseSortOrder
import com.example.feature.expense.domain.usecase.ExpenseUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpenseListViewModel @Inject constructor(
    private val useCases: ExpenseUseCases,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val apartmentId = savedStateHandle.get<String>("apartmentId") ?: ""

    private val _state = MutableStateFlow(ExpenseListState())
    val state = _state.asStateFlow()

    init {
        loadExpenses()
        syncExpenses()
    }

    fun onEvent(event: ExpenseListEvent) {
        when (event) {
            is ExpenseListEvent.OnSearchQueryChanged -> {
                _state.update { it.copy(searchQuery = event.query) }
                loadExpenses()
            }
            is ExpenseListEvent.OnSortOrderChanged -> {
                _state.update { it.copy(sortOrder = event.sortOrder) }
                loadExpenses()
            }
            is ExpenseListEvent.OnFilterChanged -> {
                _state.update { it.copy(filter = event.filter) }
                loadExpenses()
            }
            is ExpenseListEvent.Refresh -> {
                syncExpenses()
            }
        }
    }

    private fun loadExpenses() {
        if (apartmentId.isBlank()) return
        
        viewModelScope.launch {
            useCases.getExpenses(
                apartmentId = apartmentId,
                searchQuery = state.value.searchQuery,
                filter = state.value.filter,
                sortOrder = state.value.sortOrder
            ).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.update { it.copy(
                            expenses = result.data ?: emptyList(),
                            isLoading = false,
                            error = null
                        ) }
                    }
                    is Resource.Error -> {
                        _state.update { it.copy(
                            error = result.message,
                            isLoading = false
                        ) }
                    }
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true) }
                    }
                }
            }
        }
    }

    private fun syncExpenses() {
        if (apartmentId.isBlank()) return
        
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true) }
            useCases.syncExpenses(apartmentId)
            _state.update { it.copy(isRefreshing = false) }
        }
    }
}

data class ExpenseListState(
    val expenses: List<Expense> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val sortOrder: ExpenseSortOrder = ExpenseSortOrder.NEWEST_FIRST,
    val filter: ExpenseFilter = ExpenseFilter()
)

sealed class ExpenseListEvent {
    data class OnSearchQueryChanged(val query: String) : ExpenseListEvent()
    data class OnSortOrderChanged(val sortOrder: ExpenseSortOrder) : ExpenseListEvent()
    data class OnFilterChanged(val filter: ExpenseFilter) : ExpenseListEvent()
    object Refresh : ExpenseListEvent()
}
