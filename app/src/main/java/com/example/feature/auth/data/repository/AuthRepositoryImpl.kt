package com.example.feature.auth.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.example.core.util.Resource
import com.example.feature.auth.domain.model.User
import com.example.feature.auth.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.concurrent.CancellationException
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val dataStore: DataStore<Preferences>
) : AuthRepository {

    private object PreferencesKeys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    override fun getCurrentUser(): User? {
        val fbUser = auth.currentUser ?: return null
        return User(
            uid = fbUser.uid,
            email = fbUser.email ?: "",
            displayName = fbUser.displayName,
            isEmailVerified = fbUser.isEmailVerified
        )
    }

    override fun getOnboardingCompleted(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: false
        }
    }

    override suspend fun saveOnboardingCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] = completed
        }
    }

    override suspend fun signIn(email: String, password: String): Resource<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("User is null")
            Resource.Success(
                User(
                    uid = user.uid,
                    email = user.email ?: "",
                    displayName = user.displayName,
                    isEmailVerified = user.isEmailVerified
                )
            )
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Resource.Error(e.localizedMessage ?: "An unexpected error occurred")
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Resource<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val user = result.user ?: throw Exception("User is null")
            Resource.Success(
                User(
                    uid = user.uid,
                    email = user.email ?: "",
                    displayName = user.displayName,
                    isEmailVerified = user.isEmailVerified
                )
            )
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Resource.Error(e.localizedMessage ?: "Failed to sign in with Google")
        }
    }

    override suspend fun signUp(email: String, password: String, fullName: String): Resource<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("User is null")
            
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(fullName)
                .build()
                
            user.updateProfile(profileUpdates).await()
            user.sendEmailVerification().await()
            
            Resource.Success(
                User(
                    uid = user.uid,
                    email = user.email ?: "",
                    displayName = fullName,
                    isEmailVerified = user.isEmailVerified
                )
            )
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Resource.Error(e.localizedMessage ?: "An unexpected error occurred")
        }
    }

    override suspend fun signOut(): Resource<Unit> {
        return try {
            auth.signOut()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to sign out")
        }
    }

    override suspend fun resetPassword(email: String): Resource<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Resource.Error(e.localizedMessage ?: "Failed to send reset email")
        }
    }

    override suspend fun sendVerificationEmail(): Resource<Unit> {
        return try {
            auth.currentUser?.sendEmailVerification()?.await()
                ?: throw Exception("User not logged in")
            Resource.Success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Resource.Error(e.localizedMessage ?: "Failed to send verification email")
        }
    }

    override suspend fun checkVerificationStatus(): Resource<Boolean> {
        return try {
            val user = auth.currentUser ?: throw Exception("User not logged in")
            user.reload().await()
            Resource.Success(user.isEmailVerified)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Resource.Error(e.localizedMessage ?: "Failed to check verification status")
        }
    }
}
