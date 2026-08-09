package com.example.feature.recurringbill.domain.usecase

import com.example.core.util.Resource
import com.example.feature.recurringbill.domain.model.RecurringBill
import com.example.feature.recurringbill.domain.repository.RecurringBillRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRecurringBillUseCase @Inject constructor(
    private val repository: RecurringBillRepository
) {
    operator fun invoke(billId: String): Flow<Resource<RecurringBill>> {
        return repository.getRecurringBill(billId)
    }
}
