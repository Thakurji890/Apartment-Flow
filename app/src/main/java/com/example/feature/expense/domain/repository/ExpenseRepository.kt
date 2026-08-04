package com.example.feature.expense.domain.repository

import com.example.core.util.Resource
import com.example.feature.expense.domain.model.Expense
import com.example.feature.expense.domain.model.ExpenseFilter
import com.example.feature.expense.domain.model.ExpenseSortOrder
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    fun getExpenses(
        apartmentId: String,
        searchQuery: String = "",
        filter: ExpenseFilter = ExpenseFilter(),
        sortOrder: ExpenseSortOrder = ExpenseSortOrder.NEWEST_FIRST
    ): Flow<Resource<List<Expense>>>
    
    fun getExpense(expenseId: String): Flow<Resource<Expense>>
    
    suspend fun insertExpense(expense: Expense): Resource<Unit>
    
    suspend fun updateExpense(expense: Expense): Resource<Unit>
    
    suspend fun deleteExpense(expenseId: String): Resource<Unit>
    
    suspend fun syncExpenses(apartmentId: String): Resource<Unit>
}
