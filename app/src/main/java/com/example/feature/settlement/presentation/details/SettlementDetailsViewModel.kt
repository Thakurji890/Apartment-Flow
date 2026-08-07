package com.example.feature.settlement.presentation.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.util.Resource
import com.example.feature.settlement.domain.model.Settlement
import com.example.feature.settlement.domain.usecase.SettlementUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettlementDetailsState(
    val isLoading: Boolean = false,
    val settlement: Settlement? = null,
    val error: String = "",
    val isActionLoading: Boolean = false
)

@HiltViewModel
class SettlementDetailsViewModel @Inject constructor(
    private val useCases: SettlementUseCases,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(SettlementDetailsState())
    val state: StateFlow<SettlementDetailsState> = _state.asStateFlow()

    private val settlementId: String = checkNotNull(savedStateHandle.get<String>("settlementId"))

    init {
        loadSettlement()
    }

    private fun loadSettlement() {
        useCases.getSettlement(settlementId).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _state.update { it.copy(isLoading = false, settlement = result.data, error = "") }
                }
                is Resource.Error -> {
                    _state.update { it.copy(isLoading = false, error = result.message ?: "An error occurred") }
                }
                is Resource.Loading -> {
                    _state.update { it.copy(isLoading = true) }
                }
            }
        }.launchIn(viewModelScope)
    }
    
    fun confirm() {
        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true) }
            val result = useCases.confirmSettlement(settlementId)
            _state.update { it.copy(isActionLoading = false) }
        }
    }

    fun reject() {
        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true) }
            val result = useCases.rejectSettlement(settlementId)
            _state.update { it.copy(isActionLoading = false) }
        }
    }
}
