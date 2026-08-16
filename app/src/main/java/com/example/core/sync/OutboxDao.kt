package com.example.core.sync

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface OutboxDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(outboxEntity: OutboxEntity)

    @Update
    suspend fun update(outboxEntity: OutboxEntity)

    @Query("SELECT * FROM outbox WHERE status = 'PENDING' OR status = 'FAILED' ORDER BY createdAt ASC")
    suspend fun getPendingOperations(): List<OutboxEntity>

    @Query("SELECT * FROM outbox WHERE collectionPath = :collectionPath AND documentId = :documentId ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestOperationForDocument(collectionPath: String, documentId: String): OutboxEntity?

    @Query("DELETE FROM outbox WHERE id = :id")
    suspend fun deleteById(id: String)
    
    @Query("DELETE FROM outbox WHERE status = 'SUCCESS'")
    suspend fun clearSuccessful()
    
    @Query("DELETE FROM outbox")
    suspend fun clearAll()
}
