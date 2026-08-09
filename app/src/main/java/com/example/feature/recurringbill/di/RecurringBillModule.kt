package com.example.feature.recurringbill.di

import com.example.feature.expensesplit.domain.repository.ExpenseSplitRepository
import com.example.feature.recurringbill.data.repository.RecurringBillRepositoryImpl
import com.example.feature.recurringbill.domain.repository.RecurringBillRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RecurringBillModule {

    @Provides
    @Singleton
    fun provideRecurringBillRepository(
        firestore: FirebaseFirestore,
        expenseSplitRepository: ExpenseSplitRepository
    ): RecurringBillRepository {
        return RecurringBillRepositoryImpl(firestore, expenseSplitRepository)
    }
}
