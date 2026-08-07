package com.example.feature.settlement.presentation.create

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.util.Resource
import com.example.feature.settlement.domain.model.PaymentMethod
import com.example.feature.settlement.domain.model.Settlement
import com.example.feature.settlement.domain.usecase.SettlementUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateSettlementState(
    val isLoading: Boolean = false,
    val debtorId: String = "",
    val creditorId: String = "",
    val amount: String = "",
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val note: String = "",
    val error: String = "",
    val isSuccess: Boolean = false
)

@HiltViewModel
class CreateSettlementViewModel @Inject constructor(
    private val useCases: SettlementUseCases,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(CreateSettlementState())
    val state: StateFlow<CreateSettlementState> = _state.asStateFlow()

    private val apartmentId: String = savedStateHandle.get<String>("apartmentId") ?: "apt-1"

    fun onDebtorChanged(debtor: String) {
        _state.update { it.copy(debtorId = debtor) }
    }
    
    fun onCreditorChanged(creditor: String) {
        _state.update { it.copy(creditorId = creditor) }
    }
    
    fun onAmountChanged(amount: String) {
        _state.update { it.copy(amount = amount) }
    }
    
    fun onPaymentMethodChanged(method: PaymentMethod) {
        _state.update { it.copy(paymentMethod = method) }
    }
    
    fun onNoteChanged(note: String) {
        _state.update { it.copy(note = note) }
    }

    fun submit() {
        val currentState = state.value
        val amountDouble = currentState.amount.toDoubleOrNull()
        
        if (amountDouble == null || amountDouble <= 0) {
            _state.update { it.copy(error = "Enter a valid amount") }
            return
        }
        
        if (currentState.debtorId.isBlank() || currentState.creditorId.isBlank()) {
            _state.update { it.copy(error = "Select both debtor and creditor") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = "") }
            
            val settlement = Settlement(
                apartmentId = apartmentId,
                debtorId = currentState.debtorId,
                creditorId = currentState.creditorId,
                amount = amountDouble,
                paymentMethod = currentState.paymentMethod,
                note = currentState.note
            )
            
            when (val result = useCases.createSettlement(settlement)) {
                is Resource.Success -> {
                    _state.update { it.copy(isLoading = false, isSuccess = true) }
                }
                is Resource.Error -> {
                    _state.update { it.copy(isLoading = false, error = result.message ?: "Failed to create") }
                }
                is Resource.Loading -> { }
            }
        }
    }
}
