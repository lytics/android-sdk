package com.lytics.android

import org.json.JSONObject

internal data class Response(val statusCode: Int = 0, val data: String? = null) {
    val isOk: Boolean = statusCode in 1..399
    val json: JSONObject?
    get() {
        return if (data != null) {
            kotlin.runCatching { JSONObject(data) }.getOrNull()
        } else {
            null
        }
    }
}
