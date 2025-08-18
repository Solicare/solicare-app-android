package com.solicare.monitor.util

object PhoneNumberUtils {
    /**
     * 어떠한 형태의 전화번호든 010-0000-0000 형태로 변환.
     * 변환 불가(자리수 부족 등) 시 숫자만 반환.
     */
    fun formatPhoneNumber(phoneNumber: String): String {
        val normalized = normalizeToLocal(phoneNumber)
        return if (normalized.length == 11 && normalized.startsWith("010")) {
            "${normalized.substring(0, 3)}-${normalized.substring(3, 7)}-${normalized.substring(7)}"
        } else if (normalized.length == 10 && normalized.startsWith("01")) {
            "${normalized.substring(0, 3)}-${normalized.substring(3, 6)}-${normalized.substring(6)}"
        } else {
            normalized // 자리수 부족 등은 숫자만 반환
        }
    }

    /**
     * 국가번호, 하이픈, 0이 빠진 번호 등 다양한 입력을 01012345678 등으로 통일
     */
    fun normalizeToLocal(phoneNumber: String): String {
        val cleaned = onlyDigits(phoneNumber)
        return when {
            cleaned.startsWith("82") && cleaned.length >= 11 ->
                "0" + cleaned.substring(2)

            cleaned.length == 10 && cleaned.startsWith("10") ->
                "0$cleaned"

            else -> cleaned
        }
    }

    /**
     * 국가번호(+82) 포함 형태로 변환 (01012345678 → +82-10-1234-5678)
     */
    fun toInternational(phoneNumber: String): String {
        val cleaned = onlyDigits(phoneNumber)
        val local = normalizeToLocal(cleaned)
        return if (local.length == 11 && local.startsWith("010")) {
            "+82-10-${local.substring(3, 7)}-${local.substring(7)}"
        } else if (local.length == 10 && local.startsWith("01")) {
            "+82-${local.substring(1, 3)}-${local.substring(3, 6)}-${local.substring(6)}"
        } else {
            local
        }
    }

    /**
     * 하이픈 없는 숫자만 반환 (010-1234-5678 → 01012345678)
     */
    fun onlyDigits(phoneNumber: String): String =
        phoneNumber.replace("[^0-9]".toRegex(), "")
}
