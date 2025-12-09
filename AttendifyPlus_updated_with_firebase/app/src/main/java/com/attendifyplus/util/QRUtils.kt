package com.attendifyplus.util

import org.json.JSONObject

data class QRPayload(val type: String = "student", val id: String, val meta: Map<String, String> = emptyMap())

fun makeStudentQR(id: String): String {
    val obj = JSONObject()
    obj.put("t", "student")   // short keys to keep QR small
    obj.put("i", id)
    obj.put("u", System.currentTimeMillis())
    return obj.toString()
}

fun decodeStudentQR(payload: String): String? {
    return try {
        val obj = JSONObject(payload)
        if (obj.optString("t") == "student") obj.optString("i") else null
    } catch (e: Exception) { null }
}