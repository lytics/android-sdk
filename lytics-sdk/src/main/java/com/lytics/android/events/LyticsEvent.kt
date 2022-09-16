package com.lytics.android.events

import org.json.JSONObject

data class LyticsEvent(
    var stream: String? = null,
    var name: String? = null,
    var identifiers: Map<String, String>? = null,
    var properties: Map<String, String>? = null
) {
    fun toJson(): JSONObject {
        return JSONObject()
    }
}
