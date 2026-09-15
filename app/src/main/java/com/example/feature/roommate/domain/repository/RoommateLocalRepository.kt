package com.example.feature.roommate.domain.repository

import com.example.feature.roommate.data.local.entity.RoommateEntity
import kotlinx.coroutines.flow.Flow

/**
 * Room database Repository interface for managing [RoommateEntity] data.
 * Provides abstracted methods for inserting, querying, updating, and deleting roommate records.
 */
interface RoommateLocalRepository {

    /**
     * Reactively queries all roommates ordered by name.
     */
    fun getAllRoommates(): Flow<List<RoommateEntity>>

    /**
     * Reactively queries all roommates for a given apartment ID.
     */
    fun getRoommatesByApartment(apartmentId: String): Flow<List<RoommateEntity>>

    /**
     * Suspended query to retrieve a single roommate by ID.
     */
    suspend fun getRoommateById(id: String): RoommateEntity?

    /**
     * Reactively observes a single roommate record by ID.
     */
    fun observeRoommateById(id: String): Flow<RoommateEntity?>

    /**
     * Suspended query to get total count of roommates.
     */
    suspend fun getRoommatesCount(): Int

    /**
     * Inserts a single roommate entity into the Room database.
     */
    suspend fun insertRoommate(roommate: RoommateEntity)

    /**
     * Inserts a batch list of roommate entities into the Room database.
     */
    suspend fun insertRoommates(roommates: List<RoommateEntity>)

    /**
     * Updates an existing roommate record.
     */
    suspend fun updateRoommate(roommate: RoommateEntity)

    /**
     * Updates the calculated balance status string and numerical net balance amount.
     */
    suspend fun updateBalanceStatus(id: String, balanceStatus: String, balanceAmount: Double)

    /**
     * Deletes a roommate by entity reference.
     */
    suspend fun deleteRoommate(roommate: RoommateEntity)

    /**
     * Deletes a roommate by ID.
     */
    suspend fun deleteRoommateById(id: String)

    /**
     * Clears all roommate records from the table.
     */
    suspend fun clearAllRoommates()
}
