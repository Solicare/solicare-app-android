package com.solicare.monitor.data.remote

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

object HttpJsonHelper {
    private val client = OkHttpClient()
    private val JSON = "application/json; charset=utf-8".toMediaType()

    private fun requestJson(
        url: String,
        method: String,
        body: JSONObject? = null,
        jwtToken: String? = null
    ): JSONObject? {
        return try {
            val builder = Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
            if (jwtToken != null) {
                builder.addHeader("Authorization", "Bearer $jwtToken")
            }
            // 항상 Content-Type: application/json 헤더 추가
            builder.addHeader("Content-Type", "application/json")
            val requestBody = when (method) {
                "POST", "PUT", "DELETE" -> (body?.toString() ?: "{}").toRequestBody(JSON)
                else -> null
            }
            builder.method(method, requestBody)
            val request = builder.build()
            val response = client.newCall(request).execute()
            response.body?.string()?.let { JSONObject(it) }
        } catch (e: Exception) {
            null
        }
    }

    fun postJsonWithoutAuth(url: String, body: JSONObject): JSONObject? =
        requestJson(url, "POST", body)

    fun postJsonWithAuth(url: String, body: JSONObject, jwtToken: String): JSONObject? =
        requestJson(url, "POST", body, jwtToken)

    fun getJsonWithAuth(url: String, jwtToken: String): JSONObject? =
        requestJson(url, "GET", null, jwtToken)

    fun putJsonWithAuth(url: String, body: JSONObject, jwtToken: String): JSONObject? =
        requestJson(url, "PUT", body, jwtToken)

    fun deleteJsonWithAuth(url: String, jwtToken: String): JSONObject? =
        requestJson(url, "DELETE", null, jwtToken)

    fun putJsonWithoutAuth(url: String, body: JSONObject?): JSONObject? =
        requestJson(url, "PUT", body)

    fun deleteJsonWithoutAuth(url: String): JSONObject? =
        requestJson(url, "DELETE")
}