package com.example.feature.expense.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.feature.expense.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Query("SELECT * FROM expenses WHERE apartmentId = :apartmentId AND deleted = 0")
    fun getExpensesForApartment(apartmentId: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE expenseId = :expenseId")
    fun getExpenseById(expenseId: String): Flow<ExpenseEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(expenses: List<ExpenseEntity>)

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Query("UPDATE expenses SET deleted = 1 WHERE expenseId = :expenseId")
    suspend fun deleteExpenseLocally(expenseId: String)

    @Query("UPDATE expenses SET status = :status, isSynced = :isSynced, updatedAt = :updatedAt WHERE expenseId = :expenseId")
    suspend fun updateExpenseStatus(expenseId: String, status: String, isSynced: Boolean = false, updatedAt: Long = System.currentTimeMillis())
    
    @Query("SELECT * FROM expenses WHERE isSynced = 0")
    suspend fun getUnsyncedExpenses(): List<ExpenseEntity>
}
