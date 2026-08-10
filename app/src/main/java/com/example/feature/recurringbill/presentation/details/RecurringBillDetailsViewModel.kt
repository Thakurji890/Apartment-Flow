package com.example.feature.recurringbill.presentation.details

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

data class RecurringBillDetailsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val bill: RecurringBill? = null,
    val isDeleted: Boolean = false,
    val isGenerated: Boolean = false
)

@HiltViewModel
class RecurringBillDetailsViewModel @Inject constructor(
    private val useCases: RecurringBillUseCases,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val billId: String = checkNotNull(savedStateHandle.get<String>("billId"))
    private val _state = MutableStateFlow(RecurringBillDetailsUiState())
    val state = _state.asStateFlow()

    init {
        loadBill()
    }

    private fun loadBill() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            useCases.getRecurringBill(billId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.update { it.copy(isLoading = false, bill = result.data) }
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

    fun deleteBill() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val result = useCases.deleteRecurringBill(billId)
            when (result) {
                is Resource.Success -> {
                    _state.update { it.copy(isLoading = false, isDeleted = true) }
                }
                is Resource.Error -> {
                    _state.update { it.copy(isLoading = false, error = result.message) }
                }
                else -> {}
            }
        }
    }
    
    fun generateExpense() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val bill = _state.value.bill ?: return@launch
            val result = useCases.generateBillExpense(bill)
            when (result) {
                is Resource.Success -> {
                    _state.update { it.copy(isLoading = false, isGenerated = true) }
                }
                is Resource.Error -> {
                    _state.update { it.copy(isLoading = false, error = result.message) }
                }
                else -> {}
            }
        }
    }

    fun skipOccurrence() {
        viewModelScope.launch {
             _state.update { it.copy(isLoading = true) }
             val bill = _state.value.bill ?: return@launch
             val result = useCases.skipOccurrence(bill.id, bill.nextDueDate)
             when (result) {
                 is Resource.Success -> {
                     _state.update { it.copy(isLoading = false) }
                 }
                 is Resource.Error -> {
                     _state.update { it.copy(isLoading = false, error = result.message) }
                 }
                 else -> {}
             }
        }
    }
}
