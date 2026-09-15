package com.example.feature.roommate.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.feature.roommate.domain.model.Roommate

@Entity(tableName = "roommates")
data class RoommateEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val email: String,
    val balanceStatus: String = "Settled",
    val balanceAmount: Double = 0.0,
    val apartmentId: String = "",
    val phoneNumber: String = "",
    val photoUrl: String? = null,
    val isAdmin: Boolean = false,
    val colorHex: Long = 0xFF006A6AL,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toRoommate(): Roommate {
        return Roommate(
            id = id,
            apartmentId = apartmentId,
            userId = id,
            displayName = name,
            email = email,
            phoneNumber = phoneNumber,
            photoUrl = photoUrl,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromRoommate(
            roommate: Roommate,
            balanceStatus: String = "Settled",
            balanceAmount: Double = 0.0,
            isAdmin: Boolean = false,
            colorHex: Long = 0xFF006A6AL
        ): RoommateEntity {
            return RoommateEntity(
                id = roommate.id.ifBlank { roommate.userId },
                name = roommate.displayName,
                email = roommate.email,
                balanceStatus = balanceStatus,
                balanceAmount = balanceAmount,
                apartmentId = roommate.apartmentId,
                phoneNumber = roommate.phoneNumber,
                photoUrl = roommate.photoUrl,
                isAdmin = isAdmin,
                colorHex = colorHex,
                createdAt = roommate.createdAt,
                updatedAt = roommate.updatedAt
            )
        }
    }
}
