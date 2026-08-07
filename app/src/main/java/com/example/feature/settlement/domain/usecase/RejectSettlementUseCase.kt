package com.example.feature.settlement.domain.usecase

import com.example.core.util.Resource
import com.example.feature.settlement.domain.repository.SettlementRepository
import javax.inject.Inject

class RejectSettlementUseCase @Inject constructor(
    private val repository: SettlementRepository
) {
    suspend operator fun invoke(settlementId: String): Resource<Unit> {
        return repository.rejectSettlement(settlementId)
    }
}
