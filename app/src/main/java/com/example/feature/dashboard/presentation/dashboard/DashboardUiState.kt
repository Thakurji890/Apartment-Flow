package com.example.feature.dashboard.presentation.dashboard

import com.example.feature.apartment.domain.model.Apartment
import com.example.feature.apartment.domain.model.ApartmentMember
import com.example.feature.auth.domain.model.User
import com.example.feature.dashboard.domain.model.ActivityItem
import com.example.feature.expense.domain.model.Expense
import com.example.feature.expensesplit.domain.model.Debt
import com.example.feature.expensesplit.domain.model.MemberBalance
import com.example.feature.settlement.domain.model.Settlement
import java.time.YearMonth

data class DashboardUiState(
    val isLoading: Boolean = true,
    val isOffline: Boolean = false, // Not perfectly tracked right now, but a placeholder
    val error: String? = null,
    val apartmentId: String = "",
    val apartmentName: String = "",
    val apartments: List<Apartment> = emptyList(),
    val currentUser: User? = null,
    val currentMember: ApartmentMember? = null,
    
    // Financial Summary
    val totalSpent: Double = 0.0,
    val amountYouOwe: Double = 0.0,
    val amountOwedToYou: Double = 0.0,
    val netBalance: Double = 0.0,
    
    // Monthly Summary
    val currentMonth: YearMonth = YearMonth.now(),
    val searchQuery: String = "",
    val searchResults: List<Any> = emptyList(), // For global search results if needed
    val currentMonthExpenses: Double = 0.0,
    val averageDailySpending: Double = 0.0,
    val highestExpense: Double = 0.0,
    val numberOfExpenses: Int = 0,
    val numberOfActiveMembers: Int = 0,
    
    // Recent Data
    val recentExpenses: List<Expense> = emptyList(),
    val outstandingDebts: List<Debt> = emptyList(),
    val members: List<ApartmentMember> = emptyList(),
    val memberBalances: List<MemberBalance> = emptyList(),
    
    // Settlement Summary
    val pendingSettlementsCount: Int = 0,
    val settledThisMonthCount: Int = 0,
    val amountPaidThisMonth: Double = 0.0,
    val amountReceivedThisMonth: Double = 0.0,
    
    // Activity
    val recentActivities: List<ActivityItem> = emptyList(),
    
    // Recurring Bills
    val recurringBills: List<com.example.feature.recurringbill.domain.model.RecurringBill> = emptyList()
)
