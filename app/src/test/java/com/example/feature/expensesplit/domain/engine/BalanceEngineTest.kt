package com.example.feature.expensesplit.domain.engine

import com.example.feature.expensesplit.domain.model.Payment
import com.example.feature.expensesplit.domain.model.Split
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BalanceEngineTest {

    @Test
    fun `test simple split debts`() {
        // user1 paid 300, splits are user1=100, user2=100, user3=100
        val payments = listOf(Payment("user1", 300.0))
        val splits = listOf(
            Split("user1", amount = 100.0),
            Split("user2", amount = 100.0),
            Split("user3", amount = 100.0)
        )
        
        val debts = BalanceEngine.calculateDebts("exp1", "apt1", payments, splits)
        
        assertEquals(2, debts.size)
        // user2 owes user1 100
        val debt1 = debts.find { it.debtorId == "user2" && it.creditorId == "user1" }
        assertEquals(100.0, debt1?.amount ?: 0.0, 0.0)
        
        // user3 owes user1 100
        val debt2 = debts.find { it.debtorId == "user3" && it.creditorId == "user1" }
        assertEquals(100.0, debt2?.amount ?: 0.0, 0.0)
    }

    @Test
    fun `test complex multi-payer debts`() {
        // user1 paid 100, user2 paid 200. Total = 300
        // splits: user1=100, user2=100, user3=100
        val payments = listOf(
            Payment("user1", 100.0),
            Payment("user2", 200.0)
        )
        val splits = listOf(
            Split("user1", amount = 100.0),
            Split("user2", amount = 100.0),
            Split("user3", amount = 100.0)
        )
        
        val debts = BalanceEngine.calculateDebts("exp1", "apt1", payments, splits)
        
        assertEquals(1, debts.size)
        // user3 owes user2 100
        val debt = debts.first()
        assertEquals("user3", debt.debtorId)
        assertEquals("user2", debt.creditorId)
        assertEquals(100.0, debt.amount, 0.0)
    }
}
