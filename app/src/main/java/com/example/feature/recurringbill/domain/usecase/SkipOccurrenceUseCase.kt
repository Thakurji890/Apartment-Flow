package com.example.feature.recurringbill.domain.usecase

import com.example.core.util.Resource
import com.example.feature.recurringbill.domain.repository.RecurringBillRepository
import javax.inject.Inject

class SkipOccurrenceUseCase @Inject constructor(
    private val repository: RecurringBillRepository
) {
    suspend operator fun invoke(billId: String, nextDueDate: Long): Resource<Unit> {
        return repository.skipOccurrence(billId, nextDueDate)
    }
}
