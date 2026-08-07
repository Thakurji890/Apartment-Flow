package com.example.feature.settlement.domain.usecase

import javax.inject.Inject

data class SettlementUseCases @Inject constructor(
    val createSettlement: CreateSettlementUseCase,
    val confirmSettlement: ConfirmSettlementUseCase,
    val rejectSettlement: RejectSettlementUseCase,
    val getSettlements: GetSettlementsUseCase,
    val getSettlement: GetSettlementUseCase
)
