package com.example.feature.expense.domain.usecase

import javax.inject.Inject

data class ExpenseUseCases @Inject constructor(
    val getExpenses: GetExpensesUseCase,
    val getExpense: GetExpenseUseCase,
    val insertExpense: InsertExpenseUseCase,
    val updateExpense: UpdateExpenseUseCase,
    val deleteExpense: DeleteExpenseUseCase,
    val syncExpenses: SyncExpensesUseCase
)
