package com.solicare.monitor.data.prefs

import android.content.Context
import androidx.core.content.edit

class UserPrefs(context: Context) {
    private val prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

    fun saveJwtToken(token: String) {
        prefs.edit { putString("jwt_token", token) }
    }

    fun getJwtToken(): String? = prefs.getString("jwt_token", null)

    fun saveEmail(id: String) {
        prefs.edit { putString("email", id) }
    }

    fun getEmail(): String? = prefs.getString("email", null)

    fun savePassword(password: String) {
        prefs.edit { putString("pw", password) }
    }

    fun getPassword(): String? = prefs.getString("pw", null)

    fun clear() {
        prefs.edit { clear() }
    }
}

