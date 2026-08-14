package com.example.feature.notification.domain.repository

import com.example.core.util.Resource
import com.example.feature.notification.domain.model.AppNotification
import com.example.feature.notification.domain.model.FcmToken
import com.example.feature.notification.domain.model.NotificationPreferences
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    // Notifications
    fun getNotifications(userId: String): Flow<Resource<List<AppNotification>>>
    fun getUnreadCount(userId: String): Flow<Int>
    suspend fun markAsRead(userId: String, notificationId: String): Resource<Unit>
    suspend fun markAllAsRead(userId: String): Resource<Unit>
    suspend fun deleteNotification(userId: String, notificationId: String): Resource<Unit>
    suspend fun cleanupExpiredNotifications(userId: String): Resource<Unit>

    // Preferences
    suspend fun getPreferences(userId: String): Resource<NotificationPreferences>
    suspend fun updatePreferences(userId: String, preferences: NotificationPreferences): Resource<Unit>

    // FCM Tokens
    suspend fun registerFcmToken(userId: String, token: FcmToken): Resource<Unit>
    suspend fun unregisterFcmToken(userId: String, tokenString: String): Resource<Unit>
}
