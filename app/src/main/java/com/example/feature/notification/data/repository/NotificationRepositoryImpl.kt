package com.example.feature.notification.data.repository

import com.example.core.util.Resource
import com.example.feature.notification.data.dto.NotificationDto
import com.example.feature.notification.data.dto.NotificationPreferencesDto
import com.example.feature.notification.data.mapper.toDomain
import com.example.feature.notification.data.mapper.toDto
import com.example.feature.notification.domain.model.AppNotification
import com.example.feature.notification.domain.model.FcmToken
import com.example.feature.notification.domain.model.NotificationPreferences
import com.example.feature.notification.domain.repository.NotificationRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : NotificationRepository {

    override fun getNotifications(userId: String): Flow<Resource<List<AppNotification>>> = callbackFlow {
        val listenerRegistration = firestore.collection("users")
            .document(userId)
            .collection("notifications")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(100)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed to fetch notifications"))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val notifications = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(NotificationDto::class.java)?.toDomain()
                    }
                    trySend(Resource.Success(notifications))
                }
            }

        awaitClose {
            listenerRegistration.remove()
        }
    }

    override fun getUnreadCount(userId: String): Flow<Int> = callbackFlow {
        val listenerRegistration = firestore.collection("users")
            .document(userId)
            .collection("notifications")
            .whereEqualTo("isRead", false)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(0)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.size())
                }
            }
        awaitClose {
            listenerRegistration.remove()
        }
    }

    override suspend fun markAsRead(userId: String, notificationId: String): Resource<Unit> {
        return try {
            firestore.collection("users")
                .document(userId)
                .collection("notifications")
                .document(notificationId)
                .update(
                    mapOf(
                        "isRead" to true,
                        "readAt" to System.currentTimeMillis()
                    )
                ).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to mark as read")
        }
    }

    override suspend fun markAllAsRead(userId: String): Resource<Unit> {
        return try {
            val unreadDocs = firestore.collection("users")
                .document(userId)
                .collection("notifications")
                .whereEqualTo("isRead", false)
                .get()
                .await()
            
            val batch = firestore.batch()
            val now = System.currentTimeMillis()
            for (doc in unreadDocs.documents) {
                batch.update(doc.reference, mapOf("isRead" to true, "readAt" to now))
            }
            batch.commit().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to mark all as read")
        }
    }

    override suspend fun deleteNotification(userId: String, notificationId: String): Resource<Unit> {
        return try {
            firestore.collection("users")
                .document(userId)
                .collection("notifications")
                .document(notificationId)
                .delete()
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete notification")
        }
    }

    override suspend fun cleanupExpiredNotifications(userId: String): Resource<Unit> {
        return try {
            val now = System.currentTimeMillis()
            val expiredDocs = firestore.collection("users")
                .document(userId)
                .collection("notifications")
                .whereLessThan("expiresAt", now)
                .get()
                .await()
            
            if (!expiredDocs.isEmpty) {
                val batch = firestore.batch()
                for (doc in expiredDocs.documents) {
                    batch.delete(doc.reference)
                }
                batch.commit().await()
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to clean up notifications")
        }
    }

    override suspend fun getPreferences(userId: String): Resource<NotificationPreferences> {
        return try {
            val doc = firestore.collection("users")
                .document(userId)
                .collection("notificationPreferences")
                .document("default")
                .get()
                .await()
            
            if (doc.exists()) {
                val prefs = doc.toObject(NotificationPreferencesDto::class.java)?.toDomain()
                Resource.Success(prefs ?: NotificationPreferences())
            } else {
                // Initialize default
                val defaultPrefs = NotificationPreferences()
                updatePreferences(userId, defaultPrefs)
                Resource.Success(defaultPrefs)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to load preferences")
        }
    }

    override suspend fun updatePreferences(
        userId: String,
        preferences: NotificationPreferences
    ): Resource<Unit> {
        return try {
            firestore.collection("users")
                .document(userId)
                .collection("notificationPreferences")
                .document("default")
                .set(preferences.toDto())
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update preferences")
        }
    }

    override suspend fun registerFcmToken(userId: String, token: FcmToken): Resource<Unit> {
        return try {
            firestore.collection("users")
                .document(userId)
                .collection("fcmTokens")
                .document(token.token)
                .set(token.toDto())
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to register FCM token")
        }
    }

    override suspend fun unregisterFcmToken(userId: String, tokenString: String): Resource<Unit> {
        return try {
            firestore.collection("users")
                .document(userId)
                .collection("fcmTokens")
                .document(tokenString)
                .delete()
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to unregister FCM token")
        }
    }
}
