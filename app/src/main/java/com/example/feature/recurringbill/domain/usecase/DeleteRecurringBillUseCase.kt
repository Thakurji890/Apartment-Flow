package com.example.feature.recurringbill.domain.usecase

import com.example.core.util.Resource
import com.example.feature.recurringbill.domain.repository.RecurringBillRepository
import javax.inject.Inject

class DeleteRecurringBillUseCase @Inject constructor(
    private val repository: RecurringBillRepository
) {
    suspend operator fun invoke(billId: String): Resource<Unit> {
        return repository.deleteRecurringBill(billId)
    }
}
