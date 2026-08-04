package com.example.feature.auth.domain.usecase

import com.example.core.util.Resource
import com.example.feature.auth.domain.model.User
import com.example.feature.auth.domain.repository.AuthRepository
import javax.inject.Inject

class SignInWithGoogleUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(idToken: String): Resource<User> {
        return repository.signInWithGoogle(idToken)
    }
}
