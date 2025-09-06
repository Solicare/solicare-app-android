package com.solicare.monitor.data.prefs

import android.content.Context
import androidx.core.content.edit

class DevicePrefs(context: Context) {
    private val prefs = context.getSharedPreferences("DevicePrefs", Context.MODE_PRIVATE)

    fun savePhoneNumber(phoneNumber: String) {
        prefs.edit { putString("phoneNumber", phoneNumber) }
    }

    fun getPhoneNumber(): String? = prefs.getString("phoneNumber", null)

    fun saveDeviceUuid(uuid: String) {
        prefs.edit { putString("deviceUuid", uuid) }
    }

    fun getDeviceUuid(): String? = prefs.getString("deviceUuid", null)

    fun clear() {
        prefs.edit { clear() }
    }
}
