package com.example.di

import com.example.feature.expense.data.local.dao.ExpenseDao
import com.example.feature.expense.data.repository.ExpenseRepositoryImpl
import com.example.feature.expense.domain.repository.ExpenseRepository
import com.example.feature.expense.domain.usecase.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ExpenseModule {

    @Provides
    @Singleton
    fun provideExpenseRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth,
        dao: ExpenseDao
    ): ExpenseRepository {
        return ExpenseRepositoryImpl(firestore, auth, dao)
    }

    @Provides
    @Singleton
    fun provideExpenseUseCases(repository: ExpenseRepository): ExpenseUseCases {
        return ExpenseUseCases(
            getExpenses = GetExpensesUseCase(repository),
            getExpense = GetExpenseUseCase(repository),
            insertExpense = InsertExpenseUseCase(repository),
            updateExpense = UpdateExpenseUseCase(repository),
            deleteExpense = DeleteExpenseUseCase(repository),
            approveExpense = ApproveExpenseUseCase(repository),
            rejectExpense = RejectExpenseUseCase(repository),
            syncExpenses = SyncExpensesUseCase(repository)
        )
    }
}
