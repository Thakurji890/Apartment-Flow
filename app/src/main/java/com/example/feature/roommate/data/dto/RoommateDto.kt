package com.example.feature.roommate.data.dto

import com.google.firebase.firestore.DocumentId

data class RoommateDto(
    @DocumentId
    val id: String = "",
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
    val moveInDate: Long = 0L,
    val isPetFriendly: Boolean = true,
    val hobbies: List<String> = emptyList(),
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)
