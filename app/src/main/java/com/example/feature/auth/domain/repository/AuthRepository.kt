package com.example.feature.auth.domain.repository

import com.example.core.util.Resource
import com.example.feature.auth.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun getCurrentUser(): User?
    fun getOnboardingCompleted(): Flow<Boolean>
    suspend fun saveOnboardingCompleted(completed: Boolean)
    suspend fun signIn(email: String, password: String): Resource<User>
    suspend fun signInWithGoogle(idToken: String): Resource<User>
    suspend fun signUp(email: String, password: String, fullName: String): Resource<User>
    suspend fun signOut(): Resource<Unit>
    suspend fun resetPassword(email: String): Resource<Unit>
    suspend fun sendVerificationEmail(): Resource<Unit>
    suspend fun checkVerificationStatus(): Resource<Boolean>
}
