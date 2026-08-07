package com.example.feature.settlement.presentation.list

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

data class SettlementListState(
    val isLoading: Boolean = false,
    val settlements: List<Settlement> = emptyList(),
    val error: String = ""
)

@HiltViewModel
class SettlementListViewModel @Inject constructor(
    private val useCases: SettlementUseCases,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(SettlementListState())
    val state: StateFlow<SettlementListState> = _state.asStateFlow()

    private val apartmentId: String = savedStateHandle.get<String>("apartmentId") ?: "apt-1"

    init {
        loadSettlements()
    }

    private fun loadSettlements() {
        useCases.getSettlements(apartmentId).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _state.update { it.copy(isLoading = false, settlements = result.data ?: emptyList(), error = "") }
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
}
