package com.example.feature.roommate.data.local.dao

import androidx.room.*
import com.example.feature.roommate.data.local.entity.RoommateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoommateDao {

    @Query("SELECT * FROM roommates ORDER BY name ASC")
    fun getAllRoommates(): Flow<List<RoommateEntity>>

    @Query("SELECT * FROM roommates WHERE apartmentId = :apartmentId ORDER BY name ASC")
    fun getRoommatesByApartment(apartmentId: String): Flow<List<RoommateEntity>>

    @Query("SELECT * FROM roommates WHERE id = :id")
    suspend fun getRoommateById(id: String): RoommateEntity?

    @Query("SELECT * FROM roommates WHERE id = :id")
    fun observeRoommateById(id: String): Flow<RoommateEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoommate(roommate: RoommateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoommates(roommates: List<RoommateEntity>)

    @Update
    suspend fun updateRoommate(roommate: RoommateEntity)

    @Query("UPDATE roommates SET balanceStatus = :balanceStatus, balanceAmount = :balanceAmount WHERE id = :id")
    suspend fun updateBalanceStatus(id: String, balanceStatus: String, balanceAmount: Double)

    @Query("DELETE FROM roommates WHERE id = :id")
    suspend fun deleteRoommateById(id: String)

    @Query("DELETE FROM roommates")
    suspend fun clearAllRoommates()
}
