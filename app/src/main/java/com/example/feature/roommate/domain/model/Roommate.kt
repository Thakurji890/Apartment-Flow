package com.example.feature.roommate.domain.model

data class Roommate(
    val id: String = "", // Document ID (usually same as userId)
    val apartmentId: String = "",
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val bio: String = "",
    val photoUrl: String? = null,
    val emergencyContactName: String = "",
    val emergencyContactPhone: String = "",
    val occupation: String = "",
    val moveInDate: Long = System.currentTimeMillis(),
    val isPetFriendly: Boolean = true,
    val hobbies: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
