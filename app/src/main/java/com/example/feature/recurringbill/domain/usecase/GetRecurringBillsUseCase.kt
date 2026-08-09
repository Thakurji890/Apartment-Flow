package com.example.feature.recurringbill.domain.usecase

import com.example.core.util.Resource
import com.example.feature.recurringbill.domain.model.RecurringBill
import com.example.feature.recurringbill.domain.repository.RecurringBillRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRecurringBillsUseCase @Inject constructor(
    private val repository: RecurringBillRepository
) {
    operator fun invoke(apartmentId: String): Flow<Resource<List<RecurringBill>>> {
        return repository.getRecurringBills(apartmentId)
    }
}
