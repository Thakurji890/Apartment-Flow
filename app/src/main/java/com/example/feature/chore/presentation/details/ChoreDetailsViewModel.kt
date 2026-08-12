package com.example.feature.chore.presentation.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.util.Resource
import com.example.feature.apartment.domain.model.ApartmentMember
import com.example.feature.apartment.domain.repository.ApartmentRepository
import com.example.feature.auth.domain.repository.AuthRepository
import com.example.feature.chore.domain.model.Chore
import com.example.feature.chore.domain.repository.ChoreRepository
import com.example.feature.chore.domain.usecase.CompleteChoreUseCase
import com.example.feature.chore.domain.usecase.DeleteChoreUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChoreDetailsState(
    val chore: Chore? = null,
    val assignee: ApartmentMember? = null,
    val creator: ApartmentMember? = null,
    val completedBy: ApartmentMember? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

sealed class ChoreDetailsEvent {
    object DeleteSuccess : ChoreDetailsEvent()
    object CompleteSuccess : ChoreDetailsEvent()
}

@HiltViewModel
class ChoreDetailsViewModel @Inject constructor(
    private val choreRepository: ChoreRepository,
    private val apartmentRepository: ApartmentRepository,
    private val authRepository: AuthRepository,
    private val completeChoreUseCase: CompleteChoreUseCase,
    private val deleteChoreUseCase: DeleteChoreUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val apartmentId: String = checkNotNull(savedStateHandle["apartmentId"])
    val choreId: String = checkNotNull(savedStateHandle["choreId"])

    private val _state = MutableStateFlow(ChoreDetailsState())
    val state = _state.asStateFlow()

    private val _eventFlow = MutableSharedFlow<ChoreDetailsEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        loadChore()
    }

    fun loadChore() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val chore = choreRepository.getChoreById(apartmentId, choreId)
                if (chore != null) {
                    val result = apartmentRepository.getApartmentMembers(apartmentId).first()
                    val members = if (result is Resource.Success) result.data ?: emptyList() else emptyList()
                    val assignee = members.find { it.userId == chore.assignedTo }
                    val creator = members.find { it.userId == chore.createdBy }
                    val completedBy = chore.completedBy?.let { id -> members.find { it.userId == id } }
                    
                    _state.value = _state.value.copy(
                        chore = chore,
                        assignee = assignee,
                        creator = creator,
                        completedBy = completedBy,
                        isLoading = false
                    )
                } else {
                    _state.value = _state.value.copy(isLoading = false, error = "Chore not found")
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun deleteChore() {
        viewModelScope.launch {
            try {
                deleteChoreUseCase(apartmentId, choreId)
                _eventFlow.emit(ChoreDetailsEvent.DeleteSuccess)
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message ?: "Failed to delete chore")
            }
        }
    }

    fun completeChore(notes: String = "") {
        viewModelScope.launch {
            val chore = _state.value.chore ?: return@launch
            val user = authRepository.getCurrentUser() ?: return@launch
            try {
                completeChoreUseCase(chore, user.uid, notes)
                _eventFlow.emit(ChoreDetailsEvent.CompleteSuccess)
                loadChore() // Reload to show completed state
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message ?: "Failed to complete chore")
            }
        }
    }
}
