package com.example.feature.settlement.domain.usecase

import com.example.core.util.Resource
import com.example.feature.settlement.domain.model.Settlement
import com.example.feature.settlement.domain.repository.SettlementRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSettlementUseCase @Inject constructor(
    private val repository: SettlementRepository
) {
    operator fun invoke(settlementId: String): Flow<Resource<Settlement>> {
        return repository.getSettlement(settlementId)
    }
}
