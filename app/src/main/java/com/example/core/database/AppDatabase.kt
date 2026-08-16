package com.example.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.feature.expense.data.local.dao.ExpenseDao
import com.example.feature.expense.data.local.entity.ExpenseEntity
import com.example.core.sync.OutboxEntity
import com.example.core.sync.OutboxDao

@Database(entities = [ExpenseEntity::class, OutboxEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun outboxDao(): OutboxDao
}
