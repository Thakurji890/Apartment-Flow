package com.example.feature.expensesplit.domain.engine

import com.example.feature.expensesplit.domain.model.Split
import com.example.feature.expensesplit.domain.model.SplitType
import kotlin.math.round

object CalculationEngine {

    fun calculateSplits(totalAmount: Double, splits: List<Split>, splitType: SplitType): List<Split> {
        if (splits.isEmpty()) return emptyList()

        return when (splitType) {
            SplitType.EQUAL -> {
                val count = splits.size
                val totalCents = round(totalAmount * 100).toLong()
                val baseCents = totalCents / count
                val remainderCents = (totalCents % count).toInt()

                splits.mapIndexed { index, split ->
                    val cents = if (index < remainderCents) baseCents + 1 else baseCents
                    split.copy(amount = cents / 100.0)
                }
            }

            SplitType.EXACT -> {
                splits.map { it.copy() }
            }

            SplitType.PERCENTAGE -> {
                val rawAmounts = splits.map { split ->
                    round(totalAmount * (split.value / 100.0) * 100.0) / 100.0
                }.toMutableList()

                val sum = rawAmounts.sum()
                val diffCents = round((totalAmount - sum) * 100).toInt()
                if (diffCents != 0 && rawAmounts.isNotEmpty()) {
                    // Adjust rounding on the element with the highest percentage or first
                    val maxIndex = splits.indices.maxByOrNull { splits[it].value } ?: 0
                    rawAmounts[maxIndex] = round((rawAmounts[maxIndex] + diffCents / 100.0) * 100.0) / 100.0
                }

                splits.mapIndexed { index, split ->
                    split.copy(amount = rawAmounts[index])
                }
            }

            SplitType.SHARES -> {
                val totalShares = splits.sumOf { it.value }
                if (totalShares <= 0) {
                    calculateSplits(totalAmount, splits, SplitType.EQUAL)
                } else {
                    val rawAmounts = splits.map { split ->
                        round(totalAmount * (split.value / totalShares) * 100.0) / 100.0
                    }.toMutableList()

                    val sum = rawAmounts.sum()
                    val diffCents = round((totalAmount - sum) * 100).toInt()
                    if (diffCents != 0 && rawAmounts.isNotEmpty()) {
                        val maxIndex = splits.indices.maxByOrNull { splits[it].value } ?: 0
                        rawAmounts[maxIndex] = round((rawAmounts[maxIndex] + diffCents / 100.0) * 100.0) / 100.0
                    }

                    splits.mapIndexed { index, split ->
                        split.copy(amount = rawAmounts[index])
                    }
                }
            }
        }
    }
}
