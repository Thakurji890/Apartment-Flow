package com.example.core.sync

import java.util.UUID
import javax.inject.Inject

class OutboxRepository @Inject constructor(
    private val outboxDao: OutboxDao,
    private val syncManager: SyncManager
) {
    suspend fun enqueueOperation(
        collectionPath: String,
        documentId: String,
        operationType: OperationType,
        payload: String
    ) {
        val outboxEntity = OutboxEntity(
            id = UUID.randomUUID().toString(),
            idempotencyKey = "${documentId}_${System.currentTimeMillis()}", // Simple idempotency key
            collectionPath = collectionPath,
            documentId = documentId,
            operationType = operationType,
            payload = payload
        )
        outboxDao.insert(outboxEntity)
        syncManager.scheduleSync()
    }
}
