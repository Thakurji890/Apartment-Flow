package com.example.feature.expense.domain.model

data class Expense(
    val expenseId: String = "",
    val apartmentId: String = "",
    val title: String = "",
    val description: String = "",
    val amount: Double = 0.0,
    val currency: String = "USD",
    val categoryId: String = "others",
    val paidBy: String = "",
    val createdBy: String = "",
    val expenseDate: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val receiptUrl: String? = null,
    val notes: String = "",
    val tags: List<String> = emptyList(),
    val isRecurring: Boolean = false,
    val status: String = "ACTIVE",
    val deleted: Boolean = false,
    // Transient field to resolve UI logic easier
    val isSynced: Boolean = true
)
