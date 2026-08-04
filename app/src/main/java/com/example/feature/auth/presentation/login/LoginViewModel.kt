package com.example.feature.auth.presentation.login

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
class LoginViewModel @Inject constructor(
    private val authUseCases: AuthUseCases
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow: SharedFlow<UiEvent> = _eventFlow.asSharedFlow()

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.EmailChanged -> {
                _state.update { it.copy(email = event.email, emailError = null) }
            }
            is LoginEvent.PasswordChanged -> {
                _state.update { it.copy(password = event.password, passwordError = null) }
            }
            is LoginEvent.RememberMeChanged -> {
                _state.update { it.copy(rememberMe = event.isChecked) }
            }
            is LoginEvent.Submit -> {
                submitData()
            }
            is LoginEvent.SubmitGoogleSignIn -> {
                submitGoogleData(event.idToken)
            }
        }
    }

    private fun submitData() {
        val emailResult = ValidationUtil.isValidEmail(state.value.email)
        val passwordResult = state.value.password.isNotEmpty()

        val hasError = listOf(
            !emailResult,
            !passwordResult
        ).any { it }

        if (hasError) {
            _state.update {
                it.copy(
                    emailError = if (!emailResult) "Please enter a valid email address" else null,
                    passwordError = if (!passwordResult) "Please enter your password" else null
                )
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = authUseCases.signIn(state.value.email, state.value.password)) {
                is Resource.Success -> {
                    _state.update { it.copy(isLoading = false) }
                    _eventFlow.emit(UiEvent.LoginSuccess(result.data.isEmailVerified))
                }
                is Resource.Error -> {
                    _state.update { it.copy(isLoading = false) }
                    _eventFlow.emit(UiEvent.ShowSnackbar(result.message))
                }
                is Resource.Loading -> {
                    _state.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    private fun submitGoogleData(idToken: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = authUseCases.signInWithGoogle(idToken)) {
                is Resource.Success -> {
                    _state.update { it.copy(isLoading = false) }
                    _eventFlow.emit(UiEvent.LoginSuccess(result.data.isEmailVerified))
                }
                is Resource.Error -> {
                    _state.update { it.copy(isLoading = false) }
                    _eventFlow.emit(UiEvent.ShowSnackbar(result.message))
                }
                is Resource.Loading -> {
                    _state.update { it.copy(isLoading = true) }
                }
            }
        }
    }
}

sealed class UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent()
    data class LoginSuccess(val isEmailVerified: Boolean) : UiEvent()
}
