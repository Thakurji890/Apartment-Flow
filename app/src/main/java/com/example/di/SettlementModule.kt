package com.example.di

import com.example.feature.settlement.data.repository.SettlementRepositoryImpl
import com.example.feature.settlement.domain.repository.SettlementRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SettlementModule {

    @Provides
    @Singleton
    fun provideSettlementRepository(
        firestore: FirebaseFirestore
    ): SettlementRepository {
        return SettlementRepositoryImpl(firestore)
    }
}
