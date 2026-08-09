package com.example.feature.recurringbill.domain.repository

import com.example.core.util.Resource
import com.example.feature.recurringbill.domain.model.RecurringBill
import kotlinx.coroutines.flow.Flow

interface RecurringBillRepository {
    fun getRecurringBills(apartmentId: String): Flow<Resource<List<RecurringBill>>>
    fun getRecurringBill(billId: String): Flow<Resource<RecurringBill>>
    suspend fun insertRecurringBill(bill: RecurringBill): Resource<Unit>
    suspend fun updateRecurringBill(bill: RecurringBill): Resource<Unit>
    suspend fun deleteRecurringBill(billId: String): Resource<Unit>
    suspend fun generateExpenseForBill(
        bill: RecurringBill,
        occurrenceId: String,
        amount: Double,
        actualDate: Long
    ): Resource<String>
    suspend fun skipOccurrence(billId: String, nextDueDate: Long): Resource<Unit>
}
