package com.example.feature.dashboard.domain.model

enum class ActivityType {
    EXPENSE_ADDED,
    EXPENSE_UPDATED,
    SETTLEMENT_CREATED,
    SETTLEMENT_CONFIRMED,
    MEMBER_JOINED,
    UNKNOWN
}

data class ActivityItem(
    val id: String,
    val type: ActivityType,
    val userId: String,
    val title: String,
    val amount: Double? = null,
    val timestamp: Long
)
