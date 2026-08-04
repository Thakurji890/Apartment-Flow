package com.example.feature.expensesplit.domain.engine

import com.example.feature.expensesplit.domain.model.Split
import com.example.feature.expensesplit.domain.model.SplitType
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculationEngineTest {

    @Test
    fun `test equal split`() {
        val splits = listOf(
            Split("user1"),
            Split("user2"),
            Split("user3"),
            Split("user4")
        )
        val calculated = CalculationEngine.calculateSplits(400.0, splits, SplitType.EQUAL)
        calculated.forEach {
            assertEquals(100.0, it.amount, 0.0)
        }
    }

    @Test
    fun `test exact split`() {
        val splits = listOf(
            Split("user1", amount = 120.0),
            Split("user2", amount = 80.0),
            Split("user3", amount = 200.0)
        )
        val calculated = CalculationEngine.calculateSplits(400.0, splits, SplitType.EXACT)
        assertEquals(120.0, calculated[0].amount, 0.0)
        assertEquals(80.0, calculated[1].amount, 0.0)
        assertEquals(200.0, calculated[2].amount, 0.0)
    }

    @Test
    fun `test percentage split`() {
        val splits = listOf(
            Split("user1", value = 50.0),
            Split("user2", value = 30.0),
            Split("user3", value = 20.0)
        )
        val calculated = CalculationEngine.calculateSplits(400.0, splits, SplitType.PERCENTAGE)
        assertEquals(200.0, calculated[0].amount, 0.0)
        assertEquals(120.0, calculated[1].amount, 0.0)
        assertEquals(80.0, calculated[2].amount, 0.0)
    }

    @Test
    fun `test share split`() {
        val splits = listOf(
            Split("user1", value = 3.0),
            Split("user2", value = 2.0),
            Split("user3", value = 1.0)
        )
        val calculated = CalculationEngine.calculateSplits(600.0, splits, SplitType.SHARES)
        assertEquals(300.0, calculated[0].amount, 0.0)
        assertEquals(200.0, calculated[1].amount, 0.0)
        assertEquals(100.0, calculated[2].amount, 0.0)
    }

    @Test
    fun `test rounding equal split`() {
        val splits = listOf(
            Split("user1"),
            Split("user2"),
            Split("user3")
        )
        val calculated = CalculationEngine.calculateSplits(10.0, splits, SplitType.EQUAL)
        // 10.0 / 3 = 3.33, 3.33, 3.34
        assertEquals(3.34, calculated[0].amount, 0.001) // Gets the extra cent
        assertEquals(3.33, calculated[1].amount, 0.001)
        assertEquals(3.33, calculated[2].amount, 0.001)
        
        val sum = calculated.sumOf { it.amount }
        assertEquals(10.0, sum, 0.001)
    }

    @Test
    fun `test rounding percentage split`() {
        val splits = listOf(
            Split("user1", value = 33.33),
            Split("user2", value = 33.33),
            Split("user3", value = 33.34)
        )
        val calculated = CalculationEngine.calculateSplits(100.0, splits, SplitType.PERCENTAGE)
        val sum = calculated.sumOf { it.amount }
        assertEquals(100.0, sum, 0.001)
    }
}
