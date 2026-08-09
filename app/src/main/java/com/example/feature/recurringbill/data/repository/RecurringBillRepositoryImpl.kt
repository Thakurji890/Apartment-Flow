package com.example.feature.recurringbill.data.repository

import com.example.core.util.Resource
import com.example.feature.expense.domain.model.Expense
import com.example.feature.recurringbill.domain.model.RecurringBill
import com.example.feature.recurringbill.domain.repository.RecurringBillRepository
import com.example.feature.expensesplit.domain.repository.ExpenseSplitRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class RecurringBillRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val expenseSplitRepository: ExpenseSplitRepository
) : RecurringBillRepository {
    private val recurringBillsCollection = firestore.collection("recurringBills")

    override fun getRecurringBills(apartmentId: String): Flow<Resource<List<RecurringBill>>> = callbackFlow {
        val listener = recurringBillsCollection
            .whereEqualTo("apartmentId", apartmentId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Unknown error"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val bills = snapshot.toObjects(RecurringBill::class.java)
                    trySend(Resource.Success(bills))
                }
            }
        awaitClose { listener.remove() }
    }

    override fun getRecurringBill(billId: String): Flow<Resource<RecurringBill>> = callbackFlow {
        val listener = recurringBillsCollection.document(billId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Unknown error"))
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val bill = snapshot.toObject(RecurringBill::class.java)
                    if (bill != null) {
                        trySend(Resource.Success(bill))
                    } else {
                        trySend(Resource.Error("Failed to parse bill"))
                    }
                } else {
                    trySend(Resource.Error("Bill not found"))
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun insertRecurringBill(bill: RecurringBill): Resource<Unit> {
        return try {
            val newBill = bill.copy(
                id = if (bill.id.isBlank()) UUID.randomUUID().toString() else bill.id,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            recurringBillsCollection.document(newBill.id).set(newBill).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to insert bill")
        }
    }

    override suspend fun updateRecurringBill(bill: RecurringBill): Resource<Unit> {
        return try {
            val updatedBill = bill.copy(updatedAt = System.currentTimeMillis())
            recurringBillsCollection.document(updatedBill.id).set(updatedBill).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update bill")
        }
    }

    override suspend fun deleteRecurringBill(billId: String): Resource<Unit> {
        return try {
            recurringBillsCollection.document(billId).delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to delete bill")
        }
    }

    override suspend fun generateExpenseForBill(
        bill: RecurringBill,
        occurrenceId: String,
        amount: Double,
        actualDate: Long
    ): Resource<String> {
        return try {
            val expenseId = UUID.randomUUID().toString()
            val newExpense = Expense(
                expenseId = expenseId,
                apartmentId = bill.apartmentId,
                title = bill.name,
                description = "Auto-generated from ${bill.name}",
                amount = amount,
                currency = bill.currency,
                categoryId = bill.categoryId,
                paidBy = bill.paidBy,
                createdBy = "system",
                expenseDate = actualDate,
                isRecurringExpense = true,
                recurringBillId = bill.id,
                occurrenceId = occurrenceId,
                isSynced = true
            )
            
            // Execute in transaction to ensure idempotency
            firestore.runTransaction { transaction ->
                // Check if occurrence already exists
                val occurrencesRef = firestore.collection("generatedOccurrences").document(occurrenceId)
                val occurrenceSnapshot = transaction.get(occurrencesRef)
                
                if (occurrenceSnapshot.exists()) {
                    throw Exception("Expense already generated for this occurrence")
                }
                
                // Write occurrence lock
                val occurrenceData = mapOf(
                    "billId" to bill.id,
                    "expenseId" to expenseId,
                    "generatedAt" to System.currentTimeMillis()
                )
                transaction.set(occurrencesRef, occurrenceData)
                
                // Save Expense
                val expenseRef = firestore.collection("expenses").document(expenseId)
                transaction.set(expenseRef, newExpense)
                
                // Update next due date on recurring bill
                val billRef = recurringBillsCollection.document(bill.id)
                val nextDate = calculateNextDueDate(bill.frequency, bill.nextDueDate)
                transaction.update(billRef, "nextDueDate", nextDate)
                transaction.update(billRef, "lastGeneratedDate", System.currentTimeMillis())
                transaction.update(billRef, "updatedAt", System.currentTimeMillis())
                
                // For splits, we normally save them separately using expenseSplitRepository
                // But in a transaction, we should just write them if possible, or do it after.
                // We'll let expenseSplitRepository handle splits/debts immediately after this transaction
                // in the UseCase. 
            }.await()
            
            // Transaction succeeded, meaning idempotency checked out
            Resource.Success(expenseId)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to generate expense")
        }
    }
    
    override suspend fun skipOccurrence(billId: String, nextDueDate: Long): Resource<Unit> {
        return try {
            firestore.runTransaction { transaction ->
                val billRef = recurringBillsCollection.document(billId)
                val snapshot = transaction.get(billRef)
                if (snapshot.exists()) {
                    val bill = snapshot.toObject(RecurringBill::class.java)
                    if (bill != null && bill.nextDueDate == nextDueDate) {
                        val nextDate = calculateNextDueDate(bill.frequency, bill.nextDueDate)
                        transaction.update(billRef, "nextDueDate", nextDate)
                        transaction.update(billRef, "updatedAt", System.currentTimeMillis())
                    }
                }
            }.await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to skip occurrence")
        }
    }
    
    private fun calculateNextDueDate(frequency: com.example.feature.recurringbill.domain.model.BillFrequency, currentDueDate: Long): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = currentDueDate
        
        when (frequency) {
            com.example.feature.recurringbill.domain.model.BillFrequency.WEEKLY -> calendar.add(java.util.Calendar.WEEK_OF_YEAR, 1)
            com.example.feature.recurringbill.domain.model.BillFrequency.BIWEEKLY -> calendar.add(java.util.Calendar.WEEK_OF_YEAR, 2)
            com.example.feature.recurringbill.domain.model.BillFrequency.MONTHLY -> calendar.add(java.util.Calendar.MONTH, 1)
            com.example.feature.recurringbill.domain.model.BillFrequency.EVERY_2_MONTHS -> calendar.add(java.util.Calendar.MONTH, 2)
            com.example.feature.recurringbill.domain.model.BillFrequency.QUARTERLY -> calendar.add(java.util.Calendar.MONTH, 3)
            com.example.feature.recurringbill.domain.model.BillFrequency.HALF_YEARLY -> calendar.add(java.util.Calendar.MONTH, 6)
            com.example.feature.recurringbill.domain.model.BillFrequency.YEARLY -> calendar.add(java.util.Calendar.YEAR, 1)
        }
        
        return calendar.timeInMillis
    }
}
