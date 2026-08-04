package com.example.feature.apartment.presentation.join

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.util.Resource
import com.example.feature.apartment.domain.usecase.ApartmentUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JoinApartmentViewModel @Inject constructor(
    private val apartmentUseCases: ApartmentUseCases
) : ViewModel() {

    private val _inviteCode = MutableStateFlow("")
    val inviteCode: StateFlow<String> = _inviteCode.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _eventFlow = MutableSharedFlow<JoinApartmentEvent>()
    val eventFlow: SharedFlow<JoinApartmentEvent> = _eventFlow.asSharedFlow()

    fun onInviteCodeChanged(code: String) {
        _inviteCode.value = code.uppercase()
        _error.value = null
    }

    fun submitInviteCode() {
        if (_inviteCode.value.isBlank()) {
            _error.value = "Invite code is required"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            when (val result = apartmentUseCases.joinApartment(_inviteCode.value)) {
                is Resource.Success -> {
                    _isLoading.value = false
                    _eventFlow.emit(JoinApartmentEvent.Success)
                }
                is Resource.Error -> {
                    _isLoading.value = false
                    _error.value = result.message
                    _eventFlow.emit(JoinApartmentEvent.ShowSnackbar(result.message))
                }
                is Resource.Loading -> {
                    _isLoading.value = true
                }
            }
        }
    }
}

sealed class JoinApartmentEvent {
    data class ShowSnackbar(val message: String) : JoinApartmentEvent()
    object Success : JoinApartmentEvent()
}
