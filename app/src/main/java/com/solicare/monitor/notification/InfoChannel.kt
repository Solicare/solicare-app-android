package com.solicare.monitor.notification

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat

object InfoChannel {
    const val CHANNEL_ID = "info_channel"
    private const val CHANNEL_NAME = "정보 알림"
    private const val CHANNEL_DESC = "일반 정보성 알림을 위한 채널입니다."

    fun register(context: Context) {
        NotificationHelper.createNotificationChannel(
            context,
            CHANNEL_ID,
            CHANNEL_NAME,
            CHANNEL_DESC
        )
    }

    fun send(context: Context, message: String, title: String = "안내", success: Boolean = true) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(if (success) android.R.drawable.ic_dialog_info else android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}

