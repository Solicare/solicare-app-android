package com.solicare.monitor.domain.util;

object JwtUtils {
    fun extractUserUuidFromJwt(jwt: String): String? {
        val parts = jwt.split(".")
        if (parts.size != 3) return null
        val payload = try {
            android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE or android.util.Base64.NO_PADDING or android.util.Base64.NO_WRAP)
        } catch (e: IllegalArgumentException) {
            return null
        }
        val payloadJson = String(payload)
        val regex = """"sub"\s*:\s*"([^"]+)"""".toRegex()
        return regex.find(payloadJson)?.groupValues?.get(1)
    }
}
