package com.example.feature.expense.data.repository

import com.example.core.util.Resource
import com.example.feature.expense.data.local.dao.ExpenseDao
import com.example.feature.expense.data.local.entity.ExpenseEntity
import com.example.feature.expense.domain.model.Expense
import com.example.feature.expense.domain.model.ExpenseFilter
import com.example.feature.expense.domain.model.ExpenseSortOrder
import com.example.feature.expense.domain.repository.ExpenseRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class ExpenseRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val dao: ExpenseDao
) : ExpenseRepository {

    private val expensesCollection = firestore.collection("expenses")

    override fun getExpenses(
        apartmentId: String,
        searchQuery: String,
        filter: ExpenseFilter,
        sortOrder: ExpenseSortOrder
    ): Flow<Resource<List<Expense>>> {
        return dao.getExpensesForApartment(apartmentId)
            .map { entities ->
                var expenses = entities.map { it.toExpense() }

                // Apply search
                if (searchQuery.isNotBlank()) {
                    expenses = expenses.filter {
                        it.title.contains(searchQuery, ignoreCase = true) ||
                        it.description.contains(searchQuery, ignoreCase = true)
                    }
                }

                // Apply filters
                filter.categoryId?.let { catId ->
                    if (catId != "all") {
                        expenses = expenses.filter { it.categoryId == catId }
                    }
                }
                filter.paidByUserId?.let { userId ->
                    expenses = expenses.filter { it.paidBy == userId }
                }
                filter.startDate?.let { start ->
                    expenses = expenses.filter { it.expenseDate >= start }
                }
                filter.endDate?.let { end ->
                    expenses = expenses.filter { it.expenseDate <= end }
                }
                filter.minAmount?.let { min ->
                    expenses = expenses.filter { it.amount >= min }
                }
                filter.maxAmount?.let { max ->
                    expenses = expenses.filter { it.amount <= max }
                }

                // Apply sorting
                expenses = when (sortOrder) {
                    ExpenseSortOrder.NEWEST_FIRST -> expenses.sortedByDescending { it.expenseDate }
                    ExpenseSortOrder.OLDEST_FIRST -> expenses.sortedBy { it.expenseDate }
                    ExpenseSortOrder.HIGHEST_AMOUNT -> expenses.sortedByDescending { it.amount }
                    ExpenseSortOrder.LOWEST_AMOUNT -> expenses.sortedBy { it.amount }
                    ExpenseSortOrder.ALPHABETICAL -> expenses.sortedBy { it.title.lowercase() }
                }

                Resource.Success(expenses) as Resource<List<Expense>>
            }
            .catch { e ->
                emit(Resource.Error(e.localizedMessage ?: "Failed to get expenses"))
            }
    }

    override fun getExpense(expenseId: String): Flow<Resource<Expense>> {
        return dao.getExpenseById(expenseId)
            .map { entity ->
                if (entity != null) {
                    Resource.Success(entity.toExpense()) as Resource<Expense>
                } else {
                    Resource.Error("Expense not found")
                }
            }
            .catch { e ->
                emit(Resource.Error(e.localizedMessage ?: "Failed to get expense"))
            }
    }

    override suspend fun insertExpense(expense: Expense): Resource<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            val newExpense = expense.copy(
                expenseId = if (expense.expenseId.isBlank()) UUID.randomUUID().toString() else expense.expenseId,
                createdBy = userId,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                isSynced = false
            )
            
            // Save locally first
            dao.insertExpense(ExpenseEntity.fromExpense(newExpense))
            
            // Try pushing to remote
            try {
                expensesCollection.document(newExpense.expenseId).set(newExpense).await()
                // If remote succeeds, update synced status
                dao.updateExpense(ExpenseEntity.fromExpense(newExpense.copy(isSynced = true)))
            } catch (e: Exception) {
                // Ignore remote failure, it's saved locally
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to create expense")
        }
    }

    override suspend fun updateExpense(expense: Expense): Resource<Unit> {
        return try {
            val updatedExpense = expense.copy(
                updatedAt = System.currentTimeMillis(),
                isSynced = false
            )
            
            // Save locally first
            dao.updateExpense(ExpenseEntity.fromExpense(updatedExpense))
            
            // Try pushing to remote
            try {
                expensesCollection.document(updatedExpense.expenseId).set(updatedExpense).await()
                dao.updateExpense(ExpenseEntity.fromExpense(updatedExpense.copy(isSynced = true)))
            } catch (e: Exception) {
                // Ignore remote failure
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update expense")
        }
    }

    override suspend fun deleteExpense(expenseId: String): Resource<Unit> {
        return try {
            // Soft delete locally
            dao.deleteExpenseLocally(expenseId)
            
            // Try pushing to remote
            try {
                expensesCollection.document(expenseId).update("deleted", true).await()
            } catch (e: Exception) {
                // Ignore remote failure, will need sync later
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to delete expense")
        }
    }

    override suspend fun syncExpenses(apartmentId: String): Resource<Unit> {
        return try {
            // 1. Fetch remote changes
            val snapshot = expensesCollection
                .whereEqualTo("apartmentId", apartmentId)
                .get()
                .await()
            
            val remoteExpenses = snapshot.toObjects(Expense::class.java).map { 
                it.copy(isSynced = true)
            }
            
            // 2. Get local unsynced changes
            val localUnsynced = dao.getUnsyncedExpenses()
            
            // 3. Update local DB with remote data
            if (remoteExpenses.isNotEmpty()) {
                val entities = remoteExpenses.map { ExpenseEntity.fromExpense(it) }
                dao.insertExpenses(entities)
            }
            
            // 4. Push local unsynced changes to remote
            for (unsyncedEntity in localUnsynced) {
                try {
                    val expense = unsyncedEntity.toExpense()
                    expensesCollection.document(expense.expenseId).set(expense).await()
                    dao.updateExpense(unsyncedEntity.copy(isSynced = true))
                } catch (e: Exception) {
                    // Stop syncing if network fails
                    break 
                }
            }
            
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to sync expenses")
        }
    }
}
