package com.solicare.monitor.domain.usecase

import android.content.Context
import com.google.firebase.messaging.RemoteMessage

class FcmNotificationUseCase(
    private val context: Context
) {
    operator fun invoke(remoteMessage: RemoteMessage) {
        val data = remoteMessage.data
        val pushType = data["type"] ?: "INFO"
        val title =
            data["title"]
                ?: context.getString(com.solicare.monitor.R.string.fcm_message_received_title)
        val body =
            data["body"]
                ?: context.getString(com.solicare.monitor.R.string.fcm_message_received_message)

        if (pushType.equals("ALERT", ignoreCase = true)) {
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