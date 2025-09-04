package com.solicare.monitor.domain.repository

interface DeviceRepository {
    /**
     * FCM 토큰을 서버에 등록합니다. (PUT /api/firebase/fcm/{token})
     * @return true(성공), false(실패)
     */
    suspend fun registerFcmToken(token: String): Boolean

    /**
     * FCM 토큰을 서버에서 해제합니다. (DELETE /api/firebase/fcm/{token})
     * @return true(성공), false(실패)
     */
    suspend fun unregisterFcmToken(token: String): Boolean
}
