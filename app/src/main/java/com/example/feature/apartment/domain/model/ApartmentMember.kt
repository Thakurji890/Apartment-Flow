package com.example.feature.apartment.domain.model

enum class Role {
    OWNER,
    ADMIN,
    MEMBER
}

data class ApartmentMember(
    val id: String = "", // Document ID, usually same as userId
    val apartmentId: String = "",
    val userId: String = "",
    val displayName: String = "",
    val photoUrl: String? = null,
    val role: Role = Role.MEMBER,
    val joinedAt: Long = System.currentTimeMillis(),
    val status: String = "ACTIVE"
)
