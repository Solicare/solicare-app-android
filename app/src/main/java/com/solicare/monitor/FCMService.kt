package com.solicare.monitor

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FCMService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "새로운 FCM 토큰: $token")

        // SharedPreferences에서 저장된 전화번호 가져오기
        val sharedPref = getSharedPreferences("FCMPrefs", Context.MODE_PRIVATE)
        val phoneNumber = sharedPref.getString("phoneNumber", null)

        if (phoneNumber != null) {
            FCMTokenManager.sendTokenToServer(token, phoneNumber, object : FCMTokenManager.TokenRegistrationCallback {
                override fun onSuccess(message: String) {
                    Log.d(TAG, "토큰 갱신 성공: $message")
                    createNotificationChannel()
                    sendNotification("알림 등록 성공", message)
                }

                override fun onFailure(error: String) {
                    Log.e(TAG, "토큰 갱신 실패: $error")
                    createNotificationChannel()
                    sendNotification("알림 등록 실패", error)
                }
            })
        } else {
            Log.e(TAG, "저장된 전화번호가 없습니다.")
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "FCM 메시지 수신: ${remoteMessage.data}")

        // 알림 생성
        remoteMessage.notification?.let { notification ->
            createNotificationChannel()
            sendNotification(notification.title, notification.body)
        }

        // 데이터 메시지 처리
        if (remoteMessage.data.isNotEmpty()) {
            val title = remoteMessage.data["title"]
            val message = remoteMessage.data["message"]
            if (title != null && message != null) {
                createNotificationChannel()
                sendNotification(title, message)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "솔리케어 알림",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "솔리케어 앱의 알림을 표시합니다"
                enableLights(true)
                enableVibration(true)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendNotification(title: String?, messageBody: String?) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // TODO: 앱 아이콘으로 변경
            .setContentTitle(title ?: "솔리케어")
            .setContentText(messageBody ?: "")
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    companion object {
        private const val TAG = "FCMService"
        private const val CHANNEL_ID = "solicare_notification"
    }
}
