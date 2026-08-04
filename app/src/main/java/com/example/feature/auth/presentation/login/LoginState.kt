package com.example.feature.auth.presentation.login

data class LoginState(
    val email: String = "",
    val emailError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val rememberMe: Boolean = false,
    val isLoading: Boolean = false
)

sealed class LoginEvent {
    data class EmailChanged(val email: String) : LoginEvent()
    data class PasswordChanged(val password: String) : LoginEvent()
    data class RememberMeChanged(val isChecked: Boolean) : LoginEvent()
    object Submit : LoginEvent()
    data class SubmitGoogleSignIn(val idToken: String) : LoginEvent()
}
