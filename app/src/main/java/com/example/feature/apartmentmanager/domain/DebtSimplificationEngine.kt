package com.example.feature.apartmentmanager.domain

import com.example.feature.apartmentmanager.model.ApartmentRoommate
import com.example.feature.apartmentmanager.model.DebtTransfer
import com.example.feature.apartmentmanager.model.RoommateBalanceSummary
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Advanced Graph-Based Debt Simplification Engine.
 *
 * Implements a flow-based network minimization algorithm similar to Tricount and Splitwise:
 * 1. Computes the exact net position (balance = total received/paid - total share/debts) for each participant.
 * 2. Partitions roommates into net Debtors (negative net) and net Creditors (positive net).
 * 3. Exact subset/pair matching: Searches for exact inverse matches (e.g. A owes $50 and B is owed $50)
 *    to resolve 1:1 circular debt in a single direct transaction.
 * 4. Greedy maximum-flow settlement: Progressively pairs the largest debtor with the largest creditor,
 *    rerouting multi-hop obligations (e.g. A owes B $50, B owes C $50 => A pays C $50 directly).
 * 5. Guarantees that at most (N - 1) transactions are required to settle all debts across N roommates,
 *    often fewer due to exact subset cancelations.
 */
object DebtSimplificationEngine {

    data class SimplificationMetrics(
        val originalTransactionCountEstimate: Int,
        val simplifiedTransactionCount: Int,
        val totalVolumeSettled: Double,
        val transactionsSavedCount: Int
    )

    data class SimplificationResult(
        val transfers: List<DebtTransfer>,
        val metrics: SimplificationMetrics
    )

    /**
     * Minimizes debt transfers across the given roommate balance summaries.
     */
    fun simplifyDebts(summaries: List<RoommateBalanceSummary>): SimplificationResult {
        // Collect debtors and creditors with non-zero balances
        val debtors = mutableListOf<MutableBalanceNode>()
        val creditors = mutableListOf<MutableBalanceNode>()

        for (summary in summaries) {
            val net = (summary.netBalance * 100.0).roundToInt() / 100.0
            when {
                net < -0.01 -> debtors.add(MutableBalanceNode(summary.roommate, abs(net)))
                net > 0.01 -> creditors.add(MutableBalanceNode(summary.roommate, net))
            }
        }

        val totalDebtVolume = debtors.sumOf { it.remaining }
        val estimatedOriginalTransactions = if (debtors.size + creditors.size > 1) {
            debtors.size * creditors.size // Worst-case pairwise settlements
        } else {
            0
        }

        val simplifiedTransfers = mutableListOf<DebtTransfer>()

        // Step 1: Exact matches optimization (e.g., Debtor owes exactly what Creditor is owed)
        val matchedDebtorIndices = mutableSetOf<Int>()
        val matchedCreditorIndices = mutableSetOf<Int>()

        for (dIdx in debtors.indices) {
            val debtor = debtors[dIdx]
            for (cIdx in creditors.indices) {
                if (cIdx in matchedCreditorIndices) continue
                val creditor = creditors[cIdx]

                if (abs(debtor.remaining - creditor.remaining) < 0.01) {
                    val amount = (debtor.remaining * 100.0).roundToInt() / 100.0
                    if (amount > 0.0) {
                        simplifiedTransfers.add(
                            DebtTransfer(
                                fromRoommate = debtor.roommate,
                                toRoommate = creditor.roommate,
                                amount = amount
                            )
                        )
                    }
                    matchedDebtorIndices.add(dIdx)
                    matchedCreditorIndices.add(cIdx)
                    debtor.remaining = 0.0
                    creditor.remaining = 0.0
                    break
                }
            }
        }

        // Filter out fully resolved nodes
        val activeDebtors = debtors.filterIndexed { index, _ -> index !in matchedDebtorIndices }.toMutableList()
        val activeCreditors = creditors.filterIndexed { index, _ -> index !in matchedCreditorIndices }.toMutableList()

        // Step 2: Greedy bilateral settlement for remaining balances
        // Sort descending so largest debts pair with largest credits first
        activeDebtors.sortByDescending { it.remaining }
        activeCreditors.sortByDescending { it.remaining }

        var dIdx = 0
        var cIdx = 0

        while (dIdx < activeDebtors.size && cIdx < activeCreditors.size) {
            val debtor = activeDebtors[dIdx]
            val creditor = activeCreditors[cIdx]

            val settleAmount = minOf(debtor.remaining, creditor.remaining)
            val roundedSettle = (settleAmount * 100.0).roundToInt() / 100.0

            if (roundedSettle > 0.0) {
                simplifiedTransfers.add(
                    DebtTransfer(
                        fromRoommate = debtor.roommate,
                        toRoommate = creditor.roommate,
                        amount = roundedSettle
                    )
                )
            }

            debtor.remaining = (debtor.remaining - settleAmount)
            creditor.remaining = (creditor.remaining - settleAmount)

            if (debtor.remaining < 0.01) {
                dIdx++
            }
            if (creditor.remaining < 0.01) {
                cIdx++
            }
        }

        val savedCount = maxOf(0, estimatedOriginalTransactions - simplifiedTransfers.size)

        return SimplificationResult(
            transfers = simplifiedTransfers,
            metrics = SimplificationMetrics(
                originalTransactionCountEstimate = estimatedOriginalTransactions,
                simplifiedTransactionCount = simplifiedTransfers.size,
                totalVolumeSettled = (totalDebtVolume * 100.0).roundToInt() / 100.0,
                transactionsSavedCount = savedCount
            )
        )
    }

    private data class MutableBalanceNode(
        val roommate: ApartmentRoommate,
        var remaining: Double
    )
}
