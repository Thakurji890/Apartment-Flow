package com.example.feature.roommate.data.mapper

import com.example.feature.roommate.data.dto.RoommateDto
import com.example.feature.roommate.domain.model.Roommate

fun RoommateDto.toRoommate(): Roommate {
    return Roommate(
        id = id,
        apartmentId = apartmentId,
        userId = userId,
        displayName = displayName,
        email = email,
        phoneNumber = phoneNumber,
        bio = bio,
        photoUrl = photoUrl,
        emergencyContactName = emergencyContactName,
        emergencyContactPhone = emergencyContactPhone,
        occupation = occupation,
        moveInDate = moveInDate,
        isPetFriendly = isPetFriendly,
        hobbies = hobbies,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Roommate.toDto(): RoommateDto {
    return RoommateDto(
        id = id,
        apartmentId = apartmentId,
        userId = userId,
        displayName = displayName,
        email = email,
        phoneNumber = phoneNumber,
        bio = bio,
        photoUrl = photoUrl,
        emergencyContactName = emergencyContactName,
        emergencyContactPhone = emergencyContactPhone,
        occupation = occupation,
        moveInDate = moveInDate,
        isPetFriendly = isPetFriendly,
        hobbies = hobbies,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
