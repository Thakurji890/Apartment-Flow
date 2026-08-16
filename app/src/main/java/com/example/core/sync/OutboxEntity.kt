package com.example.core.sync

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class OperationType {
    CREATE,
    UPDATE,
    DELETE
}

enum class SyncStatus {
    PENDING,
    SYNCING,
    FAILED,
    CONFLICT
}

@Entity(tableName = "outbox")
data class OutboxEntity(
    @PrimaryKey
    val id: String, // UUID
    val idempotencyKey: String,
    val collectionPath: String,
    val documentId: String,
    val operationType: OperationType,
    val payload: String, // JSON representation of data
    val status: SyncStatus = SyncStatus.PENDING,
    val errorReason: String? = null,
    val retryCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
