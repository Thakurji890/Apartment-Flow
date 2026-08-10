package com.example.feature.recurringbill.presentation.add_edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.util.Resource
import com.example.feature.auth.domain.repository.AuthRepository
import com.example.feature.recurringbill.domain.model.BillFrequency
import com.example.feature.recurringbill.domain.model.BillType
import com.example.feature.recurringbill.domain.model.RecurringBill
import com.example.feature.recurringbill.domain.usecase.RecurringBillUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddEditRecurringBillUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false,
    
    val name: String = "",
    val expectedAmount: String = "",
    val frequency: BillFrequency = BillFrequency.MONTHLY,
    val type: BillType = BillType.FIXED
)

@HiltViewModel
class AddEditRecurringBillViewModel @Inject constructor(
    private val useCases: RecurringBillUseCases,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val apartmentId: String = checkNotNull(savedStateHandle.get<String>("apartmentId"))
    private val billId: String? = savedStateHandle.get<String>("billId")
    
    private val _state = MutableStateFlow(AddEditRecurringBillUiState())
    val state = _state.asStateFlow()
    
    private var existingBill: RecurringBill? = null

    init {
        billId?.let {
            if (it.isNotBlank()) loadBill(it)
        }
    }

    private fun loadBill(id: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            useCases.getRecurringBill(id).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        existingBill = result.data
                        _state.update { 
                            it.copy(
                                isLoading = false,
                                name = existingBill?.name ?: "",
                                expectedAmount = existingBill?.expectedAmount?.toString() ?: "",
                                frequency = existingBill?.frequency ?: BillFrequency.MONTHLY,
                                type = existingBill?.type ?: BillType.FIXED
                            )
                        }
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

    fun onEvent(event: AddEditRecurringBillEvent) {
        when (event) {
            is AddEditRecurringBillEvent.NameChanged -> _state.update { it.copy(name = event.name) }
            is AddEditRecurringBillEvent.AmountChanged -> _state.update { it.copy(expectedAmount = event.amount) }
            is AddEditRecurringBillEvent.FrequencyChanged -> _state.update { it.copy(frequency = event.frequency) }
            is AddEditRecurringBillEvent.TypeChanged -> _state.update { it.copy(type = event.type) }
            is AddEditRecurringBillEvent.SaveBill -> saveBill()
        }
    }

    private fun saveBill() {
        val amount = _state.value.expectedAmount.toDoubleOrNull()
        if (_state.value.name.isBlank() || amount == null || amount <= 0.0) {
            _state.update { it.copy(error = "Please enter valid details") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            val userId = authRepository.getCurrentUser()?.uid ?: return@launch
            
            val bill = existingBill?.copy(
                name = _state.value.name,
                expectedAmount = amount,
                frequency = _state.value.frequency,
                type = _state.value.type,
                updatedAt = System.currentTimeMillis()
            ) ?: RecurringBill(
                apartmentId = apartmentId,
                name = _state.value.name,
                expectedAmount = amount,
                frequency = _state.value.frequency,
                type = _state.value.type,
                paidBy = userId, // Simplification for MVP
                createdBy = userId,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            val result = if (existingBill != null) {
                useCases.updateRecurringBill(bill)
            } else {
                useCases.createRecurringBill(bill)
            }

            when (result) {
                is Resource.Success -> {
                    _state.update { it.copy(isLoading = false, isSaved = true) }
                }
                is Resource.Error -> {
                    _state.update { it.copy(isLoading = false, error = result.message) }
                }
                else -> {}
            }
        }
    }
}

sealed class AddEditRecurringBillEvent {
    data class NameChanged(val name: String) : AddEditRecurringBillEvent()
    data class AmountChanged(val amount: String) : AddEditRecurringBillEvent()
    data class FrequencyChanged(val frequency: BillFrequency) : AddEditRecurringBillEvent()
    data class TypeChanged(val type: BillType) : AddEditRecurringBillEvent()
    object SaveBill : AddEditRecurringBillEvent()
}
