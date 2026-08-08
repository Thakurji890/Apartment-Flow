package com.example.feature.dashboard.domain.repository

import com.example.core.util.Resource
import com.example.feature.expense.domain.model.Expense
import com.example.feature.settlement.domain.model.Settlement
import kotlinx.coroutines.flow.Flow

interface DashboardRepository {
    fun getRecentExpenses(apartmentId: String, limit: Int): Flow<Resource<List<Expense>>>
    fun getExpensesForMonth(apartmentId: String, startTimestamp: Long, endTimestamp: Long): Flow<Resource<List<Expense>>>
    fun getSettlementsForMonth(apartmentId: String, startTimestamp: Long, endTimestamp: Long): Flow<Resource<List<Settlement>>>
    fun searchDashboard(apartmentId: String, query: String): Flow<Resource<DashboardSearchResult>>
}

data class DashboardSearchResult(
    val expenses: List<Expense> = emptyList(),
    val settlements: List<Settlement> = emptyList()
)
