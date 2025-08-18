package com.solicare.monitor.presentation.service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.solicare.monitor.domain.usecase.HandleFcmMessageUseCase
import com.solicare.monitor.domain.usecase.HandleFcmTokenRegistrationUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class FCMService : FirebaseMessagingService() {
    @Inject
    lateinit var handleFcmMessageUseCase: HandleFcmMessageUseCase

    @Inject
    lateinit var handleFcmTokenRegistrationUseCase: HandleFcmTokenRegistrationUseCase

    companion object {
        private const val TAG = "FCMService"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "새로운 FCM 토큰: $token")
        CoroutineScope(Dispatchers.IO).launch {
            handleFcmTokenRegistrationUseCase(token)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "FCM 메시지 수신: ${remoteMessage.data}")
        CoroutineScope(Dispatchers.IO).launch {
            handleFcmMessageUseCase(remoteMessage)
        }
    }

    override fun onDeletedMessages() {
        super.onDeletedMessages()
        Log.d(TAG, "FCM 메시지 일부가 삭제됨")
    }
}
