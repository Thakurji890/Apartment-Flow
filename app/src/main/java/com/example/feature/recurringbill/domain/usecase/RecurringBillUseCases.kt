package com.example.feature.recurringbill.domain.usecase

import javax.inject.Inject

data class RecurringBillUseCases @Inject constructor(
    val getRecurringBills: GetRecurringBillsUseCase,
    val getRecurringBill: GetRecurringBillUseCase,
    val createRecurringBill: CreateRecurringBillUseCase,
    val updateRecurringBill: UpdateRecurringBillUseCase,
    val deleteRecurringBill: DeleteRecurringBillUseCase,
    val generateBillExpense: GenerateBillExpenseUseCase,
    val skipOccurrence: SkipOccurrenceUseCase
)
