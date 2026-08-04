package com.example.feature.apartment.domain.model

data class InviteCode(
    val code: String = "",
    val apartmentId: String = "",
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L, // 7 days
    val isActive: Boolean = true
)
