package com.solicare.monitor.presentation.notification

import android.R
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat

object AlertChannel {
    const val CHANNEL_ID = "alert_channel"
    private const val CHANNEL_NAME = "경고 알림"
    private const val CHANNEL_DESC = "중요/경고 알림을 위한 채널입니다."

    fun register(context: Context) {
        NotificationHelper.createNotificationChannel(
            context,
            CHANNEL_ID,
            CHANNEL_NAME,
            CHANNEL_DESC
        )
    }

    fun send(context: Context, message: String, title: String = "경고", success: Boolean = false) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(if (success) R.drawable.ic_dialog_info else R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}

