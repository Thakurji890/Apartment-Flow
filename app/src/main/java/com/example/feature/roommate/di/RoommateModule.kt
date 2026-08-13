package com.example.feature.roommate.di

import com.example.feature.roommate.data.repository.RoommateRepositoryImpl
import com.example.feature.roommate.domain.repository.RoommateRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RoommateModule {

    @Provides
    @Singleton
    fun provideRoommateRepository(firestore: FirebaseFirestore): RoommateRepository {
        return RoommateRepositoryImpl(firestore)
    }
}
