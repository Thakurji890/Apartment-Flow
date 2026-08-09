package com.example.feature.recurringbill.domain.model

import com.example.feature.expensesplit.domain.model.Split

enum class BillFrequency {
    WEEKLY,
    BIWEEKLY,
    MONTHLY,
    EVERY_2_MONTHS,
    QUARTERLY,
    HALF_YEARLY,
    YEARLY
}

enum class BillType {
    FIXED,
    VARIABLE
}

enum class BillStatus {
    ACTIVE,
    PAUSED,
    COMPLETED
}

data class RecurringBill(
    val id: String = "",
    val apartmentId: String = "",
    val name: String = "",
    val description: String = "",
    val categoryId: String = "others",
    val expectedAmount: Double = 0.0,
    val currency: String = "USD",
    val type: BillType = BillType.FIXED,
    val frequency: BillFrequency = BillFrequency.MONTHLY,
    val paidBy: String = "",
    val splits: List<Split> = emptyList(),
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long? = null,
    val nextDueDate: Long = System.currentTimeMillis(),
    val lastGeneratedDate: Long? = null,
    val status: BillStatus = BillStatus.ACTIVE,
    val autoGenerate: Boolean = true,
    val reminders: List<Int> = listOf(1), // Days before due date
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val createdBy: String = ""
)
