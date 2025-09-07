package com.solicare.monitor.data.repository

import android.content.Context
import android.util.Log
import com.solicare.monitor.data.remote.HttpJsonHelper
import com.solicare.monitor.domain.repository.DeviceRepository
import com.solicare.monitor.presentation.notification.InfoChannel

class DeviceRepositoryImpl(private val context: Context) : DeviceRepository {
    private val baseUrl =
        (System.getenv("BASE_URL") ?: "https://dev-api.solicare.kro.kr") + "/api/firebase/fcm"

    override suspend fun registerFcmToken(token: String): String? {
        val response = HttpJsonHelper.putJsonWithoutAuth("$baseUrl/$token", null)
        Log.d("DeviceRepository", "registerFcmToken: $response")
        if (response?.optBoolean("isSuccess", false) == true) {
            return response.optJSONObject("body")?.optString("uuid")
        }
        return ""
    }

    override suspend fun renewFcmToken(oldToken: String, newToken: String): Boolean {
        val jsonBody = org.json.JSONObject(mapOf("oldToken" to oldToken, "newToken" to newToken))
        val response = HttpJsonHelper.postJsonWithoutAuth(
            "$baseUrl/renew",
            jsonBody
        )
        Log.d("DeviceRepository", "renewFcmToken: $response")
        val isSuccess = response?.optBoolean("isSuccess", false) == true
        if (isSuccess) {
            InfoChannel.send(
                context,
                context.getString(com.solicare.monitor.R.string.device_renew_title),
                context.getString(com.solicare.monitor.R.string.device_renew_success)
            )
        } else {
            InfoChannel.send(
                context,
                context.getString(com.solicare.monitor.R.string.device_renew_title),
                context.getString(com.solicare.monitor.R.string.device_renew_fail),
            )
        }
        return response?.optBoolean("isSuccess", false) == true
    }

    override suspend fun unregisterFcmToken(token: String): Boolean {
        val response = HttpJsonHelper.deleteJsonWithoutAuth("$baseUrl/$token")
        val isSuccess = response?.optBoolean("isSuccess", false) == true
        InfoChannel.register(context)
        if (isSuccess) {
            InfoChannel.send(
                context,
                context.getString(com.solicare.monitor.R.string.device_unregister_title),
                context.getString(com.solicare.monitor.R.string.device_unregister_success)
            )
        } else {
            InfoChannel.send(
                context,
                context.getString(com.solicare.monitor.R.string.device_unregister_title),
                context.getString(com.solicare.monitor.R.string.device_unregister_fail),
            )
        }
        return isSuccess
    }
}