package com.example.feature.chore.presentation.add_edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.util.Resource
import com.example.feature.apartment.domain.model.ApartmentMember
import com.example.feature.apartment.domain.repository.ApartmentRepository
import com.example.feature.auth.domain.repository.AuthRepository
import com.example.feature.chore.domain.model.Chore
import com.example.feature.chore.domain.model.ChoreCategory
import com.example.feature.chore.domain.model.ChorePriority
import com.example.feature.chore.domain.model.ChoreRecurrence
import com.example.feature.chore.domain.repository.ChoreRepository
import com.example.feature.chore.domain.usecase.SaveChoreUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddEditChoreState(
    val choreId: String? = null,
    val name: String = "",
    val description: String = "",
    val category: ChoreCategory = ChoreCategory.OTHER,
    val priority: ChorePriority = ChorePriority.NORMAL,
    val assignedTo: String? = null,
    val recurrence: ChoreRecurrence = ChoreRecurrence.NONE,
    val points: Int = 1,
    val dueDate: Long? = null,
    val availableMembers: List<ApartmentMember> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class AddEditChoreEvent {
    object SaveSuccess : AddEditChoreEvent()
}

@HiltViewModel
class AddEditChoreViewModel @Inject constructor(
    private val saveChoreUseCase: SaveChoreUseCase,
    private val choreRepository: ChoreRepository,
    private val apartmentRepository: ApartmentRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val apartmentId: String = checkNotNull(savedStateHandle["apartmentId"])
    private val choreId: String? = savedStateHandle["choreId"]

    private val _state = MutableStateFlow(AddEditChoreState(choreId = choreId))
    val state = _state.asStateFlow()

    private val _eventFlow = MutableSharedFlow<AddEditChoreEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            apartmentRepository.getApartmentMembers(apartmentId).onEach { result ->
                if (result is Resource.Success) {
                    _state.value = _state.value.copy(availableMembers = result.data ?: emptyList())
                }
            }.launchIn(viewModelScope)
            
            if (choreId != null) {
                val chore = choreRepository.getChoreById(apartmentId, choreId)
                if (chore != null) {
                    _state.value = _state.value.copy(
                        name = chore.name,
                        description = chore.description,
                        category = chore.category,
                        priority = chore.priority,
                        assignedTo = chore.assignedTo,
                        recurrence = chore.recurrence,
                        points = chore.points,
                        dueDate = chore.dueDate.takeIf { it > 0 },
                        isLoading = false
                    )
                } else {
                    _state.value = _state.value.copy(isLoading = false, error = "Chore not found")
                }
            } else {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    fun onNameChange(name: String) { _state.value = _state.value.copy(name = name) }
    fun onDescriptionChange(desc: String) { _state.value = _state.value.copy(description = desc) }
    fun onCategoryChange(cat: ChoreCategory) { _state.value = _state.value.copy(category = cat) }
    fun onPriorityChange(prio: ChorePriority) { _state.value = _state.value.copy(priority = prio) }
    fun onAssignedToChange(userId: String?) { _state.value = _state.value.copy(assignedTo = userId) }
    fun onRecurrenceChange(rec: ChoreRecurrence) { _state.value = _state.value.copy(recurrence = rec) }
    fun onPointsChange(points: Int) { _state.value = _state.value.copy(points = points) }
    fun onDueDateChange(date: Long?) { _state.value = _state.value.copy(dueDate = date) }

    fun saveChore() {
        if (_state.value.name.isBlank()) {
            _state.value = _state.value.copy(error = "Chore name cannot be empty")
            return
        }
        
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val user = authRepository.getCurrentUser()
                
                val currentChore = choreId?.let { choreRepository.getChoreById(apartmentId, it) }
                
                val choreToSave = currentChore?.copy(
                    name = _state.value.name,
                    description = _state.value.description,
                    category = _state.value.category,
                    priority = _state.value.priority,
                    assignedTo = _state.value.assignedTo,
                    recurrence = _state.value.recurrence,
                    points = _state.value.points,
                    dueDate = _state.value.dueDate ?: 0L
                ) ?: Chore(
                    apartmentId = apartmentId,
                    name = _state.value.name,
                    description = _state.value.description,
                    category = _state.value.category,
                    priority = _state.value.priority,
                    assignedTo = _state.value.assignedTo,
                    recurrence = _state.value.recurrence,
                    points = _state.value.points,
                    dueDate = _state.value.dueDate ?: 0L,
                    createdBy = user?.uid ?: ""
                )
                
                saveChoreUseCase(choreToSave)
                
                _state.value = _state.value.copy(isLoading = false)
                _eventFlow.emit(AddEditChoreEvent.SaveSuccess)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to save chore"
                )
            }
        }
    }
}
