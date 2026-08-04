package com.example.feature.apartment.domain.repository

import com.example.core.util.Resource
import com.example.feature.apartment.domain.model.Apartment
import com.example.feature.apartment.domain.model.ApartmentMember
import com.example.feature.apartment.domain.model.InviteCode
import com.example.feature.apartment.domain.model.Role
import kotlinx.coroutines.flow.Flow

interface ApartmentRepository {
    suspend fun createApartment(apartment: Apartment): Resource<Apartment>
    suspend fun joinApartment(inviteCode: String): Resource<Apartment>
    suspend fun getApartment(apartmentId: String): Resource<Apartment>
    fun getUserApartments(): Flow<Resource<List<Apartment>>>
    suspend fun hasApartment(): Resource<Boolean>
    
    // Member management
    fun getApartmentMembers(apartmentId: String): Flow<Resource<List<ApartmentMember>>>
    suspend fun updateMemberRole(apartmentId: String, memberId: String, newRole: Role): Resource<Unit>
    suspend fun removeMember(apartmentId: String, memberId: String): Resource<Unit>
    suspend fun leaveApartment(apartmentId: String): Resource<Unit>
    
    // Invite Codes
    suspend fun generateInviteCode(apartmentId: String): Resource<InviteCode>
    suspend fun getActiveInviteCode(apartmentId: String): Resource<InviteCode?>
    suspend fun deactivateInviteCode(code: String): Resource<Unit>
    
    // Apartment Management
    suspend fun updateApartment(apartment: Apartment): Resource<Unit>
    suspend fun archiveApartment(apartmentId: String): Resource<Unit>
    suspend fun deleteApartment(apartmentId: String): Resource<Unit>
}
