package com.example.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.core.util.FirebaseInitializer
import com.example.feature.auth.data.repository.AuthRepositoryImpl
import com.example.feature.auth.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(@ApplicationContext context: Context): FirebaseAuth {
        FirebaseInitializer.ensureInitialized(context)
        return FirebaseAuth.getInstance()
    }


    @Provides
    @Singleton
    fun provideFirebaseStorage(@ApplicationContext context: Context): FirebaseStorage {
        FirebaseInitializer.ensureInitialized(context)
        return FirebaseStorage.getInstance()
    }


    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth,
        dataStore: DataStore<Preferences>
    ): AuthRepository {
        return AuthRepositoryImpl(auth, dataStore)
    }

    @Provides
    @Singleton
    fun provideApartmentDataManager(@ApplicationContext context: Context): com.example.feature.apartmentmanager.data.ApartmentDataManager {
        return com.example.feature.apartmentmanager.data.ApartmentDataManager(context)
    }
}
