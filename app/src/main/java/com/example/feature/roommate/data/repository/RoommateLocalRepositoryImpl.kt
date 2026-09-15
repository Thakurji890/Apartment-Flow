package com.example.feature.roommate.data.repository

import com.example.feature.roommate.data.local.dao.RoommateDao
import com.example.feature.roommate.data.local.entity.RoommateEntity
import com.example.feature.roommate.domain.repository.RoommateLocalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [RoommateLocalRepository] that interacts with Room database through [RoommateDao].
 */
@Singleton
class RoommateLocalRepositoryImpl @Inject constructor(
    private val roommateDao: RoommateDao
) : RoommateLocalRepository {

    override fun getAllRoommates(): Flow<List<RoommateEntity>> {
        return roommateDao.getAllRoommates()
    }

    override fun getRoommatesByApartment(apartmentId: String): Flow<List<RoommateEntity>> {
        return roommateDao.getRoommatesByApartment(apartmentId)
    }

    override suspend fun getRoommateById(id: String): RoommateEntity? {
        return roommateDao.getRoommateById(id)
    }

    override fun observeRoommateById(id: String): Flow<RoommateEntity?> {
        return roommateDao.observeRoommateById(id)
    }

    override suspend fun getRoommatesCount(): Int {
        return roommateDao.getRoommatesCount()
    }

    override suspend fun insertRoommate(roommate: RoommateEntity) {
        roommateDao.insertRoommate(roommate)
    }

    override suspend fun insertRoommates(roommates: List<RoommateEntity>) {
        roommateDao.insertRoommates(roommates)
    }

    override suspend fun updateRoommate(roommate: RoommateEntity) {
        roommateDao.updateRoommate(roommate)
    }

    override suspend fun updateBalanceStatus(id: String, balanceStatus: String, balanceAmount: Double) {
        roommateDao.updateBalanceStatus(id, balanceStatus, balanceAmount)
    }

    override suspend fun deleteRoommate(roommate: RoommateEntity) {
        roommateDao.deleteRoommate(roommate)
    }

    override suspend fun deleteRoommateById(id: String) {
        roommateDao.deleteRoommateById(id)
    }

    override suspend fun clearAllRoommates() {
        roommateDao.clearAllRoommates()
    }
}
