package com.solicare.monitor.domain.usecase

import android.content.Context
import com.google.firebase.messaging.RemoteMessage
import javax.inject.Inject

class HandleFcmMessageUseCase @Inject constructor(
    private val context: Context
) {
    suspend operator fun invoke(remoteMessage: RemoteMessage) {
        // TODO: 구현 내용 작성
    }
}
