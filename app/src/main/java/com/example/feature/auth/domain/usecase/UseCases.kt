package com.example.feature.auth.domain.usecase

import com.example.core.util.Resource
import com.example.feature.auth.domain.model.User
import com.example.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SignInUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Resource<User> {
        return repository.signIn(email, password)
    }
}

class SignUpUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String, fullName: String): Resource<User> {
        return repository.signUp(email, password, fullName)
    }
}

class SignOutUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(): Resource<Unit> {
        return repository.signOut()
    }
}

class ResetPasswordUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String): Resource<Unit> {
        return repository.resetPassword(email)
    }
}

class SendVerificationEmailUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(): Resource<Unit> {
        return repository.sendVerificationEmail()
    }
}

class CheckVerificationUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(): Resource<Boolean> {
        return repository.checkVerificationStatus()
    }
}

class GetCurrentUserUseCase @Inject constructor(private val repository: AuthRepository) {
    operator fun invoke(): User? {
        return repository.getCurrentUser()
    }
}

class GetOnboardingCompletedUseCase @Inject constructor(private val repository: AuthRepository) {
    operator fun invoke(): Flow<Boolean> {
        return repository.getOnboardingCompleted()
    }
}

class SaveOnboardingCompletedUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(completed: Boolean) {
        repository.saveOnboardingCompleted(completed)
    }
}
