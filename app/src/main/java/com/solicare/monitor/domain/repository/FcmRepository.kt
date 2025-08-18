package com.solicare.monitor.domain.repository

interface FcmRepository {
    /**
     * /fcm/register/device
     * 전화번호, fcm_token만 보내서 디바이스를 DB에 등록 (계정과 연결 X)
     * @return true(성공), false(실패)
     */
    suspend fun registerDevice(token: String, phoneNumber: String): Boolean

    /**
     * /fcm/register/user
     * JWT토큰은 Authorization 헤더로, fcm_token은 body로 보내서 계정과 등록해둔 기기를 연결
     * @return true(성공), false(실패)
     */
    suspend fun registerUser(jwtToken: String, fcmToken: String): Boolean

    /**
     * /fcm/validate
     * jwt토큰과 fcm_token을 보내서 계정에 정상적으로 연결되어 있는지 확인
     * @return true(정상 연결), false(아님)
     */
    suspend fun validateFCMToken(jwtToken: String, fcmToken: String): Boolean
}

