package com.example.feature.analytics.domain.usecase

import com.example.core.util.Resource
import com.example.feature.analytics.domain.model.*
import com.example.feature.apartment.domain.model.ApartmentMember
import com.example.feature.apartment.domain.repository.ApartmentRepository
import com.example.feature.dashboard.domain.repository.DashboardRepository
import com.example.feature.expense.domain.model.Expense
import com.example.feature.expensesplit.domain.engine.CalculationEngine
import com.example.feature.expensesplit.domain.model.Split
import com.example.feature.expensesplit.domain.model.SplitType
import com.example.feature.recurringbill.domain.repository.RecurringBillRepository
import com.example.feature.settlement.domain.model.Settlement
import com.example.feature.settlement.domain.model.SettlementStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AnalyticsUseCases @Inject constructor(
    private val dashboardRepository: DashboardRepository,
    private val apartmentRepository: ApartmentRepository,
    private val recurringBillRepository: RecurringBillRepository
) {
    fun getAnalyticsSummary(
        apartmentId: String,
        startTimestamp: Long,
        endTimestamp: Long
    ): Flow<Resource<AnalyticsSummary>> {
        return combine(
            dashboardRepository.getExpensesForMonth(apartmentId, startTimestamp, endTimestamp),
            dashboardRepository.getSettlementsForMonth(apartmentId, startTimestamp, endTimestamp),
            recurringBillRepository.getRecurringBills(apartmentId)
        ) { expRes, setRes, billRes ->
            if (expRes is Resource.Error) return@combine Resource.Error(expRes.message ?: "Failed to load expenses")
            
            val expenses = (expRes as? Resource.Success)?.data ?: emptyList()
            val settlements = (setRes as? Resource.Success)?.data ?: emptyList()
            val bills = (billRes as? Resource.Success)?.data ?: emptyList()
            
            val totalSpending = expenses.sumOf { it.amount }
            val avg = if (expenses.isNotEmpty()) totalSpending / expenses.size else 0.0
            val highest = expenses.maxOfOrNull { it.amount } ?: 0.0
            val topCategory = expenses.groupBy { it.categoryId }.maxByOrNull { it.value.sumOf { exp -> exp.amount } }?.key ?: "None"
            
            val totalSettled = settlements.filter { it.status == SettlementStatus.CONFIRMED }.sumOf { it.amount }
            
            val totalRecurring = bills.sumOf { it.expectedAmount }
            
            Resource.Success(
                AnalyticsSummary(
                    totalSpending = totalSpending,
                    averageExpense = avg,
                    highestExpense = highest,
                    topCategory = topCategory,
                    expenseCount = expenses.size,
                    totalSettled = totalSettled,
                    outstandingBalance = 0.0, // This typically needs complex calculation or just left out
                    totalRecurringCost = totalRecurring
                )
            )
        }
    }

    fun getCategorySpending(
        apartmentId: String,
        startTimestamp: Long,
        endTimestamp: Long
    ): Flow<Resource<List<CategorySpending>>> {
        return dashboardRepository.getExpensesForMonth(apartmentId, startTimestamp, endTimestamp).map { res ->
            if (res is Resource.Error) return@map Resource.Error(res.message ?: "Error")
            val expenses = (res as? Resource.Success)?.data ?: emptyList()
            val total = expenses.sumOf { it.amount }
            if (total == 0.0) return@map Resource.Success(emptyList())
            
            val grouped = expenses.groupBy { it.categoryId }
            val categoryList = grouped.map { (catId, exps) ->
                val amount = exps.sumOf { it.amount }
                CategorySpending(
                    categoryId = catId,
                    amount = amount,
                    percentage = ((amount / total) * 100).toFloat(),
                    count = exps.size
                )
            }.sortedByDescending { it.amount }
            
            Resource.Success(categoryList)
        }
    }

    fun getMemberSpending(
        apartmentId: String,
        startTimestamp: Long,
        endTimestamp: Long
    ): Flow<Resource<List<MemberSpending>>> {
        return combine(
            dashboardRepository.getExpensesForMonth(apartmentId, startTimestamp, endTimestamp),
            apartmentRepository.getApartmentMembers(apartmentId)
        ) { expRes, memRes ->
            if (expRes is Resource.Error) return@combine Resource.Error(expRes.message ?: "Error")
            if (memRes is Resource.Error) return@combine Resource.Error(memRes.message ?: "Error")
            
            val expenses = (expRes as? Resource.Success)?.data ?: emptyList()
            val members = (memRes as? Resource.Success)?.data ?: emptyList()
            
            val activeMembers = members.filter { it.status == "ACTIVE" }
            if (activeMembers.isEmpty()) return@combine Resource.Success(emptyList())

            val memberMap = members.associateBy { it.userId }
            val paidMap = expenses.groupBy { it.paidBy }.mapValues { it.value.sumOf { e -> e.amount } }
            
            // Calculate exact shares assuming EQUAL split for simplicity 
            // since actual splits aren't persisted in standard expense addition.
            val sharesMap = mutableMapOf<String, Double>()
            expenses.forEach { exp ->
                val splits = CalculationEngine.calculateSplits(
                    totalAmount = exp.amount,
                    splits = activeMembers.map { Split(userId = it.userId) },
                    globalSplitType = SplitType.EQUAL
                )
                splits.forEach { split ->
                    sharesMap[split.userId] = (sharesMap[split.userId] ?: 0.0) + split.amount
                }
            }

            val result = activeMembers.map { member ->
                val paid = paidMap[member.userId] ?: 0.0
                val share = sharesMap[member.userId] ?: 0.0
                MemberSpending(
                    userId = member.userId,
                    displayName = member.displayName,
                    amountPaid = paid,
                    actualShare = share,
                    netBalance = paid - share,
                    expenseCount = expenses.count { it.paidBy == member.userId }
                )
            }.sortedByDescending { it.amountPaid }

            Resource.Success(result)
        }
    }

    fun getTrends(
        apartmentId: String,
        startTimestamp: Long,
        endTimestamp: Long
    ): Flow<Resource<List<TrendPoint>>> {
        return dashboardRepository.getExpensesForMonth(apartmentId, startTimestamp, endTimestamp).map { res ->
            if (res is Resource.Error) return@map Resource.Error(res.message ?: "Error")
            val expenses = (res as? Resource.Success)?.data ?: emptyList()
            
            // Simple daily aggregation
            val format = java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault())
            val calendar = java.util.Calendar.getInstance()
            
            val grouped = expenses.groupBy { exp ->
                calendar.timeInMillis = exp.expenseDate
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                calendar.set(java.util.Calendar.MINUTE, 0)
                calendar.set(java.util.Calendar.SECOND, 0)
                calendar.set(java.util.Calendar.MILLISECOND, 0)
                calendar.timeInMillis
            }

            val points = grouped.map { (timestamp, exps) ->
                TrendPoint(
                    label = format.format(java.util.Date(timestamp)),
                    timestamp = timestamp,
                    amount = exps.sumOf { it.amount }
                )
            }.sortedBy { it.timestamp }
            
            Resource.Success(points)
        }
    }
}
