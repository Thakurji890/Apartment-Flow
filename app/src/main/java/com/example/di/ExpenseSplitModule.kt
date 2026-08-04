package com.example.di

import com.example.feature.expensesplit.data.repository.ExpenseSplitRepositoryImpl
import com.example.feature.expensesplit.domain.repository.ExpenseSplitRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ExpenseSplitModule {

    @Provides
    @Singleton
    fun provideExpenseSplitRepository(
        firestore: FirebaseFirestore
    ): ExpenseSplitRepository {
        return ExpenseSplitRepositoryImpl(firestore)
    }
}
