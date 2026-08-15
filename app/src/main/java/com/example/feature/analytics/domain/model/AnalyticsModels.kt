package com.example.feature.analytics.domain.model

data class AnalyticsSummary(
    val totalSpending: Double = 0.0,
    val averageExpense: Double = 0.0,
    val highestExpense: Double = 0.0,
    val topCategory: String = "None",
    val expenseCount: Int = 0,
    val totalSettled: Double = 0.0,
    val outstandingBalance: Double = 0.0, // Across all members (sum of positive balances)
    val totalRecurringCost: Double = 0.0
)

data class CategorySpending(
    val categoryId: String,
    val amount: Double,
    val percentage: Float,
    val count: Int
)

data class MemberSpending(
    val userId: String,
    val displayName: String,
    val amountPaid: Double,
    val actualShare: Double,
    val netBalance: Double,
    val expenseCount: Int
)

data class PersonalSpending(
    val totalShare: Double,
    val amountPaid: Double,
    val debtOutstanding: Double,
    val owedToMe: Double,
    val expenseCount: Int,
    val averagePersonalExpense: Double
)

data class TrendPoint(
    val label: String,
    val timestamp: Long,
    val amount: Double
)

data class MonthlyComparison(
    val currentPeriodAmount: Double,
    val previousPeriodAmount: Double,
    val percentageChange: Double, // e.g. 8.9 for +8.9%
    val isIncrease: Boolean
)

enum class AnalyticsPeriod(val displayName: String) {
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month"),
    LAST_MONTH("Last Month"),
    LAST_3_MONTHS("Last 3 Months"),
    LAST_6_MONTHS("Last 6 Months"),
    THIS_YEAR("This Year"),
    CUSTOM("Custom Range")
}
