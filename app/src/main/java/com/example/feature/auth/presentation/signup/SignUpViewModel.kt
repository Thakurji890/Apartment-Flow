package com.example.feature.auth.presentation.signup

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
class SignUpViewModel @Inject constructor(
    private val authUseCases: AuthUseCases
) : ViewModel() {

    private val _state = MutableStateFlow(SignUpState())
    val state: StateFlow<SignUpState> = _state.asStateFlow()

    private val _eventFlow = MutableSharedFlow<SignUpUiEvent>()
    val eventFlow: SharedFlow<SignUpUiEvent> = _eventFlow.asSharedFlow()

    fun onEvent(event: SignUpEvent) {
        when (event) {
            is SignUpEvent.FullNameChanged -> {
                _state.update { it.copy(fullName = event.name, fullNameError = null) }
            }
            is SignUpEvent.EmailChanged -> {
                _state.update { it.copy(email = event.email, emailError = null) }
            }
            is SignUpEvent.PasswordChanged -> {
                val strength = ValidationUtil.getPasswordStrength(event.password)
                _state.update { it.copy(password = event.password, passwordError = null, passwordStrength = strength) }
            }
            is SignUpEvent.ConfirmPasswordChanged -> {
                _state.update { it.copy(confirmPassword = event.password, confirmPasswordError = null) }
            }
            is SignUpEvent.TermsAcceptedChanged -> {
                _state.update { it.copy(termsAccepted = event.isAccepted, termsAcceptedError = null) }
            }
            is SignUpEvent.Submit -> submitData()
        }
    }

    private fun submitData() {
        val fullNameResult = state.value.fullName.isNotBlank()
        val emailResult = ValidationUtil.isValidEmail(state.value.email)
        val passwordResult = ValidationUtil.isValidPassword(state.value.password)
        val confirmPasswordResult = state.value.password == state.value.confirmPassword
        val termsResult = state.value.termsAccepted

        val hasError = listOf(
            !fullNameResult,
            !emailResult,
            !passwordResult,
            !confirmPasswordResult,
            !termsResult
        ).any { it }

        if (hasError) {
            _state.update {
                it.copy(
                    fullNameError = if (!fullNameResult) "Please enter your full name" else null,
                    emailError = if (!emailResult) "Please enter a valid email address" else null,
                    passwordError = if (!passwordResult) "Password must be at least 8 characters with a letter and a number" else null,
                    confirmPasswordError = if (!confirmPasswordResult) "Passwords do not match" else null,
                    termsAcceptedError = if (!termsResult) "You must accept the terms and conditions" else null
                )
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = authUseCases.signUp(state.value.email, state.value.password, state.value.fullName)) {
                is Resource.Success -> {
                    _state.update { it.copy(isLoading = false) }
                    _eventFlow.emit(SignUpUiEvent.SignUpSuccess)
                }
                is Resource.Error -> {
                    _state.update { it.copy(isLoading = false) }
                    _eventFlow.emit(SignUpUiEvent.ShowSnackbar(result.message))
                }
                is Resource.Loading -> {
                    _state.update { it.copy(isLoading = true) }
                }
            }
        }
    }
}
