package com.example.feature.notification.data.dto

data class NotificationPreferencesDto(
    val expensesEnabled: Boolean = true,
    val settlementsEnabled: Boolean = true,
    val recurringBillsEnabled: Boolean = true,
    val shoppingEnabled: Boolean = true,
    val choresEnabled: Boolean = true,
    val apartmentActivityEnabled: Boolean = true,
    val quietHoursEnabled: Boolean = false,
    val quietHoursStartHour: Int = 22,
    val quietHoursEndHour: Int = 7
)
