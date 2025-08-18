package com.solicare.monitor.data.remote

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

object HttpJsonHelper {
    private val client = OkHttpClient()

    fun postJsonWithoutAuth(url: String, body: JSONObject): JSONObject? {
        return try {
            val requestBody =
                body.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .post(requestBody)
                .build()
            val response = client.newCall(request).execute()
            response.body?.string()?.let { JSONObject(it) }
        } catch (e: Exception) {
            null
        }
    }

    fun postJsonWithAuth(url: String, body: JSONObject, jwtToken: String): JSONObject? {
        return try {
            val requestBody =
                body.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "Bearer $jwtToken")
                .post(requestBody)
                .build()
            val response = client.newCall(request).execute()
            response.body?.string()?.let { JSONObject(it) }
        } catch (e: Exception) {
            null
        }
    }

    fun getJsonWithAuth(url: String, jwtToken: String): JSONObject? {
        return try {
            val request = Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "Bearer $jwtToken")
                .get()
                .build()
            val response = client.newCall(request).execute()
            response.body?.string()?.let { JSONObject(it) }
        } catch (e: Exception) {
            null
        }
    }

    fun putJsonWithAuth(url: String, body: JSONObject, jwtToken: String): JSONObject? {
        return try {
            val requestBody =
                body.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "Bearer $jwtToken")
                .put(requestBody)
                .build()
            val response = client.newCall(request).execute()
            response.body?.string()?.let { JSONObject(it) }
        } catch (e: Exception) {
            null
        }
    }

    fun deleteJsonWithAuth(url: String, jwtToken: String): JSONObject? {
        return try {
            val request = Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "Bearer $jwtToken")
                .delete()
                .build()
            val response = client.newCall(request).execute()
            response.body?.string()?.let { JSONObject(it) }
        } catch (e: Exception) {
            null
        }
    }
}