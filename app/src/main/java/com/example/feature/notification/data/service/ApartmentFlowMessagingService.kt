package com.example.feature.notification.data.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.feature.auth.domain.repository.AuthRepository
import com.example.feature.notification.domain.model.FcmToken
import com.example.feature.notification.domain.repository.NotificationRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ApartmentFlowMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var authRepository: AuthRepository
    
    @Inject
    lateinit var notificationRepository: NotificationRepository

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        scope.launch {
            val user = authRepository.getCurrentUser()
            if (user != null) {
                val fcmToken = FcmToken(
                    token = token,
                    deviceId = android.provider.Settings.Secure.getString(contentResolver, android.provider.Settings.Secure.ANDROID_ID) ?: "unknown",
                    deviceModel = Build.MODEL
                )
                notificationRepository.registerFcmToken(user.uid, fcmToken)
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Parse data payload
        val data = remoteMessage.data
        val title = remoteMessage.notification?.title ?: data["title"] ?: "New Notification"
        val body = remoteMessage.notification?.body ?: data["body"] ?: "You have a new update."
        val deepLink = data["deepLink"]
        val channelId = data["channelId"] ?: "general_channel"

        sendNotification(title, body, deepLink, channelId)
    }

    private fun sendNotification(title: String, body: String, deepLink: String?, channelId: String) {
        val intent = if (!deepLink.isNullOrBlank()) {
            Intent(Intent.ACTION_VIEW, Uri.parse(deepLink)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        } else {
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        
        // Use basic launcher icon since we might not have a transparent notification icon setup yet
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = getChannelName(channelId)
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
    
    private fun getChannelName(channelId: String): String {
        return when (channelId) {
            "financial_channel" -> "Financial & Settlements"
            "chores_channel" -> "Chores"
            "apartment_channel" -> "Apartment Activity"
            "security_channel" -> "Security"
            else -> "General Notifications"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
