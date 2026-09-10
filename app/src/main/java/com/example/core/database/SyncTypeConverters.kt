package com.example.core.database

import androidx.room.TypeConverter
import com.example.core.sync.OperationType
import com.example.core.sync.SyncStatus

class SyncTypeConverters {
    @TypeConverter
    fun fromOperationType(value: OperationType): String = value.name

    @TypeConverter
    fun toOperationType(value: String): OperationType = OperationType.valueOf(value)

    @TypeConverter
    fun fromSyncStatus(value: SyncStatus): String = value.name

    @TypeConverter
    fun toSyncStatus(value: String): SyncStatus = SyncStatus.valueOf(value)
}
