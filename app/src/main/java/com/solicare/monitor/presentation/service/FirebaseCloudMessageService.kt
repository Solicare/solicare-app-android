package com.solicare.monitor.presentation.service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.solicare.monitor.data.prefs.FcmPrefs
import com.solicare.monitor.domain.usecase.FcmNotificationUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FirebaseCloudMessageService : FirebaseMessagingService() {
    companion object {
        private const val TAG = "FCMService"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token received: $token")
        FcmPrefs(applicationContext).saveToken(token)
        // 서버 등록 및 InfoChannel 알림은 Activity 등에서 권한 허용 후 처리
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(
            TAG,
            "Received FCM message. Data payload: ${remoteMessage.data}, From: ${remoteMessage.from}"
        )
        CoroutineScope(Dispatchers.IO).launch {
            val useCase = FcmNotificationUseCase(applicationContext)
            useCase(remoteMessage)
        }
    }
}
