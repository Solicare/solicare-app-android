package com.solicare.monitor.data.prefs

import android.content.Context
import androidx.core.content.edit

class FcmPrefs(context: Context) {
    private val prefs = context.getSharedPreferences("FCMPrefs", Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        prefs.edit { putString("token", token) }
        saveRegistrationDate(System.currentTimeMillis())
    }

    fun getToken(): String? = prefs.getString("token", null)

    fun saveRegistrationDate(timestamp: Long) {
        prefs.edit { putLong("registration_date", timestamp) }
    }

    fun getRegistrationDate(): Long = prefs.getLong("registration_date", 0L)

    fun clear() {
        prefs.edit { clear() }
    }

    fun saveLastRegisteredToken(token: String) {
        prefs.edit { putString("last_registered_token", token) }
    }

    fun getLastRegisteredToken(): String? = prefs.getString("last_registered_token", null)
}
