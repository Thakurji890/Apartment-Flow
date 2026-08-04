package com.example.feature.expense.domain.model

enum class ExpenseSortOrder {
    NEWEST_FIRST,
    OLDEST_FIRST,
    HIGHEST_AMOUNT,
    LOWEST_AMOUNT,
    ALPHABETICAL
}

data class ExpenseFilter(
    val categoryId: String? = null,
    val startDate: Long? = null,
    val endDate: Long? = null,
    val minAmount: Double? = null,
    val maxAmount: Double? = null,
    val paidByUserId: String? = null
)
