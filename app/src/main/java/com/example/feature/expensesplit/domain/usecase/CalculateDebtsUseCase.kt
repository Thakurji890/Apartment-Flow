package com.example.feature.expensesplit.domain.usecase

import com.example.feature.expensesplit.domain.engine.BalanceEngine
import com.example.feature.expensesplit.domain.model.Debt
import com.example.feature.expensesplit.domain.model.Payment
import com.example.feature.expensesplit.domain.model.Split
import javax.inject.Inject

class CalculateDebtsUseCase @Inject constructor() {
    operator fun invoke(
        expenseId: String,
        apartmentId: String,
        payments: List<Payment>,
        splits: List<Split>
    ): List<Debt> {
        return BalanceEngine.calculateDebts(expenseId, apartmentId, payments, splits)
    }
}
