package com.example.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.feature.expense.data.local.dao.ExpenseDao
import com.example.feature.expense.data.local.entity.ExpenseEntity

@Database(entities = [ExpenseEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
}
