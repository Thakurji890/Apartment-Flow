package com.example.feature.expensesplit.domain.model

data class Split(
    val userId: String,
    val amount: Double = 0.0,
    val value: Double = 0.0
)

enum class SplitType {
    EQUAL,
    EXACT,
    PERCENTAGE,
    SHARES
}

data class Payment(
    val userId: String,
    val amount: Double
)

data class Debt(
    val expenseId: String,
    val apartmentId: String,
    val debtorId: String,
    val creditorId: String,
    val amount: Double
)
