package com.example.di

import com.example.feature.auth.domain.repository.AuthRepository
import com.example.feature.auth.domain.usecase.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideAuthUseCases(repository: AuthRepository): AuthUseCases {
        return AuthUseCases(
            signIn = SignInUseCase(repository),
            signInWithGoogle = SignInWithGoogleUseCase(repository),
            signUp = SignUpUseCase(repository),
            signOut = SignOutUseCase(repository),
            resetPassword = ResetPasswordUseCase(repository),
            checkVerification = CheckVerificationUseCase(repository),
            sendVerificationEmail = SendVerificationEmailUseCase(repository),
            getCurrentUser = GetCurrentUserUseCase(repository),
            getOnboardingCompleted = GetOnboardingCompletedUseCase(repository),
            saveOnboardingCompleted = SaveOnboardingCompletedUseCase(repository)
        )
    }
}
