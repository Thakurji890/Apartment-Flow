package com.example.feature.apartment.data.repository

import com.example.core.util.Resource
import com.example.feature.apartment.domain.model.Apartment
import com.example.feature.apartment.domain.model.ApartmentMember
import com.example.feature.apartment.domain.model.InviteCode
import com.example.feature.apartment.domain.model.Role
import com.example.feature.apartment.domain.repository.ApartmentRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import kotlin.random.Random

class ApartmentRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ApartmentRepository {

    private val apartmentsCollection = firestore.collection("apartments")
    private val membersCollection = firestore.collection("apartmentMembers")
    private val inviteCodesCollection = firestore.collection("inviteCodes")

    private fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: throw Exception("User not authenticated")
    }

    override suspend fun createApartment(apartment: Apartment): Resource<Apartment> {
        return try {
            val userId = getCurrentUserId()
            val apartmentId = UUID.randomUUID().toString()
            val inviteCodeString = generateUniqueInviteCodeString()
            
            val newApartment = apartment.copy(
                id = apartmentId,
                inviteCode = inviteCodeString,
                createdAt = System.currentTimeMillis()
            )

            // Start a batch write
            val batch = firestore.batch()

            // 1. Create apartment
            val apartmentRef = apartmentsCollection.document(apartmentId)
            batch.set(apartmentRef, newApartment)

            // 2. Add creator as owner
            val memberId = UUID.randomUUID().toString()
            val memberRef = membersCollection.document(memberId)
            val member = ApartmentMember(
                id = memberId,
                apartmentId = apartmentId,
                userId = userId,
                displayName = auth.currentUser?.displayName ?: "Unknown",
                photoUrl = auth.currentUser?.photoUrl?.toString(),
                role = Role.OWNER
            )
            batch.set(memberRef, member)

            // 3. Create invite code
            val inviteCodeRef = inviteCodesCollection.document(inviteCodeString)
            val inviteCode = InviteCode(
                code = inviteCodeString,
                apartmentId = apartmentId,
                createdBy = userId
            )
            batch.set(inviteCodeRef, inviteCode)

            batch.commit().await()
            
            Resource.Success(newApartment)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to create apartment")
        }
    }

    override suspend fun joinApartment(inviteCode: String): Resource<Apartment> {
        return try {
            val userId = getCurrentUserId()

            // 1. Verify invite code
            val inviteCodeSnapshot = inviteCodesCollection.document(inviteCode).get().await()
            if (!inviteCodeSnapshot.exists()) {
                return Resource.Error("Invalid invite code")
            }
            
            val invite = inviteCodeSnapshot.toObject(InviteCode::class.java)
                ?: return Resource.Error("Invalid invite code data")
                
            if (!invite.isActive || invite.expiresAt < System.currentTimeMillis()) {
                return Resource.Error("Invite code has expired")
            }

            // 2. Check if already a member
            val existingMembership = membersCollection
                .whereEqualTo("apartmentId", invite.apartmentId)
                .whereEqualTo("userId", userId)
                .get().await()
                
            if (!existingMembership.isEmpty) {
                return Resource.Error("You are already a member of this apartment")
            }

            // 3. Get apartment details
            val apartmentSnapshot = apartmentsCollection.document(invite.apartmentId).get().await()
            if (!apartmentSnapshot.exists()) {
                return Resource.Error("Apartment not found")
            }
            val apartment = apartmentSnapshot.toObject(Apartment::class.java)
                ?: return Resource.Error("Failed to parse apartment data")
                
            if (apartment.isArchived) {
                return Resource.Error("This apartment is archived")
            }

            // 4. Add user as member
            val memberId = UUID.randomUUID().toString()
            val member = ApartmentMember(
                id = memberId,
                apartmentId = apartment.id,
                userId = userId,
                displayName = auth.currentUser?.displayName ?: "Unknown",
                photoUrl = auth.currentUser?.photoUrl?.toString(),
                role = Role.MEMBER
            )
            
            membersCollection.document(memberId).set(member).await()
            
            Resource.Success(apartment)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to join apartment")
        }
    }

    override suspend fun getApartment(apartmentId: String): Resource<Apartment> {
        return try {
            val snapshot = apartmentsCollection.document(apartmentId).get().await()
            val apartment = snapshot.toObject(Apartment::class.java)
            if (apartment != null) {
                Resource.Success(apartment)
            } else {
                Resource.Error("Apartment not found")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to get apartment")
        }
    }

    override fun getUserApartments(): Flow<Resource<List<Apartment>>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(Resource.Error("User not authenticated"))
            close()
            return@callbackFlow
        }
        
        val listener = membersCollection.whereEqualTo("userId", userId)
            .addSnapshotListener { membersSnapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Unknown error"))
                    return@addSnapshotListener
                }
                
                if (membersSnapshot == null || membersSnapshot.isEmpty) {
                    trySend(Resource.Success(emptyList()))
                    return@addSnapshotListener
                }
                
                val apartmentIds = membersSnapshot.documents.mapNotNull { it.getString("apartmentId") }
                
                // Firestore 'in' queries are limited to 10 elements. 
                // For simplicity, we assume a user is in < 10 apartments.
                if (apartmentIds.isEmpty()) {
                    trySend(Resource.Success(emptyList()))
                } else if (apartmentIds.size <= 10) {
                    apartmentsCollection.whereIn("id", apartmentIds).get()
                        .addOnSuccessListener { aptsSnapshot ->
                            val apartments = aptsSnapshot.toObjects(Apartment::class.java)
                                .filter { !it.isArchived }
                            trySend(Resource.Success(apartments))
                        }
                        .addOnFailureListener { e ->
                            trySend(Resource.Error(e.localizedMessage ?: "Failed to fetch apartments"))
                        }
                } else {
                    trySend(Resource.Error("Too many apartments to fetch"))
                }
            }
            
        awaitClose { listener.remove() }
    }

    override suspend fun hasApartment(): Resource<Boolean> {
        return try {
            val userId = getCurrentUserId()
            val snapshot = membersCollection.whereEqualTo("userId", userId).limit(1).get().await()
            Resource.Success(!snapshot.isEmpty)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to check apartments")
        }
    }

    override fun getApartmentMembers(apartmentId: String): Flow<Resource<List<ApartmentMember>>> = callbackFlow {
        val listener = membersCollection.whereEqualTo("apartmentId", apartmentId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Unknown error"))
                    return@addSnapshotListener
                }
                
                val members = snapshot?.toObjects(ApartmentMember::class.java) ?: emptyList()
                trySend(Resource.Success(members))
            }
            
        awaitClose { listener.remove() }
    }

    override suspend fun updateMemberRole(apartmentId: String, memberId: String, newRole: Role): Resource<Unit> {
        return try {
            membersCollection.document(memberId).update("role", newRole).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update role")
        }
    }

    override suspend fun removeMember(apartmentId: String, memberId: String): Resource<Unit> {
        return try {
            membersCollection.document(memberId).delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to remove member")
        }
    }

    override suspend fun leaveApartment(apartmentId: String): Resource<Unit> {
         return try {
            val userId = getCurrentUserId()
            val membership = membersCollection
                .whereEqualTo("apartmentId", apartmentId)
                .whereEqualTo("userId", userId)
                .get().await()
            
            if (!membership.isEmpty) {
                val memberId = membership.documents.first().id
                membersCollection.document(memberId).delete().await()
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to leave apartment")
        }
    }

    override suspend fun generateInviteCode(apartmentId: String): Resource<InviteCode> {
        return try {
            val userId = getCurrentUserId()
            val code = generateUniqueInviteCodeString()
            val inviteCode = InviteCode(
                code = code,
                apartmentId = apartmentId,
                createdBy = userId
            )
            inviteCodesCollection.document(code).set(inviteCode).await()
            
            // Update apartment's active invite code
            apartmentsCollection.document(apartmentId).update("inviteCode", code).await()
            
            Resource.Success(inviteCode)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to generate invite code")
        }
    }

    override suspend fun getActiveInviteCode(apartmentId: String): Resource<InviteCode?> {
        return try {
            val snapshot = inviteCodesCollection
                .whereEqualTo("apartmentId", apartmentId)
                .whereEqualTo("isActive", true)
                .get().await()
            
            val validInvites = snapshot.toObjects(InviteCode::class.java)
                .filter { it.expiresAt > System.currentTimeMillis() }
                .sortedByDescending { it.createdAt }
                
            Resource.Success(validInvites.firstOrNull())
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to get invite code")
        }
    }

    override suspend fun deactivateInviteCode(code: String): Resource<Unit> {
        return try {
            inviteCodesCollection.document(code).update("isActive", false).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to deactivate invite code")
        }
    }

    override suspend fun updateApartment(apartment: Apartment): Resource<Unit> {
        return try {
            apartmentsCollection.document(apartment.id).set(apartment).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update apartment")
        }
    }

    override suspend fun archiveApartment(apartmentId: String): Resource<Unit> {
        return try {
            apartmentsCollection.document(apartmentId).update("isArchived", true).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to archive apartment")
        }
    }

    override suspend fun deleteApartment(apartmentId: String): Resource<Unit> {
        return try {
            // Check if empty first
            val members = membersCollection.whereEqualTo("apartmentId", apartmentId).get().await()
            if (members.size() > 1) {
                return Resource.Error("Cannot delete apartment with active members")
            }
            
            val batch = firestore.batch()
            
            // Delete apartment
            batch.delete(apartmentsCollection.document(apartmentId))
            
            // Delete remaining member (owner)
            if (!members.isEmpty) {
                batch.delete(members.documents.first().reference)
            }
            
            // Delete associated invite codes
            val inviteCodes = inviteCodesCollection.whereEqualTo("apartmentId", apartmentId).get().await()
            for (doc in inviteCodes.documents) {
                batch.delete(doc.reference)
            }
            
            batch.commit().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to delete apartment")
        }
    }

    private fun generateUniqueInviteCodeString(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6)
            .map { chars[Random.nextInt(chars.length)] }
            .joinToString("")
    }
}
