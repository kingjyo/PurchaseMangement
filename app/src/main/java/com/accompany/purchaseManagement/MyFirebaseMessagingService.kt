package com.accompany.purchaseManagement

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import android.util.Log

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // 데이터 메시지와 알림 메시지 구분
        if (remoteMessage.notification != null) {
            // 알림 메시지가 있을 경우
            Log.d("FCM", "알림 메시지 받음: ${remoteMessage.notification?.title}")
            showNotification(
                remoteMessage.notification?.title ?: "새 구매신청",
                remoteMessage.notification?.body ?: "새로운 구매 신청이 있습니다"
            )
        }

        // 데이터 메시지가 있을 경우
        if (remoteMessage.data.isNotEmpty()) {
            val title = remoteMessage.data["title"] ?: "새 구매신청"
            val body = remoteMessage.data["body"] ?: "새로운 구매 신청이 있습니다"
            Log.d("FCM", "데이터 메시지 받음: $title - $body")
            showNotification(title, body)
        }
    }

    private fun showNotification(title: String, body: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "purchase_requests"

        // 알림 채널 생성 (Android 8.0 이상에서는 필수)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "구매신청 알림",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // 알림 생성
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }
}
