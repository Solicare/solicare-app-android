package com.solicare.monitor.data.repository

import android.content.Context
import com.solicare.monitor.data.remote.HttpJsonHelper
import com.solicare.monitor.domain.repository.DeviceRepository
import com.solicare.monitor.presentation.notification.InfoChannel

class DeviceRepositoryImpl(private val context: Context) : DeviceRepository {
    private val baseUrl = "https://dev-api.solicare.kro.kr/api/firebase/fcm"

    override suspend fun registerFcmToken(token: String): Boolean {
        val response = HttpJsonHelper.putJsonWithoutAuth("$baseUrl/$token", null)
        val isSuccess = response?.optBoolean("isSuccess", false) == true
        if (isSuccess) {
            InfoChannel.send(
                context,
                context.getString(com.solicare.monitor.R.string.device_register_success),
                context.getString(com.solicare.monitor.R.string.device_register_title)
            )
        } else {
            InfoChannel.send(
                context,
                context.getString(com.solicare.monitor.R.string.device_register_fail),
                context.getString(com.solicare.monitor.R.string.device_register_title)
            )
        }
        return isSuccess
    }

    override suspend fun unregisterFcmToken(token: String): Boolean {
        val response = HttpJsonHelper.deleteJsonWithoutAuth("$baseUrl/$token")
        val isSuccess = response?.optBoolean("isSuccess", false) == true
        InfoChannel.register(context)
        if (isSuccess) {
            InfoChannel.send(
                context,
                context.getString(com.solicare.monitor.R.string.device_unregister_success),
                context.getString(com.solicare.monitor.R.string.device_unregister_title)
            )
        } else {
            InfoChannel.send(
                context,
                context.getString(com.solicare.monitor.R.string.device_unregister_fail),
                context.getString(com.solicare.monitor.R.string.device_unregister_title)
            )
        }
        return isSuccess
    }
}