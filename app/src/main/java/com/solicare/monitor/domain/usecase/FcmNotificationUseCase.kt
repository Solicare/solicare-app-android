package com.solicare.monitor.domain.usecase

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.RemoteMessage

class FcmNotificationUseCase(
    private val context: Context
) {
    operator fun invoke(remoteMessage: RemoteMessage) {
        Log.e("FcmNotificationUseCase", "Message: ${remoteMessage.notification}")
        val pushType = remoteMessage.notification?.channelId
        val title: String = remoteMessage.notification?.title
            ?: context.getString(com.solicare.monitor.R.string.fcm_message_received_title)
        val body: String = remoteMessage.notification?.body
            ?: context.getString(com.solicare.monitor.R.string.fcm_message_received_message)

        if (pushType.equals("alert_channel", ignoreCase = true)) {
            com.solicare.monitor.presentation.notification.AlertChannel.send(
                context = context,
                title = title,
                message = body
            )
        } else {
            com.solicare.monitor.presentation.notification.InfoChannel.send(
                context = context,
                title = title,
                message = body
            )
        }
    }
}