package com.solicare.monitor.data.repository

import android.content.Context
import android.util.Log
import com.solicare.monitor.BuildConfig
import com.solicare.monitor.data.remote.HttpJsonHelper
import com.solicare.monitor.domain.repository.DeviceRepository
import com.solicare.monitor.presentation.notification.InfoChannel

class DeviceRepositoryImpl(private val context: Context) : DeviceRepository {
    private val loggingTag = "DeviceRepositoryImpl"
    private val baseUrl = BuildConfig.BASE_API_URL + "/api"

    override suspend fun registerFcmToken(token: String): String? {
        val response = HttpJsonHelper.putJsonWithoutAuth("$baseUrl/firebase/fcm/$token", null)
        Log.d(loggingTag, "registerFcmToken: $response")
        if (response?.optBoolean("isSuccess", false) == true) {
            return response.optJSONObject("body")?.optString("uuid")
        }
        return ""
    }

    override suspend fun renewFcmToken(oldToken: String, newToken: String): Boolean {
        val jsonBody = org.json.JSONObject(mapOf("oldToken" to oldToken, "newToken" to newToken))
        val response = HttpJsonHelper.postJsonWithoutAuth(
            "$baseUrl/firebase/fcm/renew",
            jsonBody
        )
        Log.d(loggingTag, "renewFcmToken: $response")
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
        val response = HttpJsonHelper.deleteJsonWithoutAuth("$baseUrl/firebase/fcm/$token")
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

    override suspend fun linkDeviceToMember(
        accessToken: String,
        memberUuid: String,
        deviceUuid: String,
    ): Boolean {
        val response = HttpJsonHelper.putJsonWithAuth(
            "$baseUrl/member/${memberUuid}/devices/${deviceUuid}",
            org.json.JSONObject(),
            accessToken
        )
        Log.d(loggingTag, "linkDeviceToMember: $response")
        return response?.optBoolean("isSuccess", false) == true
    }
}