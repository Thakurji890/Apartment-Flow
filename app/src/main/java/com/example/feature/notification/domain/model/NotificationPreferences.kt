package com.example.feature.notification.domain.model

data class NotificationPreferences(
    val expensesEnabled: Boolean = true,
    val settlementsEnabled: Boolean = true,
    val recurringBillsEnabled: Boolean = true,
    val shoppingEnabled: Boolean = true,
    val choresEnabled: Boolean = true,
    val apartmentActivityEnabled: Boolean = true,
    val quietHoursEnabled: Boolean = false,
    val quietHoursStartHour: Int = 22, // 10 PM
    val quietHoursEndHour: Int = 7     // 7 AM
)

data class FcmToken(
    val token: String = "",
    val deviceId: String = "",
    val deviceModel: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)
