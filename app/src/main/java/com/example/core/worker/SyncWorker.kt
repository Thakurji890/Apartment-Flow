package com.example.core.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.core.sync.OperationType
import com.example.core.sync.OutboxDao
import com.example.core.sync.OutboxEntity
import com.example.core.sync.SyncStatus
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.tasks.await

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val outboxDao: OutboxDao,
    private val firestore: FirebaseFirestore
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val pendingOperations = outboxDao.getPendingOperations()
        
        if (pendingOperations.isEmpty()) {
            return Result.success()
        }

        var hasFailures = false

        for (operation in pendingOperations) {
            try {
                // Mark as syncing
                outboxDao.update(operation.copy(status = SyncStatus.SYNCING))

                // Process based on operation type
                when (operation.operationType) {
                    OperationType.CREATE -> processCreate(operation)
                    OperationType.UPDATE -> processUpdate(operation)
                    OperationType.DELETE -> processDelete(operation)
                }

                // If successful, remove from outbox
                outboxDao.deleteById(operation.id)
            } catch (e: Exception) {
                hasFailures = true
                val retryCount = operation.retryCount + 1
                
                // If it fails with permission denied or similar permanent error, we might want to mark FAILED instead
                // For simplicity, we'll keep retrying up to 5 times
                if (retryCount >= 5) {
                    outboxDao.update(
                        operation.copy(
                            status = SyncStatus.FAILED,
                            errorReason = e.message ?: "Unknown error",
                            retryCount = retryCount,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                } else {
                    outboxDao.update(
                        operation.copy(
                            status = SyncStatus.PENDING,
                            errorReason = e.message ?: "Unknown error",
                            retryCount = retryCount,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                }
            }
        }

        return if (hasFailures) Result.retry() else Result.success()
    }

    private suspend fun processCreate(operation: OutboxEntity) {
        val typeMap = parsePayload(operation.payload)
        firestore.collection(operation.collectionPath)
            .document(operation.documentId)
            .set(typeMap)
            .await()
    }

    private suspend fun processUpdate(operation: OutboxEntity) {
        val typeMap = parsePayload(operation.payload)
        firestore.collection(operation.collectionPath)
            .document(operation.documentId)
            .set(typeMap, SetOptions.merge())
            .await()
    }

    private suspend fun processDelete(operation: OutboxEntity) {
        firestore.collection(operation.collectionPath)
            .document(operation.documentId)
            .delete()
            .await()
    }

    // A simple JSON parser since we are storing standard DTOs. 
    // In a real app we might use Moshi or Gson injected here. 
    // We'll use Moshi here as the project already uses it for Retrofit.
    private fun parsePayload(payload: String): Map<String, Any> {
        // As a generic fallback when Moshi isn't directly available without re-configuring the entire app:
        // We will store our payload via Kotlinx Serialization or Moshi later.
        // For now, let's assume Moshi is used, or we can use org.json.JSONObject.
        val jsonObject = org.json.JSONObject(payload)
        val map = mutableMapOf<String, Any>()
        val keys = jsonObject.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val value = jsonObject.get(key)
            if (value != org.json.JSONObject.NULL) {
                map[key] = value
            }
        }
        return map
    }
}
