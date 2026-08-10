package com.example.feature.recurringbill.presentation.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.util.Resource
import com.example.feature.recurringbill.domain.model.RecurringBill
import com.example.feature.recurringbill.domain.usecase.RecurringBillUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecurringBillListUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val bills: List<RecurringBill> = emptyList()
)

@HiltViewModel
class RecurringBillListViewModel @Inject constructor(
    private val useCases: RecurringBillUseCases,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val apartmentId: String = checkNotNull(savedStateHandle.get<String>("apartmentId"))
    private val _state = MutableStateFlow(RecurringBillListUiState())
    val state = _state.asStateFlow()

    init {
        loadBills()
    }

    private fun loadBills() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            useCases.getRecurringBills(apartmentId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.update { it.copy(isLoading = false, bills = result.data ?: emptyList()) }
                    }
                    is Resource.Error -> {
                        _state.update { it.copy(isLoading = false, error = result.message) }
                    }
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true) }
                    }
                }
            }
        }
    }
}
