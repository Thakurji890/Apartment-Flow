package com.example.feature.chore.domain.repository

import com.example.feature.chore.domain.model.Chore
import kotlinx.coroutines.flow.Flow

interface ChoreRepository {
    fun getChores(apartmentId: String): Flow<List<Chore>>
    suspend fun getChoreById(apartmentId: String, choreId: String): Chore?
    suspend fun saveChore(chore: Chore)
    suspend fun deleteChore(apartmentId: String, choreId: String)
}
