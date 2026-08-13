package com.example.feature.roommate.data.repository

import com.example.core.util.Resource
import com.example.feature.roommate.data.dto.RoommateDto
import com.example.feature.roommate.data.mapper.toDto
import com.example.feature.roommate.data.mapper.toRoommate
import com.example.feature.roommate.domain.model.Roommate
import com.example.feature.roommate.domain.repository.RoommateRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class RoommateRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : RoommateRepository {

    override suspend fun getRoommateProfile(apartmentId: String, userId: String): Resource<Roommate?> {
        return try {
            val snapshot = firestore.collection("apartments")
                .document(apartmentId)
                .collection("roommates")
                .document(userId)
                .get()
                .await()

            if (snapshot.exists()) {
                val dto = snapshot.toObject(RoommateDto::class.java)
                Resource.Success(dto?.toRoommate())
            } else {
                Resource.Success(null)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to get roommate profile")
        }
    }

    override fun getRoommatesProfiles(apartmentId: String): Flow<Resource<List<Roommate>>> = callbackFlow {
        val listenerRegistration = firestore.collection("apartments")
            .document(apartmentId)
            .collection("roommates")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed to fetch roommate profiles"))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val roommates = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(RoommateDto::class.java)?.toRoommate()
                    }
                    trySend(Resource.Success(roommates))
                }
            }

        awaitClose {
            listenerRegistration.remove()
        }
    }

    override suspend fun saveRoommateProfile(roommate: Roommate): Resource<Unit> {
        return try {
            firestore.collection("apartments")
                .document(roommate.apartmentId)
                .collection("roommates")
                .document(roommate.userId)
                .set(roommate.toDto())
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to save roommate profile")
        }
    }
}
