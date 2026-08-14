package com.example.feature.notification.di

import com.example.feature.notification.data.repository.NotificationRepositoryImpl
import com.example.feature.notification.domain.repository.NotificationRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NotificationModule {

    @Provides
    @Singleton
    fun provideNotificationRepository(firestore: FirebaseFirestore): NotificationRepository {
        return NotificationRepositoryImpl(firestore)
    }
}
