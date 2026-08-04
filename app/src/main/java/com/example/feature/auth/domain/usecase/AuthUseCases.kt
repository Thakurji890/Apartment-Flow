package com.example.feature.auth.domain.usecase

import com.example.feature.auth.domain.repository.AuthRepository

data class AuthUseCases(
    val signIn: SignInUseCase,
    val signInWithGoogle: SignInWithGoogleUseCase,
    val signUp: SignUpUseCase,
    val signOut: SignOutUseCase,
    val resetPassword: ResetPasswordUseCase,
    val checkVerification: CheckVerificationUseCase,
    val sendVerificationEmail: SendVerificationEmailUseCase,
    val getCurrentUser: GetCurrentUserUseCase,
    val getOnboardingCompleted: GetOnboardingCompletedUseCase,
    val saveOnboardingCompleted: SaveOnboardingCompletedUseCase
)
