package com.solicare.monitor.data.prefs

import android.content.Context
import androidx.core.content.edit

class UserPrefs(context: Context) {
    private val prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

    fun getJwtToken(): String? = prefs.getString("jwt_token", null)

    fun saveJwtToken(token: String) {
        prefs.edit { putString("jwt_token", token) }
    }

    fun getMemberUuid(): String? = prefs.getString("member_uuid", null)

    fun saveMemberUuid(uuid: String) {
        prefs.edit { putString("member_uuid", uuid) }
    }
}

