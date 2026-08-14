package com.example.feature.notification.data.mapper

import com.example.feature.notification.data.dto.FcmTokenDto
import com.example.feature.notification.data.dto.NotificationDto
import com.example.feature.notification.data.dto.NotificationPreferencesDto
import com.example.feature.notification.domain.model.AppNotification
import com.example.feature.notification.domain.model.FcmToken
import com.example.feature.notification.domain.model.NotificationPreferences
import com.example.feature.notification.domain.model.NotificationPriority
import com.example.feature.notification.domain.model.NotificationType

fun NotificationDto.toDomain(): AppNotification {
    return AppNotification(
        id = id,
        apartmentId = apartmentId,
        type = runCatching { NotificationType.valueOf(type) }.getOrDefault(NotificationType.GENERAL),
        title = title,
        body = body,
        createdAt = createdAt,
        isRead = isRead,
        readAt = readAt,
        priority = runCatching { NotificationPriority.valueOf(priority) }.getOrDefault(NotificationPriority.NORMAL),
        deepLink = deepLink,
        relatedEntityType = relatedEntityType,
        relatedEntityId = relatedEntityId,
        expiresAt = expiresAt
    )
}

fun NotificationPreferencesDto.toDomain(): NotificationPreferences {
    return NotificationPreferences(
        expensesEnabled = expensesEnabled,
        settlementsEnabled = settlementsEnabled,
        recurringBillsEnabled = recurringBillsEnabled,
        shoppingEnabled = shoppingEnabled,
        choresEnabled = choresEnabled,
        apartmentActivityEnabled = apartmentActivityEnabled,
        quietHoursEnabled = quietHoursEnabled,
        quietHoursStartHour = quietHoursStartHour,
        quietHoursEndHour = quietHoursEndHour
    )
}

fun NotificationPreferences.toDto(): NotificationPreferencesDto {
    return NotificationPreferencesDto(
        expensesEnabled = expensesEnabled,
        settlementsEnabled = settlementsEnabled,
        recurringBillsEnabled = recurringBillsEnabled,
        shoppingEnabled = shoppingEnabled,
        choresEnabled = choresEnabled,
        apartmentActivityEnabled = apartmentActivityEnabled,
        quietHoursEnabled = quietHoursEnabled,
        quietHoursStartHour = quietHoursStartHour,
        quietHoursEndHour = quietHoursEndHour
    )
}

fun FcmTokenDto.toDomain(): FcmToken {
    return FcmToken(
        token = token,
        deviceId = deviceId,
        deviceModel = deviceModel,
        updatedAt = updatedAt
    )
}

fun FcmToken.toDto(): FcmTokenDto {
    return FcmTokenDto(
        token = token,
        deviceId = deviceId,
        deviceModel = deviceModel,
        updatedAt = updatedAt
    )
}
