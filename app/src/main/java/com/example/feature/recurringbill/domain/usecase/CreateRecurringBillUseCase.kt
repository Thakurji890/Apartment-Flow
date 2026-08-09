package com.example.feature.recurringbill.domain.usecase

import com.example.core.util.Resource
import com.example.feature.recurringbill.domain.model.RecurringBill
import com.example.feature.recurringbill.domain.repository.RecurringBillRepository
import javax.inject.Inject

class CreateRecurringBillUseCase @Inject constructor(
    private val repository: RecurringBillRepository
) {
    suspend operator fun invoke(bill: RecurringBill): Resource<Unit> {
        return repository.insertRecurringBill(bill)
    }
}
