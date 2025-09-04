package com.solicare.monitor.presentation.notification

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.solicare.monitor.R

object InfoChannel {
    const val CHANNEL_ID = "info_channel"

    fun register(context: Context) {
        val channelName = context.getString(R.string.info_channel_name)
        val channelDesc = context.getString(R.string.info_channel_desc)
        NotificationHelper.createNotificationChannel(
            context,
            CHANNEL_ID,
            channelName,
            channelDesc
        )
    }

    fun send(
        context: Context,
        message: String,
        title: String,
        iconResId: Int = R.drawable.ic_notification_info
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(iconResId)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
