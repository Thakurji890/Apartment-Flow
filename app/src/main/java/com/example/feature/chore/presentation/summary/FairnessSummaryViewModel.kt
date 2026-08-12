package com.example.feature.chore.presentation.summary

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.util.Resource
import com.example.feature.apartment.domain.model.ApartmentMember
import com.example.feature.apartment.domain.repository.ApartmentRepository
import com.example.feature.chore.domain.model.FairnessSummary
import com.example.feature.chore.domain.usecase.GetFairnessSummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FairnessSummaryState(
    val summary: FairnessSummary? = null,
    val members: List<ApartmentMember> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class FairnessSummaryViewModel @Inject constructor(
    private val getFairnessSummaryUseCase: GetFairnessSummaryUseCase,
    private val apartmentRepository: ApartmentRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val apartmentId: String = checkNotNull(savedStateHandle["apartmentId"])

    private val _state = MutableStateFlow(FairnessSummaryState())
    val state = _state.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            apartmentRepository.getApartmentMembers(apartmentId).onEach { result ->
                if (result is Resource.Success) {
                    _state.value = _state.value.copy(members = result.data ?: emptyList())
                }
            }.launchIn(viewModelScope)
            
            getFairnessSummaryUseCase(apartmentId)
                .onEach { summary ->
                    _state.value = _state.value.copy(
                        summary = summary,
                        isLoading = false,
                        error = null
                    )
                }
                .catch { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load summary"
                    )
                }
                .launchIn(viewModelScope)
        }
    }
}
