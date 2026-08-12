package com.example.feature.chore.presentation.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.auth.domain.repository.AuthRepository
import com.example.feature.chore.domain.model.Chore
import com.example.feature.chore.domain.model.ChoreStatus
import com.example.feature.chore.domain.usecase.CompleteChoreUseCase
import com.example.feature.chore.domain.usecase.GetChoresUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ChoreListTab {
    MY_CHORES, ALL, OVERDUE, COMPLETED
}

data class ChoreListState(
    val chores: List<Chore> = emptyList(),
    val filteredChores: List<Chore> = emptyList(),
    val currentTab: ChoreListTab = ChoreListTab.MY_CHORES,
    val currentUserId: String? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class ChoreListViewModel @Inject constructor(
    private val getChoresUseCase: GetChoresUseCase,
    private val completeChoreUseCase: CompleteChoreUseCase,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val apartmentId: String = checkNotNull(savedStateHandle["apartmentId"])
    
    private val _state = MutableStateFlow(ChoreListState())
    val state = _state.asStateFlow()

    init {
        loadUserAndChores()
    }

    private fun loadUserAndChores() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            _state.value = _state.value.copy(currentUserId = user?.uid)
            
            getChoresUseCase(apartmentId)
                .onEach { chores ->
                    val sorted = chores.sortedBy { it.dueDate }
                    _state.value = _state.value.copy(
                        chores = sorted,
                        isLoading = false,
                        error = null
                    )
                    applyFilter(_state.value.currentTab)
                }
                .catch { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load chores"
                    )
                }
                .launchIn(viewModelScope)
        }
    }

    fun setTab(tab: ChoreListTab) {
        _state.value = _state.value.copy(currentTab = tab)
        applyFilter(tab)
    }

    private fun applyFilter(tab: ChoreListTab) {
        val allChores = _state.value.chores
        val currentUserId = _state.value.currentUserId
        
        val filtered = when (tab) {
            ChoreListTab.MY_CHORES -> allChores.filter { 
                it.assignedTo == currentUserId && it.status != ChoreStatus.COMPLETED 
            }
            ChoreListTab.ALL -> allChores.filter { it.status != ChoreStatus.COMPLETED }
            ChoreListTab.OVERDUE -> allChores.filter { it.isOverdue && it.status != ChoreStatus.COMPLETED }
            ChoreListTab.COMPLETED -> allChores.filter { it.status == ChoreStatus.COMPLETED }
        }
        
        _state.value = _state.value.copy(filteredChores = filtered)
    }
    
    fun quickCompleteChore(chore: Chore) {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser() ?: return@launch
            try {
                completeChoreUseCase(chore, user.uid)
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message ?: "Failed to complete chore")
            }
        }
    }
}
