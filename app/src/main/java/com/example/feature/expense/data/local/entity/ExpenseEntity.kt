package com.example.feature.expense.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.feature.expense.domain.model.Expense

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey
    val expenseId: String,
    val apartmentId: String,
    val title: String,
    val description: String,
    val amount: Double,
    val currency: String,
    val categoryId: String,
    val paidBy: String,
    val createdBy: String,
    val expenseDate: Long,
    val createdAt: Long,
    val updatedAt: Long,
    val receiptUrl: String?,
    val notes: String,
    val tags: String, // Stored as comma-separated
    val isRecurring: Boolean,
    val status: String,
    val deleted: Boolean,
    val isRecurringExpense: Boolean,
    val recurringBillId: String?,
    val occurrenceId: String?,
    val isSynced: Boolean
) {
    fun toExpense(): Expense {
        return Expense(
            expenseId = expenseId,
            apartmentId = apartmentId,
            title = title,
            description = description,
            amount = amount,
            currency = currency,
            categoryId = categoryId,
            paidBy = paidBy,
            createdBy = createdBy,
            expenseDate = expenseDate,
            createdAt = createdAt,
            updatedAt = updatedAt,
            receiptUrl = receiptUrl,
            notes = notes,
            tags = if (tags.isNotBlank()) tags.split(",") else emptyList(),
            isRecurring = isRecurring,
            status = status,
            deleted = deleted,
            isRecurringExpense = isRecurringExpense,
            recurringBillId = recurringBillId,
            occurrenceId = occurrenceId,
            isSynced = isSynced
        )
    }

    companion object {
        fun fromExpense(expense: Expense): ExpenseEntity {
            return ExpenseEntity(
                expenseId = expense.expenseId,
                apartmentId = expense.apartmentId,
                title = expense.title,
                description = expense.description,
                amount = expense.amount,
                currency = expense.currency,
                categoryId = expense.categoryId,
                paidBy = expense.paidBy,
                createdBy = expense.createdBy,
                expenseDate = expense.expenseDate,
                createdAt = expense.createdAt,
                updatedAt = expense.updatedAt,
                receiptUrl = expense.receiptUrl,
                notes = expense.notes,
                tags = expense.tags.joinToString(","),
                isRecurring = expense.isRecurring,
                status = expense.status,
                deleted = expense.deleted,
                isRecurringExpense = expense.isRecurringExpense,
                recurringBillId = expense.recurringBillId,
                occurrenceId = expense.occurrenceId,
                isSynced = expense.isSynced
            )
        }
    }
}
