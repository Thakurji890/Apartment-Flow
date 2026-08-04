package com.example.feature.auth.presentation.signup

data class SignUpState(
    val fullName: String = "",
    val fullNameError: String? = null,
    val email: String = "",
    val emailError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val confirmPassword: String = "",
    val confirmPasswordError: String? = null,
    val termsAccepted: Boolean = false,
    val termsAcceptedError: String? = null,
    val passwordStrength: com.example.core.util.PasswordStrength = com.example.core.util.PasswordStrength.WEAK,
    val isLoading: Boolean = false
)

sealed class SignUpEvent {
    data class FullNameChanged(val name: String) : SignUpEvent()
    data class EmailChanged(val email: String) : SignUpEvent()
    data class PasswordChanged(val password: String) : SignUpEvent()
    data class ConfirmPasswordChanged(val password: String) : SignUpEvent()
    data class TermsAcceptedChanged(val isAccepted: Boolean) : SignUpEvent()
    object Submit : SignUpEvent()
}

sealed class SignUpUiEvent {
    data class ShowSnackbar(val message: String) : SignUpUiEvent()
    object SignUpSuccess : SignUpUiEvent()
}
