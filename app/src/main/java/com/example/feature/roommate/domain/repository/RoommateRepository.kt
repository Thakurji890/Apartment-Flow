package com.example.feature.roommate.domain.repository

import com.example.core.util.Resource
import com.example.feature.roommate.domain.model.Roommate
import kotlinx.coroutines.flow.Flow

interface RoommateRepository {
    suspend fun getRoommateProfile(apartmentId: String, userId: String): Resource<Roommate?>
    fun getRoommatesProfiles(apartmentId: String): Flow<Resource<List<Roommate>>>
    suspend fun saveRoommateProfile(roommate: Roommate): Resource<Unit>
}
