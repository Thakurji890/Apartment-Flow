package com.example.feature.expense.domain.usecase

import com.example.core.util.Resource
import com.example.feature.expense.domain.repository.ExpenseRepository
import javax.inject.Inject

class RejectExpenseUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    suspend operator fun invoke(expenseId: String, adminId: String, reason: String = ""): Resource<Unit> {
        return repository.rejectExpense(expenseId, adminId, reason)
    }
}
