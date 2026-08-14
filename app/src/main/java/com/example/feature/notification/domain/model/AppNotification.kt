package com.example.feature.notification.domain.model

enum class NotificationType {
    EXPENSE,
    SETTLEMENT,
    RECURRING_BILL,
    SHOPPING,
    CHORE,
    APARTMENT,
    ACCOUNT,
    GENERAL
}

enum class NotificationPriority {
    LOW,
    NORMAL,
    HIGH,
    CRITICAL
}

data class AppNotification(
    val id: String = "",
    val apartmentId: String = "",
    val type: NotificationType = NotificationType.GENERAL,
    val title: String = "",
    val body: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val readAt: Long? = null,
    val priority: NotificationPriority = NotificationPriority.NORMAL,
    val deepLink: String? = null,
    val relatedEntityType: String? = null,
    val relatedEntityId: String? = null,
    val expiresAt: Long? = null
)
