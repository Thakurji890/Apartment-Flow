package com.example.feature.roommate.data.local

import com.example.feature.roommate.data.local.dao.RoommateDao
import com.example.feature.roommate.data.local.entity.RoommateEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoommateLocalDataSource @Inject constructor(
    private val roommateDao: RoommateDao
) {
    fun getAllRoommates(): Flow<List<RoommateEntity>> {
        return roommateDao.getAllRoommates()
    }

    fun getRoommatesByApartment(apartmentId: String): Flow<List<RoommateEntity>> {
        return roommateDao.getRoommatesByApartment(apartmentId)
    }

    suspend fun getRoommateById(id: String): RoommateEntity? {
        return roommateDao.getRoommateById(id)
    }

    fun observeRoommateById(id: String): Flow<RoommateEntity?> {
        return roommateDao.observeRoommateById(id)
    }

    suspend fun insertRoommate(roommate: RoommateEntity) {
        roommateDao.insertRoommate(roommate)
    }

    suspend fun insertRoommates(roommates: List<RoommateEntity>) {
        roommateDao.insertRoommates(roommates)
    }

    suspend fun updateRoommate(roommate: RoommateEntity) {
        roommateDao.updateRoommate(roommate)
    }

    suspend fun updateBalanceStatus(id: String, balanceStatus: String, balanceAmount: Double) {
        roommateDao.updateBalanceStatus(id, balanceStatus, balanceAmount)
    }

    suspend fun deleteRoommateById(id: String) {
        roommateDao.deleteRoommateById(id)
    }

    suspend fun clearAll() {
        roommateDao.clearAllRoommates()
    }
}
