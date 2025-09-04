package com.solicare.monitor.presentation.notification

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.solicare.monitor.R

object AlertChannel {
    const val CHANNEL_ID = "alert_channel"

    fun register(context: Context) {
        val channelName = context.getString(R.string.alert_channel_name)
        val channelDesc = context.getString(R.string.alert_channel_desc)
        NotificationHelper.createNotificationChannel(
            context,
            CHANNEL_ID,
            channelName,
            channelDesc
        )
    }

    fun send(
        context: Context,
        title: String,
        message: String,
        iconResInt: Int = R.drawable.ic_notification_alert
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(iconResInt)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
