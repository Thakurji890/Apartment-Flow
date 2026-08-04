package com.example.feature.expensesplit.domain.usecase

import com.example.feature.expensesplit.domain.engine.CalculationEngine
import com.example.feature.expensesplit.domain.model.Split
import com.example.feature.expensesplit.domain.model.SplitType
import javax.inject.Inject

class CalculateSplitsUseCase @Inject constructor() {
    operator fun invoke(
        totalAmount: Double,
        splits: List<Split>,
        globalSplitType: SplitType
    ): List<Split> {
        return CalculationEngine.calculateSplits(totalAmount, splits, globalSplitType)
    }
}
