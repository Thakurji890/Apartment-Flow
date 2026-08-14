package com.example.feature.notification.data.dto

import com.google.firebase.firestore.DocumentId

data class NotificationDto(
    @DocumentId
    val id: String = "",
    val apartmentId: String = "",
    val type: String = "GENERAL",
    val title: String = "",
    val body: String = "",
    val createdAt: Long = 0L,
    val isRead: Boolean = false,
    val readAt: Long? = null,
    val priority: String = "NORMAL",
    val deepLink: String? = null,
    val relatedEntityType: String? = null,
    val relatedEntityId: String? = null,
    val expiresAt: Long? = null
)
