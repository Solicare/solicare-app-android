package com.solicare.monitor.data.repository

import com.solicare.monitor.data.remote.HttpJsonHelper
import com.solicare.monitor.domain.repository.FcmRepository
import com.solicare.monitor.util.PhoneNumberUtils
import org.json.JSONObject

class FcmRepositoryImpl : FcmRepository {
    private val baseUrl = "https://api.solicare.kro.kr/fcm"


    // TODO: change parameter name to fcm_token and url path to /fcm/register/device
    override suspend fun registerDevice(token: String, phoneNumber: String): Boolean {
        val formattedPhoneNumber = PhoneNumberUtils.formatPhoneNumber(phoneNumber)
        val json = JSONObject().apply {
            put("token", token)
            put("phoneNumber", formattedPhoneNumber)
        }
        val response = HttpJsonHelper.postJsonWithoutAuth("$baseUrl/register", json)
        return response?.optBoolean("success", false) == true || response?.toString()
            ?.contains("true") == true
    }


    override suspend fun registerUser(jwtToken: String, fcmToken: String): Boolean {
        val json = JSONObject().apply {
            put("fcm_token", fcmToken)
        }
        val response = HttpJsonHelper.postJsonWithAuth("$baseUrl/register/user", json, jwtToken)
        return response?.optBoolean("success", false) == true || response?.toString()
            ?.contains("true") == true
    }

    override suspend fun validateFCMToken(jwtToken: String, fcmToken: String): Boolean {
        val json = JSONObject().apply {
            put("fcm_token", fcmToken)
        }
        val response = HttpJsonHelper.postJsonWithAuth("$baseUrl/validate", json, jwtToken)
        return response?.optBoolean("success", false) == true || response?.toString()
            ?.contains("true") == true
    }
}