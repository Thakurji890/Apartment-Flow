package com.example.feature.expensesplit.domain.engine

import com.example.feature.expensesplit.domain.model.Split
import com.example.feature.expensesplit.domain.model.SplitType
import kotlin.math.roundToLong

object CalculationEngine {

    /**
     * Calculates the splits for a given amount and list of splits.
     * Guaranteed to sum up to exactly the total amount.
     */
    fun calculateSplits(
        totalAmount: Double,
        splits: List<Split>,
        globalSplitType: SplitType
    ): List<Split> {
        if (splits.isEmpty() || totalAmount <= 0.0) return splits

        val totalAmountCents = (totalAmount * 100).roundToLong()
        
        var calculatedSplits = splits.map { it.copy(amount = 0.0) }

        when (globalSplitType) {
            SplitType.EQUAL -> {
                val memberCount = splits.size
                val baseAmountCents = totalAmountCents / memberCount
                var remainingCents = totalAmountCents % memberCount

                calculatedSplits = splits.mapIndexed { index, split ->
                    var cents = baseAmountCents
                    if (remainingCents > 0) {
                        cents += 1
                        remainingCents -= 1
                    }
                    split.copy(amount = cents / 100.0, type = SplitType.EQUAL)
                }
            }
            SplitType.PERCENTAGE -> {
                val totalPercentage = splits.sumOf { it.value ?: 0.0 }
                if (totalPercentage == 0.0) return splits // Avoid division by zero
                
                var remainingCents = totalAmountCents
                val unadjustedSplits = splits.map { split ->
                    val percentage = split.value ?: 0.0
                    val exactAmountCents = ((percentage / totalPercentage) * totalAmountCents).roundToLong()
                    remainingCents -= exactAmountCents
                    exactAmountCents
                }.toMutableList()

                // Distribute remaining cents to avoid rounding errors
                // Sort by fractional parts to be most fair, but for simplicity, we just distribute 1 cent to the first N
                var i = 0
                while (remainingCents > 0) {
                    unadjustedSplits[i % unadjustedSplits.size] += 1
                    remainingCents -= 1
                    i++
                }
                while (remainingCents < 0) {
                    unadjustedSplits[i % unadjustedSplits.size] -= 1
                    remainingCents += 1
                    i++
                }

                calculatedSplits = splits.mapIndexed { index, split ->
                    split.copy(amount = unadjustedSplits[index] / 100.0, type = SplitType.PERCENTAGE)
                }
            }
            SplitType.SHARES -> {
                val totalShares = splits.sumOf { it.value ?: 0.0 }
                if (totalShares == 0.0) return splits
                
                var remainingCents = totalAmountCents
                val unadjustedSplits = splits.map { split ->
                    val shares = split.value ?: 0.0
                    val exactAmountCents = ((shares / totalShares) * totalAmountCents).roundToLong()
                    remainingCents -= exactAmountCents
                    exactAmountCents
                }.toMutableList()

                var i = 0
                while (remainingCents > 0) {
                    unadjustedSplits[i % unadjustedSplits.size] += 1
                    remainingCents -= 1
                    i++
                }
                while (remainingCents < 0) {
                    unadjustedSplits[i % unadjustedSplits.size] -= 1
                    remainingCents += 1
                    i++
                }

                calculatedSplits = splits.mapIndexed { index, split ->
                    split.copy(amount = unadjustedSplits[index] / 100.0, type = SplitType.SHARES)
                }
            }
            SplitType.EXACT, SplitType.CUSTOM -> {
                // For exact/custom, we trust the amounts provided, but we might validate them separately
                // Ensure they don't exceed total, or distribute remainder if requested, 
                // but usually Exact means the user typed the exact amounts.
                calculatedSplits = splits.map { it.copy(type = globalSplitType) }
            }
        }
        return calculatedSplits
    }

    fun validateSplits(totalAmount: Double, splits: List<Split>): Boolean {
        val totalAmountCents = (totalAmount * 100).roundToLong()
        val splitsSumCents = splits.sumOf { (it.amount * 100).roundToLong() }
        return totalAmountCents == splitsSumCents
    }
}
