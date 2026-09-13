package com.example.feature.expensesplit.domain.engine

import com.example.feature.expensesplit.domain.model.Debt
import com.example.feature.expensesplit.domain.model.Payment
import com.example.feature.expensesplit.domain.model.Split
import kotlin.math.min
import kotlin.math.round

object BalanceEngine {

    fun calculateDebts(
        expenseId: String,
        apartmentId: String,
        payments: List<Payment>,
        splits: List<Split>
    ): List<Debt> {
        val balances = mutableMapOf<String, Double>()

        for (p in payments) {
            balances[p.userId] = (balances[p.userId] ?: 0.0) + p.amount
        }

        for (s in splits) {
            balances[s.userId] = (balances[s.userId] ?: 0.0) - s.amount
        }

        val creditors = mutableListOf<Pair<String, Double>>()
        val debtors = mutableListOf<Pair<String, Double>>()

        for ((user, balance) in balances) {
            val rounded = round(balance * 100.0) / 100.0
            if (rounded > 0.001) {
                creditors.add(user to rounded)
            } else if (rounded < -0.001) {
                debtors.add(user to -rounded)
            }
        }

        val debts = mutableListOf<Debt>()
        var cIdx = 0
        var dIdx = 0

        while (cIdx < creditors.size && dIdx < debtors.size) {
            val (cUser, cAmount) = creditors[cIdx]
            val (dUser, dAmount) = debtors[dIdx]

            val settleAmount = round(min(cAmount, dAmount) * 100.0) / 100.0
            if (settleAmount > 0.001) {
                debts.add(
                    Debt(
                        expenseId = expenseId,
                        apartmentId = apartmentId,
                        debtorId = dUser,
                        creditorId = cUser,
                        amount = settleAmount
                    )
                )
            }

            val remC = round((cAmount - settleAmount) * 100.0) / 100.0
            val remD = round((dAmount - settleAmount) * 100.0) / 100.0

            if (remC <= 0.001) {
                cIdx++
            } else {
                creditors[cIdx] = cUser to remC
            }

            if (remD <= 0.001) {
                dIdx++
            } else {
                debtors[dIdx] = dUser to remD
            }
        }

        return debts
    }
}
