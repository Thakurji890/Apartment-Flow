package com.example.feature.settlement.data.repository

import com.example.core.util.Resource
import com.example.feature.settlement.domain.model.Settlement
import com.example.feature.settlement.domain.model.SettlementReceipt
import com.example.feature.settlement.domain.model.SettlementStatus
import com.example.feature.settlement.domain.repository.SettlementRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class SettlementRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : SettlementRepository {

    override fun getSettlements(apartmentId: String): Flow<Resource<List<Settlement>>> = callbackFlow {
        val listener = firestore.collection("settlements")
            .whereEqualTo("apartmentId", apartmentId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Unknown error"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val settlements = snapshot.documents.mapNotNull { it.toObject(Settlement::class.java) }
                    trySend(Resource.Success(settlements))
                }
            }
        awaitClose { listener.remove() }
    }

    override fun getSettlement(settlementId: String): Flow<Resource<Settlement>> = callbackFlow {
        val listener = firestore.collection("settlements").document(settlementId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Unknown error"))
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    snapshot.toObject(Settlement::class.java)?.let {
                        trySend(Resource.Success(it))
                    }
                } else {
                    trySend(Resource.Error("Settlement not found"))
                }
            }
        awaitClose { listener.remove() }
    }

    override fun getSettlementsByUser(
        apartmentId: String,
        userId: String
    ): Flow<Resource<List<Settlement>>> = callbackFlow {
        val listener = firestore.collection("settlements")
            .whereEqualTo("apartmentId", apartmentId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Unknown error"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val settlements = snapshot.documents
                        .mapNotNull { it.toObject(Settlement::class.java) }
                        .filter { it.debtorId == userId || it.creditorId == userId }
                        .sortedByDescending { it.createdAt }
                    trySend(Resource.Success(settlements))
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun createSettlement(settlement: Settlement): Resource<Unit> {
        return try {
            val id = if (settlement.settlementId.isEmpty()) UUID.randomUUID().toString() else settlement.settlementId
            val newSettlement = settlement.copy(
                settlementId = id,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            firestore.collection("settlements").document(id).set(newSettlement).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to create settlement")
        }
    }

    override suspend fun updateSettlement(settlement: Settlement): Resource<Unit> {
        return try {
            val updatedSettlement = settlement.copy(updatedAt = System.currentTimeMillis())
            firestore.collection("settlements").document(settlement.settlementId).set(updatedSettlement).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update settlement")
        }
    }

    override suspend fun confirmSettlement(settlementId: String): Resource<Unit> {
        return try {
            firestore.runTransaction { transaction ->
                val settlementRef = firestore.collection("settlements").document(settlementId)
                val snapshot = transaction.get(settlementRef)
                val settlement = snapshot.toObject(Settlement::class.java) ?: throw Exception("Settlement not found")
                
                if (settlement.status != SettlementStatus.PENDING) {
                    throw Exception("Only pending settlements can be confirmed")
                }

                transaction.update(settlementRef, "status", SettlementStatus.CONFIRMED.name)
                transaction.update(settlementRef, "updatedAt", System.currentTimeMillis())
            }.await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to confirm settlement")
        }
    }

    override suspend fun rejectSettlement(settlementId: String): Resource<Unit> {
        return try {
            val settlementRef = firestore.collection("settlements").document(settlementId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(settlementRef)
                val settlement = snapshot.toObject(Settlement::class.java) ?: throw Exception("Settlement not found")
                
                if (settlement.status != SettlementStatus.PENDING) {
                    throw Exception("Only pending settlements can be rejected")
                }

                transaction.update(settlementRef, "status", SettlementStatus.REJECTED.name)
                transaction.update(settlementRef, "updatedAt", System.currentTimeMillis())
            }.await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to reject settlement")
        }
    }

    override suspend fun deleteSettlement(settlementId: String): Resource<Unit> {
        return try {
            val settlementRef = firestore.collection("settlements").document(settlementId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(settlementRef)
                val settlement = snapshot.toObject(Settlement::class.java) ?: throw Exception("Settlement not found")
                
                if (settlement.status == SettlementStatus.CONFIRMED) {
                    throw Exception("Cannot delete a confirmed settlement")
                }

                transaction.delete(settlementRef)
            }.await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete settlement")
        }
    }

    override suspend fun uploadReceipt(receipt: SettlementReceipt, fileBytes: ByteArray): Resource<String> {
        return Resource.Success("https://dummy.url/receipt/${receipt.receiptId}")
    }
}
