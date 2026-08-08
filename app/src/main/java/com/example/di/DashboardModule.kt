package com.example.di

import com.example.feature.dashboard.data.repository.DashboardRepositoryImpl
import com.example.feature.dashboard.domain.repository.DashboardRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DashboardModule {

    @Provides
    @Singleton
    fun provideDashboardRepository(firestore: FirebaseFirestore): DashboardRepository {
        return DashboardRepositoryImpl(firestore)
    }
}
