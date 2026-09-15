package com.example.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.core.database.AppDatabase
import com.example.feature.expense.data.local.dao.ExpenseDao
import com.example.core.sync.OutboxDao
import com.example.feature.roommate.data.local.dao.RoommateDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `outbox` (`id` TEXT NOT NULL, `idempotencyKey` TEXT NOT NULL, `collectionPath` TEXT NOT NULL, `documentId` TEXT NOT NULL, `operationType` TEXT NOT NULL, `payload` TEXT NOT NULL, `status` TEXT NOT NULL, `errorReason` TEXT, `retryCount` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))"
            )
        }
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `roommates` (" +
                    "`id` TEXT NOT NULL, " +
                    "`name` TEXT NOT NULL, " +
                    "`email` TEXT NOT NULL, " +
                    "`balanceStatus` TEXT NOT NULL, " +
                    "`balanceAmount` REAL NOT NULL, " +
                    "`apartmentId` TEXT NOT NULL, " +
                    "`phoneNumber` TEXT NOT NULL, " +
                    "`photoUrl` TEXT, " +
                    "`isAdmin` INTEGER NOT NULL, " +
                    "`colorHex` INTEGER NOT NULL, " +
                    "`createdAt` INTEGER NOT NULL, " +
                    "`updatedAt` INTEGER NOT NULL, " +
                    "PRIMARY KEY(`id`))"
            )
        }
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "apartment_flow_db"
        )
        .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun provideExpenseDao(database: AppDatabase): ExpenseDao {
        return database.expenseDao()
    }

    @Provides
    fun provideOutboxDao(database: AppDatabase): OutboxDao {
        return database.outboxDao()
    }

    @Provides
    fun provideRoommateDao(database: AppDatabase): RoommateDao {
        return database.roommateDao()
    }
}
