package com.solicare.monitor.data.prefs

import android.content.Context
import androidx.core.content.edit

class DevicePrefs(context: Context) {
    private val prefs = context.getSharedPreferences("DevicePrefs", Context.MODE_PRIVATE)

    fun savePhoneNumber(phoneNumber: String) {
        prefs.edit { putString("phoneNumber", phoneNumber) }
    }

    fun getPhoneNumber(): String? = prefs.getString("phoneNumber", null)
}
