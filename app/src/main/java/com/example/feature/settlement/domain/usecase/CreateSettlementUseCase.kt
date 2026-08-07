package com.example.feature.settlement.domain.usecase

import com.example.core.util.Resource
import com.example.feature.settlement.domain.model.Settlement
import com.example.feature.settlement.domain.repository.SettlementRepository
import javax.inject.Inject

class CreateSettlementUseCase @Inject constructor(
    private val repository: SettlementRepository
) {
    suspend operator fun invoke(settlement: Settlement): Resource<Unit> {
        if (settlement.amount <= 0) {
            return Resource.Error("Amount must be greater than 0")
        }
        if (settlement.debtorId == settlement.creditorId) {
            return Resource.Error("Cannot settle with yourself")
        }
        return repository.createSettlement(settlement)
    }
}
