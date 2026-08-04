package com.example.feature.auth.presentation.emailverification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.util.Resource
import com.example.feature.auth.domain.usecase.AuthUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EmailVerificationViewModel @Inject constructor(
    private val authUseCases: AuthUseCases
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _eventFlow = MutableSharedFlow<EmailVerificationEvent>()
    val eventFlow: SharedFlow<EmailVerificationEvent> = _eventFlow.asSharedFlow()

    fun checkVerificationStatus() {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = authUseCases.checkVerification()) {
                is Resource.Success -> {
                    _isLoading.value = false
                    if (result.data) {
                        _eventFlow.emit(EmailVerificationEvent.VerificationSuccess)
                    } else {
                        _eventFlow.emit(EmailVerificationEvent.ShowSnackbar("Email not yet verified. Please check your inbox."))
                    }
                }
                is Resource.Error -> {
                    _isLoading.value = false
                    _eventFlow.emit(EmailVerificationEvent.ShowSnackbar(result.message))
                }
                is Resource.Loading -> {
                    _isLoading.value = true
                }
            }
        }
    }

    fun resendVerificationEmail() {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = authUseCases.sendVerificationEmail()) {
                is Resource.Success -> {
                    _isLoading.value = false
                    _eventFlow.emit(EmailVerificationEvent.ShowSnackbar("Verification email sent!"))
                }
                is Resource.Error -> {
                    _isLoading.value = false
                    _eventFlow.emit(EmailVerificationEvent.ShowSnackbar(result.message))
                }
                is Resource.Loading -> {
                    _isLoading.value = true
                }
            }
        }
    }

    fun signOut(onNavigateToWelcome: () -> Unit) {
        viewModelScope.launch {
            authUseCases.signOut()
            onNavigateToWelcome()
        }
    }
}

sealed class EmailVerificationEvent {
    data class ShowSnackbar(val message: String) : EmailVerificationEvent()
    object VerificationSuccess : EmailVerificationEvent()
}
