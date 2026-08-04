package com.example.feature.expensesplit.domain.model

data class Debt(
    val debtId: String = "",
    val apartmentId: String,
    val expenseId: String,
    val debtorId: String,
    val creditorId: String,
    val amount: Double,
    val status: String = "PENDING",
    val createdAt: Long = System.currentTimeMillis()
)

data class MemberBalance(
    val userId: String,
    val apartmentId: String,
    val totalOwed: Double = 0.0,
    val totalToReceive: Double = 0.0,
    val netBalance: Double = 0.0,
    val updatedAt: Long = System.currentTimeMillis()
)
