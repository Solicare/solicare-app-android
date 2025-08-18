package com.solicare.monitor.domain.usecase

import android.content.Context
import javax.inject.Inject

class HandleFcmTokenRegistrationUseCase @Inject constructor(
    private val context: Context
) {
    suspend operator fun invoke(token: String) {
        // TODO: 구현 내용 작성
    }
}
