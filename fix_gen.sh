cat << 'INNER' > app/src/main/java/com/example/feature/recurringbill/domain/usecase/GenerateBillExpenseUseCase.kt
package com.example.feature.recurringbill.domain.usecase

import com.example.core.util.Resource
import com.example.feature.expensesplit.domain.repository.ExpenseSplitRepository
import com.example.feature.expensesplit.domain.usecase.CalculateDebtsUseCase
import com.example.feature.expensesplit.domain.usecase.CalculateSplitsUseCase
import com.example.feature.recurringbill.domain.model.RecurringBill
import com.example.feature.recurringbill.domain.repository.RecurringBillRepository
import com.example.feature.expensesplit.domain.model.SplitType
import com.example.feature.expensesplit.domain.model.Payment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class GenerateBillExpenseUseCase @Inject constructor(
    private val recurringBillRepository: RecurringBillRepository,
    private val expenseSplitRepository: ExpenseSplitRepository,
    private val calculateSplitsUseCase: CalculateSplitsUseCase,
    private val calculateDebtsUseCase: CalculateDebtsUseCase
) {
    suspend operator fun invoke(
        bill: RecurringBill,
        amount: Double? = null,
        expenseDate: Long? = null
    ): Resource<Unit> {
        val actualAmount = amount ?: bill.expectedAmount
        val actualDate = expenseDate ?: bill.nextDueDate
        
        val formatter = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val dateString = formatter.format(Date(bill.nextDueDate))
        val occurrenceId = "${bill.id}_$dateString"
        
        val result = recurringBillRepository.generateExpenseForBill(
            bill = bill,
            occurrenceId = occurrenceId,
            amount = actualAmount,
            actualDate = actualDate
        )
        
        if (result is Resource.Success) {
            val expenseId = result.data
            if (expenseId != null) {
                val globalSplitType = bill.splits.firstOrNull()?.type ?: SplitType.EQUAL
                val calculatedSplits = calculateSplitsUseCase(actualAmount, bill.splits, globalSplitType)
                
                val payments = listOf(Payment(userId = bill.paidBy, amount = actualAmount))
                val calculatedDebts = calculateDebtsUseCase(
                    expenseId = expenseId,
                    apartmentId = bill.apartmentId,
                    payments = payments,
                    splits = calculatedSplits
                )
                
                val splitResult = expenseSplitRepository.saveExpenseWithSplits(
                    expenseId = expenseId,
                    apartmentId = bill.apartmentId,
                    splits = calculatedSplits,
                    debts = calculatedDebts
                )
                if (splitResult is Resource.Error) {
                    return Resource.Error(splitResult.message ?: "Failed to save splits for generated expense")
                }
            }
            return Resource.Success(Unit)
        }
        
        return Resource.Error(result.message ?: "Failed to generate expense")
    }
}
INNER
