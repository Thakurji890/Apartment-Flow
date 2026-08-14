package com.example.feature.notification.data.dto

import com.google.firebase.firestore.DocumentId

data class FcmTokenDto(
    @DocumentId
    val token: String = "",
    val deviceId: String = "",
    val deviceModel: String = "",
    val updatedAt: Long = 0L
)
