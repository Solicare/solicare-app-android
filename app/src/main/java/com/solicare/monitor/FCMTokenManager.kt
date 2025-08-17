package com.solicare.monitor

import android.util.Log
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

object FCMTokenManager {
    private const val TAG = "FCMTokenManager"
    private const val SERVER_URL = "http://api.solicare.kro.kr/fcm/register"

    private val client = OkHttpClient()
    private val JSON = "application/json; charset=utf-8".toMediaType()

    interface TokenRegistrationCallback {
        fun onSuccess(message: String)
        fun onFailure(error: String)
    }

    private fun formatPhoneNumber(phoneNumber: String): String {
        // 전화번호에서 하이픈 제거
        val cleaned = phoneNumber.replace("-", "")

        // 11자리 전화번호인 경우 (01012345678)
        return when (cleaned.length) {
            11 -> "${cleaned.substring(0, 3)}-${cleaned.substring(3, 7)}-${cleaned.substring(7)}"
            // 10자리 전화번호인 경우 (0101234567)
            10 -> "${cleaned.substring(0, 3)}-${cleaned.substring(3, 6)}-${cleaned.substring(6)}"
            else -> cleaned // 다른 형식은 그대로 반환
        }
    }

    fun sendTokenToServer(token: String, phoneNumber: String, callback: TokenRegistrationCallback) {
        Log.d(TAG, "토큰 서버 전송 시작: $token, 전화번호: $phoneNumber")

        val formattedPhoneNumber = formatPhoneNumber(phoneNumber)
        val json = JSONObject().apply {
            put("token", token)
            put("phoneNumber", formattedPhoneNumber)
        }

        val requestBody = json.toString()
        Log.d(TAG, "전송할 데이터: $requestBody")

        val request = Request.Builder()
            .url(SERVER_URL)
            .post(requestBody.toRequestBody(JSON))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "FCM 토큰 서버 전송 실패", e)
                callback.onFailure("서버 연결에 실패했습니다")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful) {
                    Log.d(TAG, "FCM 토큰 서버 전송 성공: $responseBody")
                    callback.onSuccess("알림 등록이 완료되었습니다")
                } else {
                    Log.e(TAG, "FCM 토큰 서버 전송 실패: ${response.code}")
                    callback.onFailure("서버 응답 오류: ${response.code}")
                }
                response.close()
            }
        })
    }
}
