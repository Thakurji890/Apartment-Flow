package com.example.feature.auth.presentation.forgotpassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.util.Resource
import com.example.core.util.ValidationUtil
import com.example.feature.auth.domain.usecase.AuthUseCases
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
class ForgotPasswordViewModel @Inject constructor(
    private val authUseCases: AuthUseCases
) : ViewModel() {

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _eventFlow = MutableSharedFlow<ForgotPasswordEvent>()
    val eventFlow: SharedFlow<ForgotPasswordEvent> = _eventFlow.asSharedFlow()

    fun onEmailChanged(email: String) {
        _email.value = email
    }

    fun submitEmail() {
        if (!ValidationUtil.isValidEmail(_email.value)) {
            viewModelScope.launch {
                _eventFlow.emit(ForgotPasswordEvent.ShowSnackbar("Please enter a valid email address"))
            }
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            when (val result = authUseCases.resetPassword(_email.value)) {
                is Resource.Success -> {
                    _isLoading.value = false
                    _eventFlow.emit(ForgotPasswordEvent.ResetEmailSent)
                }
                is Resource.Error -> {
                    _isLoading.value = false
                    _eventFlow.emit(ForgotPasswordEvent.ShowSnackbar(result.message))
                }
                is Resource.Loading -> {
                    _isLoading.value = true
                }
            }
        }
    }
}

sealed class ForgotPasswordEvent {
    data class ShowSnackbar(val message: String) : ForgotPasswordEvent()
    object ResetEmailSent : ForgotPasswordEvent()
}
