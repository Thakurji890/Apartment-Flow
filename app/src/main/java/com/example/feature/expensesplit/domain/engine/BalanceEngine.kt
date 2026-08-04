package com.example.feature.expensesplit.domain.engine

import com.example.feature.expensesplit.domain.model.Debt
import com.example.feature.expensesplit.domain.model.Payment
import com.example.feature.expensesplit.domain.model.Split
import java.util.UUID
import kotlin.math.absoluteValue
import kotlin.math.roundToLong

object BalanceEngine {

    /**
     * Calculates the minimum debts required to settle the expense.
     * Takes payments (who paid how much) and splits (who owes how much) 
     * and generates direct debts from debtors to creditors.
     */
    fun calculateDebts(
        expenseId: String,
        apartmentId: String,
        payments: List<Payment>,
        splits: List<Split>
    ): List<Debt> {
        val balancesCents = mutableMapOf<String, Long>()

        // Add what people paid (they are owed this money)
        for (payment in payments) {
            val amountCents = (payment.amount * 100).roundToLong()
            balancesCents[payment.userId] = (balancesCents[payment.userId] ?: 0L) + amountCents
        }

        // Subtract what people owe (their share of the expense)
        for (split in splits) {
            val amountCents = (split.amount * 100).roundToLong()
            balancesCents[split.userId] = (balancesCents[split.userId] ?: 0L) - amountCents
        }

        val debtors = mutableListOf<Pair<String, Long>>()
        val creditors = mutableListOf<Pair<String, Long>>()

        for ((userId, balance) in balancesCents) {
            if (balance < 0) {
                debtors.add(userId to balance.absoluteValue)
            } else if (balance > 0) {
                creditors.add(userId to balance)
            }
        }

        debtors.sortByDescending { it.second }
        creditors.sortByDescending { it.second }

        val debts = mutableListOf<Debt>()
        var dIndex = 0
        var cIndex = 0

        while (dIndex < debtors.size && cIndex < creditors.size) {
            val (debtorId, debtAmount) = debtors[dIndex]
            val (creditorId, creditAmount) = creditors[cIndex]

            val settleAmountCents = minOf(debtAmount, creditAmount)

            if (settleAmountCents > 0) {
                debts.add(
                    Debt(
                        debtId = UUID.randomUUID().toString(),
                        apartmentId = apartmentId,
                        expenseId = expenseId,
                        debtorId = debtorId,
                        creditorId = creditorId,
                        amount = settleAmountCents / 100.0
                    )
                )
            }

            debtors[dIndex] = debtorId to (debtAmount - settleAmountCents)
            creditors[cIndex] = creditorId to (creditAmount - settleAmountCents)

            if (debtors[dIndex].second == 0L) dIndex++
            if (creditors[cIndex].second == 0L) cIndex++
        }

        return debts
    }
}
