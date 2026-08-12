package com.example.feature.chore.data.repository

import com.example.feature.chore.data.dto.ChoreDto
import com.example.feature.chore.data.mapper.toDomain
import com.example.feature.chore.data.mapper.toDto
import com.example.feature.chore.domain.model.Chore
import com.example.feature.chore.domain.repository.ChoreRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ChoreRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ChoreRepository {

    override fun getChores(apartmentId: String): Flow<List<Chore>> = callbackFlow {
        val subscription = firestore.collection("apartments")
            .document(apartmentId)
            .collection("chores")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val chores = snapshot.documents.mapNotNull {
                        it.toObject(ChoreDto::class.java)?.toDomain()
                    }
                    trySend(chores)
                }
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun getChoreById(apartmentId: String, choreId: String): Chore? {
        val snapshot = firestore.collection("apartments")
            .document(apartmentId)
            .collection("chores")
            .document(choreId)
            .get()
            .await()
        return snapshot.toObject(ChoreDto::class.java)?.toDomain()
    }

    override suspend fun saveChore(chore: Chore) {
        firestore.collection("apartments")
            .document(chore.apartmentId)
            .collection("chores")
            .document(chore.id)
            .set(chore.toDto())
            .await()
    }

    override suspend fun deleteChore(apartmentId: String, choreId: String) {
        firestore.collection("apartments")
            .document(apartmentId)
            .collection("chores")
            .document(choreId)
            .delete()
            .await()
    }
}
