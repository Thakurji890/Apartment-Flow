package com.example.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.feature.expense.data.local.dao.ExpenseDao
import com.example.feature.expense.data.local.entity.ExpenseEntity
import com.example.core.sync.OutboxEntity
import com.example.core.sync.OutboxDao
import com.example.feature.roommate.data.local.dao.RoommateDao
import com.example.feature.roommate.data.local.entity.RoommateEntity

@Database(entities = [ExpenseEntity::class, OutboxEntity::class, RoommateEntity::class], version = 3, exportSchema = false)
@androidx.room.TypeConverters(SyncTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun outboxDao(): OutboxDao
    abstract fun roommateDao(): RoommateDao
}
