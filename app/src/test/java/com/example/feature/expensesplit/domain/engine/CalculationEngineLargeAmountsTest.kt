package com.example.feature.expensesplit.domain.engine

import com.example.feature.expensesplit.domain.model.Split
import com.example.feature.expensesplit.domain.model.SplitType
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculationEngineLargeAmountsTest {

    @Test
    fun `test large amounts`() {
        val splits = listOf(
            Split("user1"),
            Split("user2"),
            Split("user3")
        )
        // Testing with 1,000,000.00
        val calculated = CalculationEngine.calculateSplits(1000000.00, splits, SplitType.EQUAL)
        assertEquals(333333.34, calculated[0].amount, 0.0)
        assertEquals(333333.33, calculated[1].amount, 0.0)
        assertEquals(333333.33, calculated[2].amount, 0.0)
        
        val sum = calculated.sumOf { it.amount }
        assertEquals(1000000.00, sum, 0.0)
    }

    @Test
    fun `test decimals exact`() {
        val splits = listOf(
            Split("user1", amount = 10.12),
            Split("user2", amount = 15.34),
            Split("user3", amount = 5.23)
        )
        val calculated = CalculationEngine.calculateSplits(30.69, splits, SplitType.EXACT)
        val sum = calculated.sumOf { it.amount }
        assertEquals(30.69, sum, 0.001)
    }
}
