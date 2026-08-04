package com.example.feature.expensesplit.domain.repository

import com.example.core.util.Resource
import com.example.feature.expensesplit.domain.model.Debt
import com.example.feature.expensesplit.domain.model.MemberBalance
import com.example.feature.expensesplit.domain.model.Split
import kotlinx.coroutines.flow.Flow

interface ExpenseSplitRepository {
    
    fun getSplitsForExpense(expenseId: String): Flow<Resource<List<Split>>>
    
    fun getMemberBalances(apartmentId: String): Flow<Resource<List<MemberBalance>>>
    
    fun getDebtsForApartment(apartmentId: String): Flow<Resource<List<Debt>>>
    
    suspend fun saveExpenseWithSplits(
        expenseId: String, 
        apartmentId: String, 
        splits: List<Split>, 
        debts: List<Debt>
    ): Resource<Unit>
    
    suspend fun deleteExpenseSplits(expenseId: String, apartmentId: String): Resource<Unit>
    
    suspend fun syncBalances(apartmentId: String): Resource<Unit>
}
