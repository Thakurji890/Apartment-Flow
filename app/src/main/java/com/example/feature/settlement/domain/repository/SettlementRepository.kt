package com.example.feature.settlement.domain.repository

import com.example.core.util.Resource
import com.example.feature.settlement.domain.model.Settlement
import com.example.feature.settlement.domain.model.SettlementReceipt
import kotlinx.coroutines.flow.Flow

interface SettlementRepository {
    fun getSettlements(apartmentId: String): Flow<Resource<List<Settlement>>>
    
    fun getSettlement(settlementId: String): Flow<Resource<Settlement>>
    
    fun getSettlementsByUser(apartmentId: String, userId: String): Flow<Resource<List<Settlement>>>
    
    suspend fun createSettlement(settlement: Settlement): Resource<Unit>
    
    suspend fun updateSettlement(settlement: Settlement): Resource<Unit>
    
    suspend fun confirmSettlement(settlementId: String): Resource<Unit>
    
    suspend fun rejectSettlement(settlementId: String): Resource<Unit>
    
    suspend fun deleteSettlement(settlementId: String): Resource<Unit>
    
    suspend fun uploadReceipt(receipt: SettlementReceipt, fileBytes: ByteArray): Resource<String>
}
