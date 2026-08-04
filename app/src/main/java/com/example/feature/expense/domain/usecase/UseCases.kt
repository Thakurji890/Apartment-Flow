package com.example.feature.expense.domain.usecase

import com.example.feature.expense.domain.repository.ExpenseRepository
import javax.inject.Inject

class GetExpensesUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    operator fun invoke(apartmentId: String, searchQuery: String, filter: com.example.feature.expense.domain.model.ExpenseFilter, sortOrder: com.example.feature.expense.domain.model.ExpenseSortOrder) = 
        repository.getExpenses(apartmentId, searchQuery, filter, sortOrder)
}

class GetExpenseUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    operator fun invoke(expenseId: String) = repository.getExpense(expenseId)
}

class InsertExpenseUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    suspend operator fun invoke(expense: com.example.feature.expense.domain.model.Expense) = repository.insertExpense(expense)
}

class UpdateExpenseUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    suspend operator fun invoke(expense: com.example.feature.expense.domain.model.Expense) = repository.updateExpense(expense)
}

class DeleteExpenseUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    suspend operator fun invoke(expenseId: String) = repository.deleteExpense(expenseId)
}

class SyncExpensesUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    suspend operator fun invoke(apartmentId: String) = repository.syncExpenses(apartmentId)
}
